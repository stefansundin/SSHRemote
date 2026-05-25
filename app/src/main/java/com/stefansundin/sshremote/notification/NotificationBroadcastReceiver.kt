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

package com.stefansundin.sshremote.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.stefansundin.sshremote.EXTRA_HOST_ID
import com.stefansundin.sshremote.EXTRA_REMOTE_CONTROL_KEY
import com.stefansundin.sshremote.Result
import com.stefansundin.sshremote.SshRemoteApplication
import com.stefansundin.sshremote.data.host.ConnectionStatus
import com.stefansundin.sshremote.data.host.RemoteControlKey
import kotlinx.coroutines.launch

class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            NotificationController.ACTION_EXECUTE_COMMAND -> {
                val hostId = intent.getStringExtra(EXTRA_HOST_ID)
                val remoteControlKeyString = intent.getStringExtra(EXTRA_REMOTE_CONTROL_KEY)
                if (hostId.isNullOrEmpty() || remoteControlKeyString.isNullOrEmpty()) {
                    return
                }

                val remoteControlKey = try {
                    RemoteControlKey.valueOf(remoteControlKeyString)
                } catch (e: IllegalArgumentException) {
                    Log.e("NotificationReceiver", "Invalid RemoteControlKey received: $remoteControlKeyString", e)
                    return
                }

                val app = context.applicationContext as SshRemoteApplication
                val connectionState = app.activeConnectionTracker.state.value
                if (connectionState.connectionStatus != ConnectionStatus.CONNECTED || connectionState.hostId != hostId) {
                    Log.d(
                        "NotificationReceiver",
                        "Ignoring notification command because no matching active connection exists.",
                    )
                    return
                }

                val pendingResult = goAsync()
                app.applicationScope.launch {
                    try {
                        val host = app.hostRepository.getOnce(hostId)
                        val command = host?.remoteCommands?.get(remoteControlKey)
                        if (command == null) {
                            Log.w("NotificationReceiver", "No command found for key $remoteControlKey on host $hostId.")
                            return@launch
                        }

                        when (val result = app.sshRepository.executeCommand(command.command)) {
                            is Result.Success -> {
                                Log.d(
                                    "NotificationReceiver",
                                    "Executed notification command $remoteControlKey for host $hostId.",
                                )
                            }

                            is Result.Error -> {
                                Log.w(
                                    "NotificationReceiver",
                                    "Notification command failed for host $hostId: ${result.message}",
                                )
                            }
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            NotificationController.ACTION_STOP -> {
                NotificationController.stop(context)
            }
        }
    }
}
