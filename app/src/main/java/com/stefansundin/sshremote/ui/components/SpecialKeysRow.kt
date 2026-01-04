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

package com.stefansundin.sshremote.ui.components

import android.view.SoundEffectConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.data.host.ConnectionStatus
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.RemoteControlKey

@Composable
fun SpecialKeysRow(
    onKey: (String) -> Unit,
    modifier: Modifier = Modifier,
    host: Host? = null,
    connectionStatus: ConnectionStatus? = null,
) {
    val keyboardConfigured = host == null ||
            (host.remoteCommands != null && !host.remoteCommands[RemoteControlKey.KEYBOARD_KEY_INPUT]?.command.isNullOrEmpty())
    val isEnabled = connectionStatus == ConnectionStatus.CONNECTED && keyboardConfigured
    val specialKeys = listOf(
        "Esc" to "Escape",
        "Tab" to "Tab",
        "Caps" to "Caps_Lock",
        "Shift" to "Shift_L",
        "Ctrl" to "Control_L",
        "Super" to "Super_L",
        "Alt" to "Alt_L",
    )
    val view = LocalView.current

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        specialKeys.forEach { (label, key) ->
            Button(
                enabled = isEnabled,
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onKey(key)
                },
            ) {
                Text(label)
            }
        }
    }
}
