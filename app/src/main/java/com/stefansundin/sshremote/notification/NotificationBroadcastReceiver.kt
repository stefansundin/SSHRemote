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

package com.stefansundin.sshremote.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.stefansundin.sshremote.data.host.RemoteControlKey

// TODO: Check if MainActivity is still running.
// Longer term, NotificationService should be able to connect to the host on its own without having to pass messages via MainActivity.
// You can test this by enabling "Don't keep activities" in the developer options.

class NotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            NotificationService.ACTION_EXECUTE_COMMAND -> {
                val hostId = intent.getIntExtra(NotificationService.EXTRA_HOST_ID, -1)

                val remoteControlKey = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getSerializableExtra(
                        NotificationService.EXTRA_REMOTE_CONTROL_KEY,
                        RemoteControlKey::class.java,
                    )
                } else {
                    @Suppress("DEPRECATION")
                    intent.getSerializableExtra(NotificationService.EXTRA_REMOTE_CONTROL_KEY) as? RemoteControlKey
                }

                if (hostId != -1 && remoteControlKey != null) {
                    // Send the intent to MainActivity
                    val executeIntent = Intent(NotificationService.ACTION_EXECUTE_COMMAND).apply {
                        putExtra(NotificationService.EXTRA_HOST_ID, hostId.toString())
                        putExtra(NotificationService.EXTRA_REMOTE_CONTROL_KEY, remoteControlKey.name)
                        setPackage(context.packageName)
                    }
                    context.sendBroadcast(executeIntent)
                }
            }

            NotificationService.ACTION_STOP -> {
                NotificationService.stop(context)
            }
        }
    }
}
