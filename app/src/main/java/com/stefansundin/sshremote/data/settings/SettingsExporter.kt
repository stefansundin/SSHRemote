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

package com.stefansundin.sshremote.data.settings

import android.content.Context
import android.net.Uri
import com.google.gson.GsonBuilder
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommandRepository
import com.stefansundin.sshremote.data.host.HostRepository
import kotlinx.coroutines.flow.first

data class ExportedCommand(
    val name: String?,
    val command: String,
    val showOutput: Boolean,
    val repeat: Boolean,
)

class SettingsExporter(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val hostRepository: HostRepository,
    private val adHocCommandRepository: AdHocCommandRepository,
) {

    suspend fun export(uri: Uri) {
        val settings = getSettingsToExport()
        val json = GsonBuilder().setPrettyPrinting().create().toJson(settings)

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(json.toByteArray())
        }
    }

    private suspend fun getSettingsToExport(): ExportedSettings {
        val theme = settingsRepository.theme.first()
        val hapticFeedback = settingsRepository.hapticFeedback.first()
        val keepScreenOn = settingsRepository.keepScreenOn.first()
        val notificationsEnabled = settingsRepository.notificationsEnabled.first()
        val strictHostKeyChecking = settingsRepository.strictHostKeyChecking.first()
        val hosts = hostRepository.getAll().first().map { host ->
            ExportedHost(
                name = host.name,
                hostname = host.hostname,
                port = host.port,
                user = host.user,
                allowIdentities = host.identityIds?.isNotEmpty() ?: true,
                knownHosts = host.knownHosts,
                commands = host.commands.map {
                    ExportedCommand(
                        name = it.name,
                        command = it.command,
                        showOutput = it.showOutput,
                        repeat = it.repeat,
                    )
                },
                remoteCommands = host.remoteCommands.mapValues {
                    ExportedCommand(
                        name = it.value.name,
                        command = it.value.command,
                        showOutput = it.value.showOutput,
                        repeat = it.value.repeat,
                    )
                },
                startScreen = host.startScreen,
            )
        }
        val adHocCommands = adHocCommandRepository.getAdHocCommands().first().map { command ->
            ExportedAdHocCommand(
                command = command.command,
                lastUsed = command.lastUsed.toString(),
            )
        }
        return ExportedSettings(
            theme,
            hapticFeedback.duration,
            keepScreenOn,
            notificationsEnabled,
            strictHostKeyChecking,
            hosts,
            adHocCommands,
        )
    }
}
