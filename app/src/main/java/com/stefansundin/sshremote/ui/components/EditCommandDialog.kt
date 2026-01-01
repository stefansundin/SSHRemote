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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
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

@Composable
fun EditCommandDialog(
    command: Command?,
    onDismiss: () -> Unit,
    onSave: (Command) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(command?.name ?: "") }
    var commandText by rememberSaveable { mutableStateOf(command?.command ?: "") }
    var showOutput by rememberSaveable { mutableStateOf(command?.showOutput ?: true) }

    AlertDialog(
        title = { Text(if (command == null) "Add Command" else "Edit Command") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = commandText,
                    onValueChange = { commandText = it },
                    label = { Text("Command") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = showOutput,
                            onValueChange = { showOutput = it },
                            role = Role.Checkbox,
                        ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = showOutput,
                        onCheckedChange = { showOutput = it },
                    )
                    Text("Show output")
                }
            }
        },
        properties = DialogProperties(dismissOnClickOutside = false),
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Command(
                            command = commandText,
                            name = name.ifBlank { null },
                            showOutput = showOutput,
                        ),
                    )
                },
                enabled = commandText.isNotBlank(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
