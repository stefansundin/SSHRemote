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

import android.os.SystemClock
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
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
        const val SHELL_COMMAND_TIMEOUT_MS = 5_000L

        /** Max time to discard startup output after opening/reinitializing the shell channel. */
        const val SHELL_STARTUP_DRAIN_TIMEOUT_MS = 750L

        /** Stop draining once no bytes have arrived for this long. */
        const val SHELL_STARTUP_QUIET_PERIOD_MS = 120L
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
                Log.d("SshRepository", "Opening new exec channel")
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
     * terminal looks dumb.
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
                        Log.d("SshRepository", "Opening new shell channel")
                        val newChannel = session.openChannel("shell") as ChannelShell
                        newChannel.setPty(true)
                        // TERM=dumb prevents interactive shells (like fish) from stalling while
                        // waiting for answers to their terminal capability queries.
                        newChannel.setPtyType("dumb")
                        // https://www.rfc-editor.org/info/rfc4254/#section-8
                        newChannel.setTerminalMode(
                            byteArrayOf(
                                53, 0, 0, 0, 0, // ECHO OFF
                                0, // TTY_OP_END
                            ),
                        )
                        val newInputStream = newChannel.inputStream
                        val newOutputStream = newChannel.outputStream
                        newChannel.connect(30000)
                        // Force a predictable POSIX shell for command execution regardless of
                        // the account's login shell (for example fish).
                        // In my testing this improves performance by a lot!
                        newOutputStream.write("exec /bin/sh\n".toByteArray())
                        // Disable the prompt and turn off echo again (required on macOS for some reason)
                        newOutputStream.write("PS1=\nexport PS1\nPS2=\nexport PS2\nstty -echo\n".toByteArray())
                        newOutputStream.flush()
                        // Drop any banner/prompt text emitted while the shell is starting so
                        // the next command reads only its own output.
                        drainPendingShellOutput(newInputStream, newChannel.extInputStream)
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

                    // A unique separator is used to mark the end of the command output and carry the exit code
                    val endMarker = "END_OF_COMMAND_${UUID.randomUUID()}"
                    val fullCommand = "$command 2>&1; echo \"${endMarker}$?\"\n"
                    outputStream.write(fullCommand.toByteArray())
                    outputStream.flush()
                    Log.d("SshRepository", "Sent command: $fullCommand")

                    val watchdog = launch {
                        delay(SHELL_COMMAND_TIMEOUT_MS.milliseconds)
                        disconnectChannel()
                    }

                    val buffer = ByteArray(1024)
                    val output = StringBuilder()

                    try {
                        while (true) {
                            coroutineContext.ensureActive()
                            watchdog.ensureActive()

                            val bytesRead = inputStream.read(buffer)
                            if (bytesRead < 0) break
                            val chunk = String(buffer, 0, bytesRead)
                            output.append(chunk)

                            val outputSoFar = output.toString()
                            if (outputSoFar.contains(endMarker)) {
                                val endMarkerIndex = outputSoFar.lastIndexOf(endMarker)
                                if (endMarkerIndex != -1) {
                                    val rest = outputSoFar.substring(endMarkerIndex)
                                    if (rest.contains("\n")) {
                                        break
                                    }
                                }
                            }
                        }
                    } catch (e: IOException) {
                        // The watchdog (or a cancellation) disconnected the channel to unblock this read.
                        Log.e("SshRepository", "Error when running command", e)
                    } finally {
                        watchdog.cancel()
                    }

                    if (channel == null) {
                        return@withContext Result.Error("Command did not complete in ${SHELL_COMMAND_TIMEOUT_MS.milliseconds}.")
                    }

                    val outputString = output.toString()
                    Log.d("SshRepository", "Received output: $outputString")
                    val endMarkerIndex = outputString.lastIndexOf(endMarker)
                    if (endMarkerIndex == -1) {
                        return@withContext Result.Error("Failed to determine command exit status. Output:\n$outputString")
                    }

                    // Extract the command output
                    val commandOutput = outputString.take(endMarkerIndex).trim()

                    // Extract the exit code
                    val markerLine = outputString.substring(endMarkerIndex)
                    val exitCodeString = markerLine.substring(endMarker.length).trim().lines().first()
                    val exitStatus = exitCodeString.toIntOrNull() ?: -1

                    if (exitStatus == 0) {
                        Result.Success(commandOutput)
                    } else {
                        Result.Error("Command failed (Status $exitStatus).\nOutput:\n$commandOutput")
                    }
                } catch (e: CancellationException) {
                    Log.e("SshRepository", "Error when running command", e)
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

    /** Best-effort startup drain to avoid mixing login banners/prompts into command output. */
    private suspend fun drainPendingShellOutput(
        inputStream: InputStream,
        errorStream: InputStream?,
    ) {
        val buffer = ByteArray(1024)
        val deadline = SystemClock.uptimeMillis() + SHELL_STARTUP_DRAIN_TIMEOUT_MS
        var lastDataAt = SystemClock.uptimeMillis()

        suspend fun drainAvailableBytes(stream: InputStream): Boolean {
            var drainedAny = false
            while (runInterruptible(Dispatchers.IO) { stream.available() } > 0) {
                val bytesRead = runInterruptible(Dispatchers.IO) { stream.read(buffer) }
                if (bytesRead <= 0) break
                drainedAny = true
            }
            return drainedAny
        }

        try {
            while (SystemClock.uptimeMillis() < deadline) {
                val drainedStdout = drainAvailableBytes(inputStream)
                val drainedStderr = errorStream?.let { drainAvailableBytes(it) } ?: false
                val drainedAny = drainedStdout || drainedStderr

                if (drainedAny) {
                    lastDataAt = SystemClock.uptimeMillis()
                } else if (SystemClock.uptimeMillis() - lastDataAt >= SHELL_STARTUP_QUIET_PERIOD_MS) {
                    return
                }

                delay(20.milliseconds)
            }
        } catch (_: IOException) {
            // If the channel closes while draining startup output, continue with normal handling.
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
