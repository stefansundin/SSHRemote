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
import android.view.SoundEffectConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.data.host.wtypePreset
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme

@Composable
fun EditKeyboardCommandDialog(
    typeCommand: Command,
    keyCommand: Command,
    onDismiss: () -> Unit,
    onSave: (typeCommand: Command, keyCommand: Command) -> Unit,
) {
    var newTypeCommand by rememberSaveable { mutableStateOf(typeCommand) }
    var newKeyCommand by rememberSaveable { mutableStateOf(keyCommand) }
    val view = LocalView.current

    AlertDialog(
        title = { Text("Edit Keyboard Commands") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextField(
                    value = newTypeCommand.command,
                    onValueChange = { value ->
                        newTypeCommand = newTypeCommand.copy(command = value)
                    },
                    label = { Text(RemoteControlKey.KEYBOARD_TYPE_INPUT.title) },
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = newKeyCommand.command,
                    onValueChange = { value ->
                        newKeyCommand = newKeyCommand.copy(command = value)
                    },
                    label = { Text(RemoteControlKey.KEYBOARD_KEY_INPUT.title) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        properties = DialogProperties(dismissOnClickOutside = false),
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onSave(newTypeCommand, newKeyCommand)
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onDismiss()
                },
            ) {
                Text("Cancel")
            }
        },
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun EditKeyboardCommandDialogPreview() {
    val remoteCommands = wtypePreset

    SSHRemoteTheme {
        Surface {
            EditKeyboardCommandDialog(
                typeCommand = remoteCommands[RemoteControlKey.KEYBOARD_TYPE_INPUT]!!,
                keyCommand = remoteCommands[RemoteControlKey.KEYBOARD_KEY_INPUT]!!,
                onDismiss = {},
                onSave = { _, _ -> },
            )
        }
    }
}
