/*
 * SSH Remote
 * Copyright (C) 2026  Stefan Sundin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.stefansundin.sshremote

import android.util.Log
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.HostKey
import com.jcraft.jsch.JSch
import com.jcraft.jsch.OpenSSHConfig
import com.jcraft.jsch.Session
import com.jcraft.jsch.UserInfo
import com.stefansundin.sshremote.data.host.HostConnectionDetails
import com.stefansundin.sshremote.data.settings.SettingsRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.concurrent.CancellationException
import kotlin.time.Duration.Companion.milliseconds

sealed class Result {
    data class Success(val output: String) : Result()
    data class Error(val message: String, val isConnectionError: Boolean = false) : Result()
}

data class HostKeyVerification(
    val message: String,
    val response: CompletableDeferred<Boolean>,
)

data class PasswordPrompt(
    val message: String,
    val response: CompletableDeferred<String?>,
)

data class PassphrasePrompt(
    val message: String,
    val response: CompletableDeferred<String?>,
)

data class Message(
    val message: String,
    val response: CompletableDeferred<Unit>,
)

interface ISshRepository {
    val hostKeyVerification: StateFlow<HostKeyVerification?>
    val message: StateFlow<Message?>
    val passwordPrompt: StateFlow<PasswordPrompt?>
    val passphrasePrompt: StateFlow<PassphrasePrompt?>

    suspend fun connect(details: HostConnectionDetails): HostKey?
    fun onHostKeyVerificationComplete(result: Boolean)
    fun onMessageDismissed()
    fun onPasswordPromptComplete(password: String?)
    fun onPassphrasePromptComplete(passphrase: String?)
    suspend fun executeCommand(command: String): Result
    suspend fun executeCommandReuseShell(command: String): Result
    suspend fun disconnect()
}

/**
 * A repository for handling SSH connection and command execution.
 */
class SshRepository(private val settingsRepository: SettingsRepository) : ISshRepository {

    private companion object {
        /** Commands sent over the reusable shell channel are expected to finish quickly. */
        const val ShellCommandTimeoutMillis = 5_000L
    }

    private val commandMutex = Mutex()

    private var session: Session? = null
    private var channel: ChannelShell? = null
    private var channelInputStream: InputStream? = null
    private var channelOutputStream: OutputStream? = null

    private val _hostKeyVerification = MutableStateFlow<HostKeyVerification?>(null)
    override val hostKeyVerification = _hostKeyVerification.asStateFlow()

    private val _message = MutableStateFlow<Message?>(null)
    override val message = _message.asStateFlow()

    private val _passwordPrompt = MutableStateFlow<PasswordPrompt?>(null)
    override val passwordPrompt = _passwordPrompt.asStateFlow()

    private val _passphrasePrompt = MutableStateFlow<PassphrasePrompt?>(null)
    override val passphrasePrompt = _passphrasePrompt.asStateFlow()

    /**
     * Connects to a host. This is a suspending function and must be called
     * from a coroutine, preferably on an IO dispatcher.
     *
     * @param details The connection details from the database.
     * @throws Exception if connection fails.
     */
    override suspend fun connect(details: HostConnectionDetails): HostKey? {
        return withContext(Dispatchers.IO) {
            clearPendingPrompts()
            disconnectChannel()
            session?.disconnect()
            session = null

            if (BuildConfig.DEBUG) {
                JSch.setLogger(JschLogger())
            }

            val jsch = JSch()
            jsch.configRepository = OpenSSHConfig.parse(details.sshConfig)

            val useStrictHostKeyChecking = settingsRepository.strictHostKeyChecking.first()
            val allowPasswordPrompting = settingsRepository.allowPasswordPrompting.first()

            if (useStrictHostKeyChecking) {
                details.knownHosts.joinToString("\n").let { jsch.setKnownHosts(it.byteInputStream()) }
            }

            details.identities?.forEach { identity ->
                val certificate = identity.certificate?.toByteArray()
                jsch.addIdentity(identity.name, identity.privateKey.toByteArray(), certificate, null)
            }

            val newSession = jsch.getSession(details.user, details.hostname, details.port)
            session = newSession

            val strictHostKeyChecking = if (useStrictHostKeyChecking) "ask" else "no"
            newSession.setConfig("StrictHostKeyChecking", strictHostKeyChecking)

            newSession.userInfo = object : UserInfo {
                var passwordPromptMessage: String? = null
                var passphrasePromptMessage: String? = null
                var userCancelledAuth = false

                override fun promptYesNo(message: String): Boolean {
                    val deferred = CompletableDeferred<Boolean>()
                    _hostKeyVerification.value = HostKeyVerification(message, deferred)
                    val result = runBlocking { deferred.await() }
                    _hostKeyVerification.value = null
                    return result
                }

                override fun showMessage(message: String) {
                    val deferred = CompletableDeferred<Unit>()
                    _message.value = Message(message, deferred)
                    runBlocking { deferred.await() }
                    _message.value = null
                }

                override fun promptPassword(message: String): Boolean {
                    if (userCancelledAuth || !allowPasswordPrompting) return false
                    passwordPromptMessage = message
                    return true
                }

                override fun getPassword(): String? {
                    val deferred = CompletableDeferred<String?>()
                    _passwordPrompt.value = PasswordPrompt(passwordPromptMessage ?: "Enter password", deferred)
                    val result = runBlocking { deferred.await() }
                    _passwordPrompt.value = null
                    if (result == null) {
                        userCancelledAuth = true
                    }
                    return result
                }

                override fun getPassphrase(): String? {
                    val deferred = CompletableDeferred<String?>()
                    _passphrasePrompt.value =
                        PassphrasePrompt(passphrasePromptMessage ?: "Enter passphrase for private key", deferred)
                    val result = runBlocking { deferred.await() }
                    _passphrasePrompt.value = null
                    if (result == null) {
                        userCancelledAuth = true
                    }
                    return result
                }

                override fun promptPassphrase(message: String): Boolean {
                    if (userCancelledAuth) return false
                    passphrasePromptMessage = message
                    return true
                }
            }

            details.password?.let { newSession.setPassword(it.toByteArray()) }

            Log.d("SshRepository", "Connecting to ${details.hostname}")
            runInterruptible(Dispatchers.IO) {
                newSession.connect(30000) // 30-second timeout
            }

            return@withContext newSession.hostKey
        }
    }

