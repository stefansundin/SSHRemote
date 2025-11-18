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
import com.google.gson.JsonSyntaxException
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommand
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommandRepository
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.HostRepository
import com.stefansundin.sshremote.data.host.StartScreen
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

class ImportException(message: String) : Exception(message)

class SettingsImporter(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val hostRepository: HostRepository,
    private val adHocCommandRepository: AdHocCommandRepository,
) {

    suspend fun import(uri: Uri, merge: Boolean): Int {
        val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().use { it.readText() }
        } ?: throw ImportException("Could not read file")

        try {
            val settings: ExportedSettings = Gson().fromJson(json, ExportedSettings::class.java)
                ?: throw ImportException("Not a valid JSON file")

            if (settings.hosts == null) {
                throw ImportException("Invalid file format")
            }

            if (settings.theme != null) {
                settingsRepository.setTheme(settings.theme)
            }
            if (settings.strictHostKeyChecking != null) {
                settingsRepository.setStrictHostKeyChecking(settings.strictHostKeyChecking)
            }

            if (!merge) {
                hostRepository.deleteAll()
                adHocCommandRepository.clear()
            }

            settings.hosts.forEach { exportedHost ->
                val host = Host(
                    name = exportedHost.name,
                    hostname = exportedHost.hostname,
                    port = exportedHost.port,
                    user = exportedHost.user,
                    encryptedPassword = null,
                    identityIds = if (exportedHost.allowIdentities) null else emptyList(),
                    knownHosts = exportedHost.knownHosts,
                    commands = exportedHost.commands,
                    remoteCommands = exportedHost.remoteCommands ?: emptyMap(),
                    startScreen = exportedHost.startScreen ?: StartScreen.COMMAND_LIST,
                )
                hostRepository.upsert(host)
            }

            settings.adHocCommands?.forEach { exportedAdHocCommand ->
                try {
                    val adHocCommand = AdHocCommand(
                        command = exportedAdHocCommand.command,
                        lastUsed = OffsetDateTime.parse(exportedAdHocCommand.lastUsed),
                    )
                    adHocCommandRepository.insert(adHocCommand)
                } catch (_: DateTimeParseException) {
                    // Ignore commands with invalid date format
                }
            }

            return settings.hosts.size
        } catch (e: ImportException) {
            throw e
        } catch (_: JsonSyntaxException) {
            throw ImportException("Invalid file format")
        } catch (_: Exception) {
            throw ImportException("Something went wrong")
        }
    }
}
