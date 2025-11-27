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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.stefansundin.sshremote.MainActivity
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.data.host.ConnectionStatus
import com.stefansundin.sshremote.data.host.RemoteControlKey

class NotificationService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val host = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_NOTIFICATION_HOST, NotificationHost::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_NOTIFICATION_HOST)
                }

                if (host != null) {
                    val notification = createNotification(host)
                    startForeground(NOTIFICATION_ID, notification)
                }
                return START_STICKY
            }

            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }

            else -> {
                return START_NOT_STICKY
            }
        }
    }

    private fun createNotification(host: NotificationHost): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val remoteViews = RemoteViews(packageName, R.layout.notification_remote_control).apply {
            setOnClickPendingIntent(R.id.button_up, createPendingIntent(host, RemoteControlKey.UP))
            setOnClickPendingIntent(R.id.button_down, createPendingIntent(host, RemoteControlKey.DOWN))
            setOnClickPendingIntent(R.id.button_left, createPendingIntent(host, RemoteControlKey.LEFT))
            setOnClickPendingIntent(R.id.button_right, createPendingIntent(host, RemoteControlKey.RIGHT))
            setOnClickPendingIntent(R.id.button_select, createPendingIntent(host, RemoteControlKey.SELECT))
            setOnClickPendingIntent(R.id.button_back, createPendingIntent(host, RemoteControlKey.BACK))
            setOnClickPendingIntent(R.id.button_home, createPendingIntent(host, RemoteControlKey.HOME))
            setOnClickPendingIntent(R.id.button_play_pause, createPendingIntent(host, RemoteControlKey.PLAY_PAUSE))
            setOnClickPendingIntent(R.id.button_volume_up, createPendingIntent(host, RemoteControlKey.VOLUME_UP))
            setOnClickPendingIntent(R.id.button_volume_down, createPendingIntent(host, RemoteControlKey.VOLUME_DOWN))
            setOnClickPendingIntent(R.id.button_mute, createPendingIntent(host, RemoteControlKey.MUTE))
        }
        val closeIntent = Intent(this, NotificationBroadcastReceiver::class.java).apply {
            action = ACTION_STOP
        }
        val closePendingIntent = PendingIntent.getBroadcast(
            this, 0, closeIntent, PendingIntent.FLAG_IMMUTABLE,
        )

        val title: String
        val text: String
        when (host.status) {
            ConnectionStatus.CONNECTED -> {
                title = "Connected to ${host.name}"
                text = "Remote control active"
            }

            ConnectionStatus.CONNECTING -> {
                title = "Connecting to ${host.name}"
                text = "Please wait..."
            }

            ConnectionStatus.DISCONNECTED -> {
                title = "Disconnected from ${host.name}"
                text = "Remote control inactive"
            }
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setSound(null)
            .setOngoing(false) // Allow swipe to dismiss
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomBigContentView(remoteViews)
            .addAction(NotificationCompat.Action.Builder(null, "Open", pendingIntent).build())
            .addAction(NotificationCompat.Action.Builder(null, "Close", closePendingIntent).build())
            .build()
    }

    private fun createPendingIntent(host: NotificationHost, key: RemoteControlKey): PendingIntent {
        val intent = Intent(this, NotificationBroadcastReceiver::class.java).apply {
            action = ACTION_EXECUTE_COMMAND
            putExtra(EXTRA_HOST_ID, host.id)
            putExtra(EXTRA_REMOTE_CONTROL_KEY, key)
        }
        return PendingIntent.getBroadcast(
            this,
            key.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val EXTRA_NOTIFICATION_HOST = "com.stefansundin.sshremote.notification.EXTRA_NOTIFICATION_HOST"
        const val EXTRA_HOST_ID = "com.stefansundin.sshremote.notification.EXTRA_HOST_ID"
        const val EXTRA_REMOTE_CONTROL_KEY = "com.stefansundin.sshremote.notification.EXTRA_REMOTE_CONTROL_KEY"
        const val ACTION_START = "com.stefansundin.sshremote.notification.ACTION_START"
        const val ACTION_STOP = "com.stefansundin.sshremote.notification.ACTION_STOP"
        const val ACTION_EXECUTE_COMMAND = "com.stefansundin.sshremote.notification.ACTION_EXECUTE_COMMAND"
        private const val CHANNEL_ID = "NotificationServiceChannel"
        private const val NOTIFICATION_ID = 1

        fun start(context: Context, host: NotificationHost) {
            val intent = Intent(context, NotificationService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_NOTIFICATION_HOST, host)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, NotificationService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val serviceChannel = NotificationChannel(
                    CHANNEL_ID,
                    "Remote Control",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    setSound(null, null)
                    setShowBadge(false)
                }
                val manager = context.getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(serviceChannel)
            }
        }
    }
}
