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

import android.content.res.Configuration
import android.view.KeyEvent
import android.view.SoundEffectConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.data.host.ConnectionStatus
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme

@Composable
fun SpecialKeysRow(
    onKey: (Int) -> Unit,
    onKeyDown: (Int) -> Unit,
    onKeyUp: (Int) -> Unit,
    pressedKeys: Set<Int>,
    modifier: Modifier = Modifier,
    host: Host? = null,
    connectionStatus: ConnectionStatus? = null,
) {
    val keyboardConfigured = host == null ||
            (host.remoteCommands != null && !host.remoteCommands[RemoteControlKey.KEYBOARD_KEY_INPUT]?.command.isNullOrEmpty())
    val isEnabled = connectionStatus == ConnectionStatus.CONNECTED && keyboardConfigured
    val specialKeys = listOf(
        "Esc" to KeyEvent.KEYCODE_ESCAPE,
        "Tab" to KeyEvent.KEYCODE_TAB,
        "Caps" to KeyEvent.KEYCODE_CAPS_LOCK,
        "Shift" to KeyEvent.KEYCODE_SHIFT_LEFT,
        "Ctrl" to KeyEvent.KEYCODE_CTRL_LEFT,
        "Super" to KeyEvent.KEYCODE_META_LEFT,
        "Alt" to KeyEvent.KEYCODE_ALT_LEFT,
    )
    val modifiers = setOf(
        KeyEvent.KEYCODE_SHIFT_LEFT,
        KeyEvent.KEYCODE_CTRL_LEFT,
        KeyEvent.KEYCODE_META_LEFT,
        KeyEvent.KEYCODE_ALT_LEFT,
    )
    val view = LocalView.current

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        specialKeys.forEach { (label, key) ->
            val isModifier = modifiers.contains(key)
            val isPressed = pressedKeys.contains(key)

            Button(
                enabled = isEnabled,
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    if (isModifier) {
                        if (isPressed) {
                            onKeyUp(key)
                        } else {
                            onKeyDown(key)
                        }
                    } else {
                        onKey(key)
                    }
                },
                colors = if (isPressed) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                },
            ) {
                Text(label)
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun SpecialKeysRowPreview() {
    SSHRemoteTheme {
        Surface {
            SpecialKeysRow(
                onKey = {},
                onKeyDown = {},
                onKeyUp = {},
                pressedKeys = emptySet(),
                connectionStatus = ConnectionStatus.CONNECTED,
            )
        }
    }
}
