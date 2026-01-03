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

package com.stefansundin.sshremote.ui.tooling

import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.data.host.SmartVolumeSettings
import com.stefansundin.sshremote.data.host.macosVlcPreset
import com.stefansundin.sshremote.data.host.wtypePreset

object PreviewData {
    val dummyCommand = Command("uptime")

    val sampleHost = Host(
        id = "1",
        name = "Raspberry Pi",
        hostname = "192.168.1.10",
        port = 22,
        user = "pi",
        remoteCommands = wtypePreset,
    )

    val sampleHostWithSmartVolume = sampleHost.copy(
        smartVolume = SmartVolumeSettings(true),
    )

    val sampleHostVlc = sampleHost.copy(
        remoteCommands = macosVlcPreset,
    )

    val sampleHostWithoutSelect = sampleHost.copy(
        remoteCommands = mapOf(
            RemoteControlKey.UP to dummyCommand,
            RemoteControlKey.DOWN to dummyCommand,
            RemoteControlKey.RIGHT to dummyCommand,
            RemoteControlKey.LEFT to dummyCommand,
        ),
    )
}
