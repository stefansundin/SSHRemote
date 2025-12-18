/*
 * SSH Remote
 * Copyright (C) 2025  Stefan Sundin
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
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.UserInfo
import com.stefansundin.sshremote.data.host.HostConnectionDetails
import com.stefansundin.sshremote.data.settings.SettingsRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.Properties
import java.util.UUID

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

/**
 * A repository for handling SSH connection and command execution.
 */
class SshRepository(private val settingsRepository: SettingsRepository) {

    private val commandMutex = Mutex()

    private var session: Session? = null
    private var channel: ChannelShell? = null
    private var channelInputStream: InputStream? = null
    private var channelOutputStream: OutputStream? = null

    private val _hostKeyVerification = MutableStateFlow<HostKeyVerification?>(null)
    val hostKeyVerification = _hostKeyVerification.asStateFlow()

    private val _message = MutableStateFlow<Message?>(null)
    val message = _message.asStateFlow()

    private val _passwordPrompt = MutableStateFlow<PasswordPrompt?>(null)
    val passwordPrompt = _passwordPrompt.asStateFlow()

    private val _passphrasePrompt = MutableStateFlow<PassphrasePrompt?>(null)
    val passphrasePrompt = _passphrasePrompt.asStateFlow()

    /**
     * Connects to a host. This is a suspending function and must be called
     * from a coroutine, preferably on an IO dispatcher.
     *
     * @param details The connection details from the database.
     * @throws Exception if connection fails.
     */
    suspend fun connect(details: HostConnectionDetails): List<String> {
        return withContext(Dispatchers.IO) {
            session?.disconnect()

            JSch.setLogger(JschLogger())

            val jsch = JSch()

            val useStrictHostKeyChecking = settingsRepository.strictHostKeyChecking.first()
            if (useStrictHostKeyChecking) {
                details.knownHosts.joinToString("\n").let { jsch.setKnownHosts(it.byteInputStream()) }
            }

            details.privateKeys?.forEach { (name, key) ->
                jsch.addIdentity(name, key.toByteArray(), null, null)
            }

            val newSession = jsch.getSession(details.user, details.hostname, details.port)
            session = newSession

            newSession.userInfo = object : UserInfo {
                var passwordPromptMessage: String? = null
                var passphrasePromptMessage: String? = null
                var userCancelledAuth = false

                override fun promptYesNo(message: String): Boolean {
                    val deferred = CompletableDeferred<Boolean>()
                    _hostKeyVerification.value = HostKeyVerification(message, deferred)
                    val result = kotlinx.coroutines.runBlocking { deferred.await() }
                    _hostKeyVerification.value = null
                    return result
                }

                override fun showMessage(message: String) {
                    val deferred = CompletableDeferred<Unit>()
                    _message.value = Message(message, deferred)
                    kotlinx.coroutines.runBlocking { deferred.await() }
                    _message.value = null
                }

                override fun promptPassword(message: String): Boolean {
                    if (userCancelledAuth) return false
                    passwordPromptMessage = message
                    return true
                }

                override fun getPassword(): String? {
                    val deferred = CompletableDeferred<String?>()
                    _passwordPrompt.value = PasswordPrompt(passwordPromptMessage ?: "Enter password", deferred)
                    val result = kotlinx.coroutines.runBlocking { deferred.await() }
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
                    val result = kotlinx.coroutines.runBlocking { deferred.await() }
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

            details.password?.let { newSession.setPassword(it) }

            val config = Properties()
            val strictHostKeyChecking = if (useStrictHostKeyChecking) "ask" else "no"
            config["StrictHostKeyChecking"] = strictHostKeyChecking
            newSession.setConfig(config)

            newSession.connect(30000) // 30-second timeout

            val hostKeyRepository = jsch.hostKeyRepository
            val newKnownHosts = hostKeyRepository.hostKey.joinToString("\n") { "${it.host} ${it.type} ${it.key}" }

            return@withContext newKnownHosts.split("\n").filter { it.isNotEmpty() }
        }
    }

    fun onHostKeyVerificationComplete(result: Boolean) {
        _hostKeyVerification.value?.response?.complete(result)
    }

    fun onMessageDismissed() {
        _message.value?.response?.complete(Unit)
    }

    fun onPasswordPromptComplete(password: String?) {
        _passwordPrompt.value?.response?.complete(password)
    }

    fun onPassphrasePromptComplete(passphrase: String?) {
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
    suspend fun executeCommand(command: String): Result {
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
                    Thread.sleep(100)
                }

                val exitStatus = channel.exitStatus
                channel.disconnect()

                if (exitStatus == 0) {
                    return@withContext Result.Success("Output:\n${output}")
                } else {
                    return@withContext Result.Error("Command failed (Status $exitStatus). Output:\n${output}")
                }

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
     * @param command The command string to execute.
     * @return The result (output and exit code, or error information) from the command.
     * @throws Exception if not connected or command fails.
     */
    suspend fun executeCommandReuseShell(command: String): Result {
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
                        newChannel.setPty(false)
                        newChannel.connect(30000)
                        channel = newChannel
                        channelInputStream = newChannel.inputStream
                        channelOutputStream = newChannel.outputStream
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

                    val buffer = ByteArray(1024)
                    val output = StringBuilder()

                    // Read until the marker appears in the output
                    while (true) {
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

                    val outputString = output.toString()
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

                } catch (e: Exception) {
                    e.printStackTrace()
                    disconnectChannel()
                    Result.Error("Execution failed: ${e.message}", isConnectionError = true)
                }
            }
        }
    }

    private fun disconnectChannel() {
        channel?.disconnect()
        channel = null
        channelInputStream = null
        channelOutputStream = null
    }

    /**
     * Disconnects the current session.
     */
    suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            disconnectChannel()
            session?.disconnect()
            session = null
        }
    }
}
