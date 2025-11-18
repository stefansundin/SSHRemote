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
import com.google.gson.Gson
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommandRepository
import com.stefansundin.sshremote.data.host.HostRepository
import kotlinx.coroutines.flow.first

class SettingsExporter(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val hostRepository: HostRepository,
    private val adHocCommandRepository: AdHocCommandRepository,
) {

    suspend fun export(uri: Uri) {
        val settings = getSettingsToExport()
        val json = Gson().toJson(settings)

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(json.toByteArray())
        }
    }

    private suspend fun getSettingsToExport(): ExportedSettings {
        val theme = settingsRepository.theme.first()
        val strictHostKeyChecking = settingsRepository.strictHostKeyChecking.first()
        val hosts = hostRepository.getAll().first().map { host ->
            ExportedHost(
                name = host.name,
                hostname = host.hostname,
                port = host.port,
                user = host.user,
                allowIdentities = host.identityIds?.isNotEmpty() ?: true,
                knownHosts = host.knownHosts,
                commands = host.commands,
                remoteCommands = host.remoteCommands,
                startScreen = host.startScreen,
            )
        }
        val adHocCommands = adHocCommandRepository.getAdHocCommands().first().map { command ->
            ExportedAdHocCommand(
                command = command.command,
                lastUsed = command.lastUsed.toString(),
            )
        }
        return ExportedSettings(theme, strictHostKeyChecking, hosts, adHocCommands)
    }
}
