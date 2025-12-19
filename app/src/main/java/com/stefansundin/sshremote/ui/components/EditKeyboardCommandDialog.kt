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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.RemoteControlKey

@Composable
fun EditKeyboardCommandDialog(
    typeCommand: Command,
    keyCommand: Command,
    onDismiss: () -> Unit,
    onSave: (typeCommand: Command, keyCommand: Command) -> Unit,
) {
    var newTypeCommand by rememberSaveable { mutableStateOf(typeCommand) }
    var newKeyCommand by rememberSaveable { mutableStateOf(keyCommand) }

    AlertDialog(
        onDismissRequest = onDismiss,
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
        confirmButton = {
            TextButton(onClick = { onSave(newTypeCommand, newKeyCommand) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
