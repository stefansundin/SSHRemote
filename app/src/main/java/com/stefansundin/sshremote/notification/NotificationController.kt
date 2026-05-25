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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.stefansundin.sshremote.EXTRA_HOST_ID
import com.stefansundin.sshremote.EXTRA_REMOTE_CONTROL_KEY
import com.stefansundin.sshremote.MainActivity
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.data.host.ConnectionStatus
import com.stefansundin.sshremote.data.host.RemoteControlKey

object NotificationController {
    const val ACTION_STOP = "com.stefansundin.sshremote.notification.ACTION_STOP"
    const val ACTION_EXECUTE_COMMAND = "com.stefansundin.sshremote.notification.ACTION_EXECUTE_COMMAND"

    private const val CHANNEL_ID = "NotificationServiceChannel"
    private const val NOTIFICATION_ID = 1

    private var currentHost: NotificationHost? = null
    private var isCommandLoading = false

    fun show(context: Context, host: NotificationHost) {
        if (currentHost?.id != host.id || host.status != ConnectionStatus.CONNECTED) {
            isCommandLoading = false
        }
        currentHost = host
        NotificationCleanupService.start(context)
        updateNotification(context)
    }

    fun stop(context: Context) {
        currentHost = null
        isCommandLoading = false
        NotificationCleanupService.stop(context)
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.cancel(NOTIFICATION_ID)
    }

    fun commandStarted(context: Context, hostId: String) {
        if (currentHost?.id == hostId) {
            isCommandLoading = true
            updateNotification(context)
        }
    }

    fun commandFinished(context: Context, hostId: String) {
        if (currentHost?.id == hostId) {
            isCommandLoading = false
            updateNotification(context)
        }
    }

    private fun updateNotification(context: Context) {
        val host = currentHost ?: return
        val notification = createNotification(context, host, isCommandLoading)
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotification(
        context: Context,
        host: NotificationHost,
        isCommandLoading: Boolean,
    ): Notification {
        val openPendingIntent = createOpenPendingIntent(context, host)
        val closePendingIntent = PendingIntent.getBroadcast(
            context,
            host.id.hashCode(),
            Intent(context, NotificationBroadcastReceiver::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val title: String
        val text: String
        when (host.status) {
            ConnectionStatus.CONNECTED -> {
                title = context.getString(R.string.notification_connected_to, host.name)
                text = context.getString(R.string.notification_active)
            }

            ConnectionStatus.CONNECTING -> {
                title = context.getString(R.string.notification_connecting_to, host.name)
                text = context.getString(R.string.please_wait)
            }

            ConnectionStatus.DISCONNECTED -> {
                title = context.getString(R.string.notification_disconnected_from, host.name)
                text = context.getString(R.string.notification_inactive)
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setSound(null)
            .setOnlyAlertOnce(true)
            .setOngoing(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                NotificationCompat.Action.Builder(
                    null,
                    context.getString(R.string.notification_open),
                    openPendingIntent,
                ).build(),
            )
            .addAction(
                NotificationCompat.Action.Builder(
                    null,
                    context.getString(R.string.notification_close),
                    closePendingIntent,
                ).build(),
            )

        if (host.status == ConnectionStatus.CONNECTED) {
            val remoteViews = RemoteViews(context.packageName, R.layout.notification_remote_control).apply {
                setViewVisibility(
                    R.id.command_loading_indicator,
                    if (isCommandLoading) android.view.View.VISIBLE else android.view.View.GONE,
                )
                setOnClickPendingIntent(R.id.button_up, createCommandPendingIntent(context, host, RemoteControlKey.UP))
                setOnClickPendingIntent(
                    R.id.button_down,
                    createCommandPendingIntent(context, host, RemoteControlKey.DOWN),
                )
                setOnClickPendingIntent(
                    R.id.button_left,
                    createCommandPendingIntent(context, host, RemoteControlKey.LEFT),
                )
                setOnClickPendingIntent(
                    R.id.button_right,
                    createCommandPendingIntent(context, host, RemoteControlKey.RIGHT),
                )
                setOnClickPendingIntent(
                    R.id.button_select,
                    createCommandPendingIntent(context, host, RemoteControlKey.SELECT),
                )
                setOnClickPendingIntent(
                    R.id.button_back,
                    createCommandPendingIntent(context, host, RemoteControlKey.BACK),
                )
                setOnClickPendingIntent(
                    R.id.button_home,
                    createCommandPendingIntent(context, host, RemoteControlKey.HOME),
                )
                setOnClickPendingIntent(
                    R.id.button_play_pause,
                    createCommandPendingIntent(context, host, RemoteControlKey.PLAY_PAUSE),
                )
                setOnClickPendingIntent(
                    R.id.button_volume_up,
                    createCommandPendingIntent(context, host, RemoteControlKey.VOLUME_UP),
                )
                setOnClickPendingIntent(
                    R.id.button_volume_down,
                    createCommandPendingIntent(context, host, RemoteControlKey.VOLUME_DOWN),
                )
                setOnClickPendingIntent(
                    R.id.button_mute,
                    createCommandPendingIntent(context, host, RemoteControlKey.MUTE),
                )
            }
            builder
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomBigContentView(remoteViews)
        }

        return builder.build()
    }

    private fun createOpenPendingIntent(context: Context, host: NotificationHost): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_HOST_ID, host.id)
        }
        return PendingIntent.getActivity(
            context,
            host.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createCommandPendingIntent(
        context: Context,
        host: NotificationHost,
        key: RemoteControlKey,
    ): PendingIntent {
        val intent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
            action = ACTION_EXECUTE_COMMAND
            putExtra(EXTRA_HOST_ID, host.id)
            putExtra(EXTRA_REMOTE_CONTROL_KEY, key.name)
        }
        return PendingIntent.getBroadcast(
            context,
            31 * host.id.hashCode() + key.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
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
