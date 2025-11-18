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

package com.stefansundin.sshremote.data.host

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class StartScreen {
    COMMAND_LIST,
    REMOTE_CONTROL
}

/**
 * A host is an network device that can be accessed via SSH.
 *
 * A host is also known as an SSH server, but in this app the term host is used consistently, including in the UI.
 */
@Entity(tableName = "hosts")
data class Host(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val hostname: String,
    val port: Int,
    val user: String,
    val encryptedPassword: String?,
    val identityIds: List<Int>? = null,
    val knownHosts: List<String> = emptyList(),
    val commands: List<Command> = listOf(Command("Uptime", "uptime", true)),
    val remoteCommands: Map<RemoteControlKey, String> = emptyMap(),
    val startScreen: StartScreen = StartScreen.COMMAND_LIST,
)
