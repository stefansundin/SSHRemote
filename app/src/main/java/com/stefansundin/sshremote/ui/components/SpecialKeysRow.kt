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

package com.stefansundin.sshremote.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.ConnectionStatus
import com.stefansundin.sshremote.data.host.RemoteControlKey

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpecialKeysRow(
    onKey: (String) -> Unit,
    modifier: Modifier = Modifier,
    commands: Map<RemoteControlKey, Command>? = null,
    connectionStatus: ConnectionStatus? = null,
) {
    val isEnabled = (connectionStatus == null || connectionStatus == ConnectionStatus.CONNECTED)
            && (commands == null || !commands[RemoteControlKey.KEYBOARD_KEY_INPUT]?.command.isNullOrEmpty())
    val specialKeys = listOf(
        "Esc" to "Escape",
        "Tab" to "Tab",
        "Caps" to "Caps_Lock",
        "Shift" to "Shift_L",
        "Ctrl" to "Control_L",
        "Super" to "Super_L",
        "Alt" to "Alt_L",
    )

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        specialKeys.forEach { (label, key) ->
            Button(
                onClick = { onKey(key) },
                enabled = isEnabled,
            ) {
                Text(label)
            }
        }
    }
}
