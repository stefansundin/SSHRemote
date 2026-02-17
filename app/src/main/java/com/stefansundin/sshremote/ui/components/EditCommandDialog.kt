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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.CommandItem
import com.stefansundin.sshremote.data.host.toItem
import com.stefansundin.sshremote.ui.dpadFocusable
import com.stefansundin.sshremote.ui.portraitImePadding
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import java.util.UUID

@Composable
fun EditCommandDialog(
    commandItem: CommandItem?,
    onDismiss: () -> Unit,
    onSave: (CommandItem) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(commandItem?.command?.name ?: "") }
    var commandText by rememberSaveable { mutableStateOf(commandItem?.command?.command ?: "") }
    var showOutput by rememberSaveable { mutableStateOf(commandItem?.command?.showOutput ?: true) }
    val view = LocalView.current

    AlertDialog(
        title = { Text(if (commandItem == null) "Add Command" else "Edit Command") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .dpadFocusable(),
                )
                TextField(
                    value = commandText,
                    onValueChange = { commandText = it },
                    label = { Text("Command") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .dpadFocusable(),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = showOutput,
                            onValueChange = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                showOutput = it
                            },
                            role = Role.Checkbox,
                        ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = showOutput,
                        onCheckedChange = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            showOutput = it
                        },
                    )
                    Text("Show output")
                }
            }
        },
        properties = DialogProperties(dismissOnClickOutside = false, decorFitsSystemWindows = false),
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onSave(
                        CommandItem(
                            key = commandItem?.key ?: UUID.randomUUID().toString(),
                            Command(
                                command = commandText,
                                name = name.ifBlank { null },
                                showOutput = showOutput,
                            ),
                        ),
                    )
                },
                enabled = commandText.isNotBlank(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onDismiss()
                },
            ) {
                Text("Cancel")
            }
        },
        modifier = Modifier.portraitImePadding(),
    )
}

@Preview(showBackground = true, name = "Add command")
@Preview(
    showBackground = true,
    name = "Add command (dark and large font)",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun EditCommandDialogPreview_Add() {
    SSHRemoteTheme {
        Surface {
            EditCommandDialog(
                commandItem = null,
                onDismiss = {},
                onSave = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "Edit command")
@Preview(
    showBackground = true,
    name = "Edit command (dark and large font)",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun EditCommandDialogPreview_Edit() {
    SSHRemoteTheme {
        Surface {
            EditCommandDialog(
                commandItem = Command("uptime", name = "Uptime").toItem(),
                onDismiss = {},
                onSave = {},
            )
        }
    }
}
