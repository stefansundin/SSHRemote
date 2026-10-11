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

package com.stefansundin.sshremote.data.host

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stefansundin.sshremote.data.settings.ExportedCommand
import com.stefansundin.sshremote.data.settings.ExportedHost
import java.util.UUID

enum class RemoteControlScreen(val tabIndex: Int) {
    REMOTE(0),
    MOUSE(1),
    KEYBOARD(2),
    COMMANDS(3);

    companion object {
        val Default = REMOTE
    }
}

/**
 * Captures network service discovery metadata.
 */
@Keep
data class ServiceInfo(
    val serviceName: String,
    val serviceType: String,
    val hostAddresses: List<String> = emptyList(),
    val port: Int? = null,
    val attributes: Map<String, String> = emptyMap(),
    val networkHandle: Long? = null,
)

/**
 * A host is a network device that can be accessed via SSH.
 *
 * A host is also known as an SSH server, but in this app the term host is used consistently, including in the UI.
 */
@Entity(tableName = "hosts")
data class Host(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val hostname: String,
    val port: Int,
    val user: String,
    val passwordId: String? = null,
    val identityIds: List<String>? = null,
    val knownHosts: List<String> = emptyList(),
    val commands: List<Command> = DEFAULT_COMMANDS,
    val remoteCommands: Map<RemoteControlKey, Command>? = null,
    val shareInBackground: Boolean = false,
    val startScreen: RemoteControlScreen = RemoteControlScreen.Default,
    val smartVolume: SmartVolumeSettings? = null,
    val sshConfig: String? = null,
    val serviceInfo: ServiceInfo? = null,
) {
    companion object {
        val DEFAULT_COMMANDS = listOf(Command("uptime", name = "Uptime", showOutput = true))
        const val DEFAULT_SSH_CONFIG = "ServerAliveInterval 60\n"
    }

    fun resolveShareCommandTemplate(): Command? {
        return remoteCommands?.get(RemoteControlKey.SHARE_TEXT)
            ?: remoteCommands?.get(RemoteControlKey.KEYBOARD_TYPE_INPUT)
    }

    fun toExportedHost(): ExportedHost {
        return ExportedHost(
            id = id,
            name = name,
            hostname = hostname,
            port = port,
            user = user,
            allowIdentities = identityIds?.isNotEmpty() ?: true,
            knownHosts = knownHosts,
            shareInBackground = shareInBackground,
            commands = commands.map {
                ExportedCommand(
                    id = it.id,
                    name = it.name,
                    command = it.command,
                    showOutput = it.showOutput,
                    renderOutputAsMarkdown = it.renderOutputAsMarkdown,
                )
            },
            remoteCommands = remoteCommands?.mapValues {
                val command = it.value.normalized()
                ExportedCommand(
                    name = command.name,
                    command = command.command,
                    longPressCommand = command.longPressCommand,
                    renderOutputAsMarkdown = command.renderOutputAsMarkdown,
                    repeat = command.repeat,
                    downCommand = command.downCommand,
                    upCommand = command.upCommand,
                    physicalKeyCodes = command.physicalKeyCodes,
                )
            },
            startScreen = startScreen,
            smartVolume = smartVolume,
            sshConfig = sshConfig,
            serviceInfo = serviceInfo,
        )
    }
}