    override fun onHostKeyVerificationComplete(result: Boolean) {
        _hostKeyVerification.value?.response?.complete(result)
    }

    override fun onMessageDismissed() {
        _message.value?.response?.complete(Unit)
    }

    override fun onPasswordPromptComplete(password: String?) {
        _passwordPrompt.value?.response?.complete(password)
    }

    override fun onPassphrasePromptComplete(passphrase: String?) {
        _passphrasePrompt.value?.response?.complete(passphrase)
    }

    /**
     * Executes a command on the currently connected SSH session.
     * This function always opens a new shell channel.
     *
     * @param command The command string to execute.
     * @return The result (output and exit code, or error information) from the command.
     * @throws Exception if not connected or command fails.
     */
    override suspend fun executeCommand(command: String): Result {
        return withContext(Dispatchers.IO) {
            val session = session

            if (session == null || !session.isConnected) {
                return@withContext Result.Error(
                    "SSH session is not active. Please reconnect.",
                    isConnectionError = true,
                )
            }

            var channel: ChannelExec? = null
            try {
                channel = session.openChannel("exec") as ChannelExec
                channel.setCommand(command)

                val inputStream: InputStream = channel.inputStream
                val errorStream: InputStream = channel.extInputStream

                channel.connect()

                val buffer = ByteArray(1024)
                val output = StringBuilder()

                while (true) {
                    currentCoroutineContext().ensureActive()

                    // Read stdout
                    while (inputStream.available() > 0) {
                        val i = inputStream.read(buffer, 0, 1024)
                        if (i < 0) break
                        output.append(String(buffer, 0, i))
                    }

                    // Read stderr
                    while (errorStream.available() > 0) {
                        val i = errorStream.read(buffer, 0, 1024)
                        if (i < 0) break
                        output.append("\n[ERROR] ").append(String(buffer, 0, i))
                    }

                    // Check channel status and break condition
                    if (channel.isClosed) {
                        // Only break if the channel is closed AND both streams are empty
                        if (inputStream.available() <= 0 && errorStream.available() <= 0) {
                            break
                        }
                    }
                    delay(100.milliseconds)
                }

                val exitStatus = channel.exitStatus
                channel.disconnect()

                if (exitStatus == 0) {
                    return@withContext Result.Success(output.toString())
                } else {
                    return@withContext Result.Error("Command failed (Status $exitStatus). Output:\n${output}")
                }

            } catch (e: CancellationException) {
                channel?.disconnect()
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                channel?.disconnect()
                return@withContext Result.Error("Execution failed: ${e.message}", isConnectionError = true)
            } finally {
                channel?.disconnect()
            }
        }
    }

