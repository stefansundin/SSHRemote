/*
SSH Remote
Copyright (C) 2025  Stefan Sundin

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.stefansundin.sshremote

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.Properties

sealed class Result {
    data class Success(val output: String) : Result()
    data class Error(val message: String) : Result()
}

/**
 * A repository for handling SSH connection and command execution.
 */
class SshRepository {

    private var session: Session? = null

    /**
     * Connects to an SSH server. This is a suspending function and must be called
     * from a coroutine, preferably on an IO dispatcher.
     *
     * @param details The server details from the database.
     * @throws Exception if connection fails.
     */
    suspend fun connect(details: SshServerConnectionDetails) {
        withContext(Dispatchers.IO) {
            if (session?.isConnected == true) {
                session?.disconnect()
            }

            JSch.setLogger(JschLogger())

            val jsch = JSch()
            session = jsch.getSession(details.user, details.host, details.port)
            details.password?.let { session?.setPassword(it) }

            // TODO: manage known_hosts
            val config = Properties()
            config["StrictHostKeyChecking"] = "no"
            session?.setConfig(config)

            session?.connect(30000) // 30-second timeout
        }
    }

    /**
     * Executes a command on the currently connected SSH session.
     *
     * @param command The command string to execute.
     * @return The output from the command.
     * @throws Exception if not connected or command fails.
     */
    suspend fun executeCommand(command: String): Result {
        return withContext(Dispatchers.IO) {

            val session = session

            if (session == null || !session.isConnected) {
                return@withContext Result.Error("SSH session is not active or target server mismatch. Please reconnect.")
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
                return@withContext Result.Error("Execution failed: ${e.message}")
            } finally {
                channel?.disconnect()
            }
        }
    }

    /**
     * Disconnects the current session.
     */
    suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            session?.disconnect()
            session = null
        }
    }
}
