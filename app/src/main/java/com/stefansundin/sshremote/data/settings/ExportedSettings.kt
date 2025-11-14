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

import com.stefansundin.sshremote.Theme
import com.stefansundin.sshremote.data.host.Command

data class ExportedHost(
    val name: String,
    val hostname: String,
    val port: Int,
    val user: String,
    val allowIdentities: Boolean,
    val knownHosts: List<String>,
    val commands: List<Command>,
)

data class ExportedAdHocCommand(
    val command: String,
    val lastUsed: String,
)

data class ExportedSettings(
    val theme: Theme?,
    val strictHostKeyChecking: Boolean?,
    val hosts: List<ExportedHost>?,
    val adHocCommands: List<ExportedAdHocCommand>?,
)
