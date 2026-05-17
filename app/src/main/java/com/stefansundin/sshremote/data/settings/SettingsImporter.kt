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

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.google.gson.JsonSyntaxException
import com.stefansundin.sshremote.HapticFeedback
import com.stefansundin.sshremote.Theme
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommand
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommandRepository
import com.stefansundin.sshremote.data.gson
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.HostRepository
import com.stefansundin.sshremote.data.knownhost.KnownHost
import com.stefansundin.sshremote.data.knownhost.KnownHostRepository
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.data.host.RemoteControlScreen
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.UUID
import java.util.zip.GZIPInputStream
import java.util.zip.ZipException

enum class ImportStrategy {
    Upsert,
    Duplicate,
    Replace,
}

class ImportException(message: String) : Exception(message)

class SettingsImporter(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val hostRepository: HostRepository,
    private val knownHostRepository: KnownHostRepository,
    private val adHocCommandRepository: AdHocCommandRepository,
) {

    suspend fun import(uri: Uri, importStrategy: ImportStrategy): Triple<Int, Boolean, Theme?> {
        val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().use { it.readText() }
        } ?: throw ImportException("Could not read file")
        return import(json, importStrategy)
    }

    private fun decompress(data: String): String {
        return try {
            val compressed = Base64.decode(data, Base64.DEFAULT)
            val bis = ByteArrayInputStream(compressed)
            val gis = GZIPInputStream(bis)
            val buffer = ByteArray(1024)
            val out = ByteArrayOutputStream()
            var len: Int
            while (gis.read(buffer).also { len = it } != -1) {
                out.write(buffer, 0, len)
            }
            out.toString("UTF-8")
        } catch (e: Exception) {
            when (e) {
                is ZipException, is IllegalArgumentException -> {
                    // Not compressed or not Base64, assume it's the raw JSON
                    data
                }

                else -> throw e
            }
        }
    }

    private fun parseColor(hex: String?): Color? {
        if (hex == null) return null
        return try {
            Color(hex.toColorInt())
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    suspend fun import(json: String, importStrategy: ImportStrategy): Triple<Int, Boolean, Theme?> {
        var requestNotificationPermission = false
        var importedTheme: Theme? = null

        try {
            val decompressedJson = decompress(json)
            val settings: ExportedSettings = gson.fromJson(decompressedJson, ExportedSettings::class.java)
                ?: throw ImportException("Not a valid JSON file")

            if (settings.hosts == null) {
                throw ImportException("Invalid file format")
            }

            if (settings.theme != null) {
                settingsRepository.setTheme(settings.theme)
                importedTheme = settings.theme
            }
            if (settings.useDynamicColors != null) {
                settingsRepository.setUseDynamicColors(settings.useDynamicColors)
            }
            settingsRepository.setBackgroundColor(parseColor(settings.backgroundColor))
            settingsRepository.setPrimaryColor(parseColor(settings.primaryColor))
            settingsRepository.setOnPrimaryColor(parseColor(settings.onPrimaryColor))

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
            if (settings.allowPasswordPrompting != null) {
                settingsRepository.setAllowPasswordPrompting(settings.allowPasswordPrompting)
            }
            if (settings.shareTargetEnabled != null) {
                settingsRepository.setShareTargetEnabled(settings.shareTargetEnabled)
            }

            if (importStrategy == ImportStrategy.Replace) {
                hostRepository.deleteAll()
                knownHostRepository.deleteAll()
                adHocCommandRepository.deleteAll()
            }

            settings.hosts.forEach { exportedHost ->
                val id =
                    if (importStrategy == ImportStrategy.Duplicate || exportedHost.id == null) UUID.randomUUID()
                        .toString()
                    else exportedHost.id

                @Suppress("UNCHECKED_CAST")
                val host = Host(
                    id = id,
                    name = exportedHost.name!!,
                    hostname = exportedHost.hostname!!,
                    port = exportedHost.port ?: 22,
                    user = exportedHost.user!!,
                    passwordId = null,
                    identityIds = if (exportedHost.allowIdentities == true) null else emptyList(),
                    knownHosts = exportedHost.knownHosts ?: emptyList(),
                    shareInBackground = exportedHost.shareInBackground ?: false,
                    commands = exportedHost.commands?.map { it.toCommand() } ?: emptyList(),
                    remoteCommands = (exportedHost.remoteCommands?.filterKeys { it != null } as Map<RemoteControlKey, ExportedCommand>?)?.mapValues { it.value.toCommand() }
                        ?: emptyMap(),
                    startScreen = exportedHost.startScreen ?: RemoteControlScreen.Default,
                    sshConfig = exportedHost.sshConfig,
                )
                hostRepository.upsert(host)
            }

            settings.knownHosts?.forEach { exportedKnownHost ->
                try {
                    val knownHost = KnownHost(
                        line = exportedKnownHost.line,
                        createdAt = OffsetDateTime.parse(exportedKnownHost.createdAt),
                    )
                    knownHostRepository.insert(knownHost)
                } catch (_: DateTimeParseException) {
                    // Ignore data with invalid date format
                }
            }

            settings.adHocCommands?.forEach { exportedAdHocCommand ->
                try {
                    val adHocCommand = AdHocCommand(
                        command = exportedAdHocCommand.command,
                        lastUsed = OffsetDateTime.parse(exportedAdHocCommand.lastUsed),
                    )
                    adHocCommandRepository.insert(adHocCommand)
                } catch (_: DateTimeParseException) {
                    // Ignore data with invalid date format
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
