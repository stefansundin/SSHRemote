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
import android.util.Log
import com.google.gson.JsonSyntaxException
import com.stefansundin.sshremote.HapticFeedback
import com.stefansundin.sshremote.Theme
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommand
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommandRepository
import com.stefansundin.sshremote.data.gson
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.HostRepository
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.data.host.RemoteControlScreen
import kotlinx.coroutines.flow.first
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

class ImportException(message: String) : Exception(message)

class SettingsImporter(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val hostRepository: HostRepository,
    private val adHocCommandRepository: AdHocCommandRepository,
) {

    suspend fun import(uri: Uri, merge: Boolean): Triple<Int, Boolean, Theme?> {
        val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().use { it.readText() }
        } ?: throw ImportException("Could not read file")

        var requestNotificationPermission = false
        var importedTheme: Theme? = null

        try {
            val settings: ExportedSettings = gson.fromJson(json, ExportedSettings::class.java)
                ?: throw ImportException("Not a valid JSON file")

            if (settings.hosts == null) {
                throw ImportException("Invalid file format")
            }

            if (settings.theme != null) {
                settingsRepository.setTheme(settings.theme)
                importedTheme = settings.theme
            }
            if (settings.hapticFeedbackDuration != null) {
                settingsRepository.setHapticFeedback(HapticFeedback.fromDuration(settings.hapticFeedbackDuration))
            }
            if (settings.keepScreenOn != null) {
                settingsRepository.setKeepScreenOn(settings.keepScreenOn)
            }
            if (settings.notificationsEnabled != null) {
                settingsRepository.setNotificationsEnabled(settings.notificationsEnabled)
                if (settings.notificationsEnabled) {
                    requestNotificationPermission = true
                }
            }
            if (settings.strictHostKeyChecking != null) {
                settingsRepository.setStrictHostKeyChecking(settings.strictHostKeyChecking)
            }

            if (!merge) {
                hostRepository.deleteAll()
                adHocCommandRepository.clear()
            }

            @Suppress("UNCHECKED_CAST")
            settings.hosts.forEach { exportedHost ->
                val host = Host(
                    name = exportedHost.name,
                    hostname = exportedHost.hostname,
                    port = exportedHost.port,
                    user = exportedHost.user,
                    passwordId = null,
                    identityIds = if (exportedHost.allowIdentities) null else emptyList(),
                    knownHosts = exportedHost.knownHosts,
                    commands = exportedHost.commands.map {
                        Command(
                            command = it.command,
                            name = it.name,
                            showOutput = it.showOutput,
                            repeat = it.repeat,
                        )
                    },
                    remoteCommands = (exportedHost.remoteCommands?.filterKeys { it != null } as Map<RemoteControlKey, ExportedCommand>?)?.mapValues {
                        Command(
                            command = it.value.command,
                            name = it.value.name,
                            showOutput = it.value.showOutput,
                            repeat = it.value.repeat,
                        )
                    },
                    startScreen = exportedHost.startScreen ?: RemoteControlScreen.Default,
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

            return Triple(settings.hosts.size, requestNotificationPermission, importedTheme)
        } catch (e: ImportException) {
            throw e
        } catch (_: JsonSyntaxException) {
            throw ImportException("Invalid file format")
        } catch (e: Exception) {
            Log.e("SettingsImporter", "Error importing settings", e)
            throw ImportException("Something went wrong")
        }
    }
}