    /**
     * Executes a command on the currently connected SSH session.
     * This function reuses the existing channel if available, otherwise creates a new one.
     *
     * The channel requests a PTY with TERM=dumb. Some servers refuse to run a plain shell without
     * a PTY, and interactive shells like fish stall on terminal capability queries unless the
     * terminal looks dumb. The command and the end marker are sent as separate lines using only
     * shell syntax that is supported everywhere (no `$?`, no `2>&1`), so this works in POSIX
     * shells, fish, etc. The exit status of the command is therefore not reported, but the
     * command's stderr is captured via the channel's extended data stream. The marker echo uses
     * split quoting (`echo END_OF_COMMAND"$id"`), so a terminal echoing back the sent lines
     * cannot be mistaken for the marker; only the actual output matches.
     *
     * @param command The command string to execute.
     * @return The output from the command, or error information if the command could not be completed.
     */
    override suspend fun executeCommandReuseShell(command: String): Result {
        return commandMutex.withLock {
            withContext(Dispatchers.IO) {
                try {
                    val session = session
                    if (session == null || !session.isConnected) {
                        Log.e("SshRepository", "Cannot execute command, session is null or not connected.")
                        return@withContext Result.Error(
                            "SSH session is not active. Please reconnect.",
                            isConnectionError = true,
                        )
                    }

                    if (channel == null || channel?.isConnected != true) {
                        disconnectChannel()
                        val newChannel = session.openChannel("shell") as ChannelShell
                        newChannel.setPty(true)
                        // TERM=dumb prevents interactive shells (like fish) from stalling while
                        // waiting for answers to their terminal capability queries.
                        newChannel.setPtyType("dumb")
                        val newInputStream = newChannel.inputStream
                        val newOutputStream = newChannel.outputStream
                        newChannel.connect(30000)
                        channel = newChannel
                        channelInputStream = newInputStream
                        channelOutputStream = newOutputStream
                    }

                    val outputStream = channelOutputStream ?: return@withContext Result.Error(
                        "Channel output stream is null",
                        isConnectionError = true,
                    )
                    val inputStream = channelInputStream ?: return@withContext Result.Error(
                        "Channel input stream is null",
                        isConnectionError = true,
                    )
                    val errorStream = channel?.extInputStream

                    // The end marker is printed as its own line using nothing but `echo`, which
                    // every shell understands. The quotes around the id ensure that a terminal
                    // echoing back this line does not itself contain the marker that is being
                    // waited for; only the actual command output does.
                    val markerId = UUID.randomUUID().toString()
                    val endMarker = "END_OF_COMMAND$markerId"
                    outputStream.write("$command\necho END_OF_COMMAND\"$markerId\"\n".toByteArray())
                    outputStream.flush()

                    val outputString = readUntilEndMarker(inputStream, errorStream, endMarker)

                    if (outputString == null) {
                        // The marker never arrived, so the channel is in an unknown state. Discard
                        // it to guarantee the next call starts with a fresh shell.
                        disconnectChannel()
                        Log.e("SshRepository", "Command did not complete: '$command'")
                        return@withContext Result.Error("Command did not complete.")
                    }

                    // Extract the command output
                    val endMarkerIndex = outputString.lastIndexOf(endMarker)
                    val commandOutput = outputString.take(endMarkerIndex).trim()
                    Result.Success(commandOutput)
                } catch (e: CancellationException) {
                    // Unblock this thread in case a read is still waiting on the channel:
                    disconnectChannel()
                    throw e
                } catch (e: Exception) {
                    Log.e("SshRepository", "Execution failed for '$command'.", e)
                    disconnectChannel()
                    Result.Error("Execution failed: ${e.message}", isConnectionError = true)
                }
            }
        }
    }

    /**
     * Reads from [inputStream] until [endMarker] followed by a newline appears in the output,
     * and returns everything that was read, or null if the stream ends or the command times out
     * before that happens. Anything received on [errorStream] is appended to the output as well.
     * A watchdog disconnects the shell channel on timeout so that the blocking read cannot stall
     * forever and hold up other commands.
     */
    private fun CoroutineScope.readUntilEndMarker(
        inputStream: InputStream,
        errorStream: InputStream?,
        endMarker: String,
    ): String? {
        val buffer = ByteArray(1024)
        val output = StringBuilder()

        val watchdog = launch {
            delay(ShellCommandTimeoutMillis)
            disconnectChannel()
        }

        try {
            while (true) {
                coroutineContext.ensureActive()

                val bytesRead = inputStream.read(buffer)
                if (bytesRead < 0) break
                output.append(String(buffer, 0, bytesRead))

                // Drain any stderr that arrived alongside the stdout.
                if (errorStream != null) {
                    while (errorStream.available() > 0) {
                        val bytesAvailable = errorStream.read(buffer)
                        if (bytesAvailable < 0) break
                        output.append(String(buffer, 0, bytesAvailable))
                    }
                }

                val outputSoFar = output.toString()
                val endMarkerIndex = outputSoFar.lastIndexOf(endMarker)
                if (endMarkerIndex != -1 && outputSoFar.indexOf('\n', endMarkerIndex) != -1) {
                    return outputSoFar
                }
            }
            return null
        } catch (e: IOException) {
            // The watchdog (or a cancellation) disconnected the channel to unblock this read.
            return null
        } finally {
            watchdog.cancel()
        }
    }

    private fun disconnectChannel() {
        channel?.disconnect()
        channel = null
        channelInputStream = null
        channelOutputStream = null
    }

    private fun clearPendingPrompts() {
        _hostKeyVerification.value?.response?.complete(false)
        _message.value?.response?.complete(Unit)
        _passwordPrompt.value?.response?.complete(null)
        _passphrasePrompt.value?.response?.complete(null)

        _hostKeyVerification.value = null
        _message.value = null
        _passwordPrompt.value = null
        _passphrasePrompt.value = null
    }

    /**
     * Disconnects the current session.
     */
    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            disconnectChannel()
            session?.disconnect()
            session = null
            clearPendingPrompts()
        }
    }
}
