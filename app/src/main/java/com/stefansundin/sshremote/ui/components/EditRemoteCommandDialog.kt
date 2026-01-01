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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.RemoteControlKey

@Composable
fun EditRemoteCommandDialog(
    command: Pair<RemoteControlKey, Command>,
    onDismiss: () -> Unit,
    onSave: (RemoteControlKey, Command) -> Unit,
) {
    val (key, initialCommand) = command
    var newCommand by rememberSaveable { mutableStateOf(initialCommand.command) }
    var newLongPressCommand by rememberSaveable { mutableStateOf(initialCommand.longPressCommand ?: "") }
    var repeatCommand by rememberSaveable { mutableStateOf(initialCommand.repeat) }

    AlertDialog(
        title = { Text("Edit Command") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(key.title, style = MaterialTheme.typography.titleLarge)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = newCommand,
                        onValueChange = { newCommand = it },
                        label = { Text("Command") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextField(
                        value = newLongPressCommand,
                        onValueChange = { newLongPressCommand = it },
                        label = { Text("Long press command") },
                        enabled = !repeatCommand,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = repeatCommand,
                                onValueChange = { repeatCommand = it },
                                role = Role.Checkbox,
                            ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = repeatCommand,
                            onCheckedChange = { repeatCommand = it },
                        )
                        Text("Repeat command while button is pressed")
                    }
                }
            }
        },
        properties = DialogProperties(dismissOnClickOutside = false),
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        key,
                        initialCommand.copy(
                            command = newCommand,
                            longPressCommand = newLongPressCommand.takeIf { it.isNotBlank() },
                            repeat = repeatCommand,
                        ),
                    )
                },
            ) {
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
