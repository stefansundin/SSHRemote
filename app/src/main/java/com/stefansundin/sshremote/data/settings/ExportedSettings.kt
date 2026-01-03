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

package com.stefansundin.sshremote.data.settings

import androidx.annotation.Keep
import com.google.gson.GsonBuilder
import com.stefansundin.sshremote.Theme
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.data.host.RemoteControlScreen
import com.stefansundin.sshremote.data.host.SmartVolumeSettings

@Keep
data class ExportedHost(
    val id: String?,
    val name: String?,
    val hostname: String?,
    val port: Int?,
    val user: String?,
    val allowIdentities: Boolean?,
    val knownHosts: List<String>?,
    val commands: List<ExportedCommand>?,
    val remoteCommands: Map<RemoteControlKey?, ExportedCommand>?,
    val startScreen: RemoteControlScreen?,
    val smartVolume: SmartVolumeSettings?,
    val sshConfig: String?,
) {
    fun exportToString(): String {
        return GsonBuilder().create().toJson(this)
    }
}

@Keep
data class ExportedAdHocCommand(
    val command: String,
    val lastUsed: String,
)

@Keep
data class ExportedSettings(
    val theme: Theme?,
    val hapticFeedbackDuration: Long?,
    val keepScreenOn: Boolean?,
    val notificationsEnabled: Boolean?,
    val strictHostKeyChecking: Boolean?,
    val hosts: List<ExportedHost>?,
    val adHocCommands: List<ExportedAdHocCommand>?,
)
