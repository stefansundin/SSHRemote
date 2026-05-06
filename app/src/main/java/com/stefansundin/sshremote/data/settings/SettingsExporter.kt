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
import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.gson.GsonBuilder
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommandRepository
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.HostRepository
import com.stefansundin.sshremote.data.knownhost.KnownHostRepository
import kotlinx.coroutines.flow.first
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

@Keep
data class ExportedCommand(
    val name: String?,
    val command: String,
    val showOutput: Boolean,
    val repeat: Boolean,
) {
    fun toCommand(): Command {
        return Command(
            name = name,
            command = command,
            showOutput = showOutput,
            repeat = repeat,
        )
    }
}

class SettingsExporter(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val hostRepository: HostRepository,
    private val knownHostRepository: KnownHostRepository,
    private val adHocCommandRepository: AdHocCommandRepository,
) {

    suspend fun export(uri: Uri) {
        val settings = getSettingsToExport()
        val json = GsonBuilder().setPrettyPrinting().create().toJson(settings)
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(json.toByteArray())
        }
    }

    private fun compress(data: String): ByteArray {
        val bos = ByteArrayOutputStream(data.length)
        val gzip = GZIPOutputStream(bos)
        gzip.write(data.toByteArray())
        gzip.close()
        return bos.toByteArray()
    }

    suspend fun exportToString(): String {
        val settings = getSettingsToExport()
        val json = GsonBuilder().create().toJson(settings)
        val compressed = compress(json)
        return Base64.encodeToString(compressed, Base64.DEFAULT)
    }

    private fun Color.toHex(): String {
        // This includes the alpha channel which you can't set in the app, but it is possible to edit it in the JSON file and import it back, and it does work.
        // How useful this is however, I don't know, but it's a nice little secret feature.
        return String.format("#%08X", this.toArgb())
    }

    private suspend fun getSettingsToExport(): ExportedSettings {
        val theme = settingsRepository.theme.first()
        val useDynamicColors = settingsRepository.useDynamicColors.first()
        val primaryColor = settingsRepository.primaryColor.first()
        val onPrimaryColor = settingsRepository.onPrimaryColor.first()
        val backgroundColor = settingsRepository.backgroundColor.first()
        val hapticFeedback = settingsRepository.hapticFeedback.first()
        val keepScreenOn = settingsRepository.keepScreenOn.first()
        val notificationsEnabled = settingsRepository.notificationsEnabled.first()
        val strictHostKeyChecking = settingsRepository.strictHostKeyChecking.first()
        val hosts = hostRepository.getAll().first().map { it.toExportedHost() }
        val knownHosts = knownHostRepository.getAll().first().map { knownHost ->
            ExportedKnownHost(
                line = knownHost.line,
                createdAt = knownHost.createdAt.toString(),
            )
        }
        val adHocCommands = adHocCommandRepository.getAll().first().map { command ->
            ExportedAdHocCommand(
                command = command.command,
                lastUsed = command.lastUsed.toString(),
            )
        }
        return ExportedSettings(
            theme,
            useDynamicColors,
            primaryColor?.toHex(),
            onPrimaryColor?.toHex(),
            backgroundColor?.toHex(),
            hapticFeedback.duration,
            keepScreenOn,
            notificationsEnabled,
            strictHostKeyChecking,
            hosts,
            knownHosts,
            adHocCommands,
        )
    }
}
