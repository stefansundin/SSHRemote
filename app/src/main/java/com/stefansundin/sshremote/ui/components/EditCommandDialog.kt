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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.data.host.RemoteControlKey

@Composable
fun EditCommandDialog(
    command: Triple<RemoteControlKey, String, Boolean>,
    onDismiss: () -> Unit,
    onSave: (RemoteControlKey, String, Boolean) -> Unit,
) {
    val (key, initialCommandValue, initialRepeatValue) = command
    var newCommand by remember { mutableStateOf(initialCommandValue) }
    var repeatCommand by remember { mutableStateOf(initialRepeatValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Command") },
        text = {
            Column {
                TextField(
                    value = newCommand,
                    onValueChange = { newCommand = it },
                    label = { Text(key.title) },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(key, newCommand, repeatCommand)
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
