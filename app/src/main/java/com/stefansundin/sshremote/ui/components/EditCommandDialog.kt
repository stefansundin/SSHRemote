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
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.ui.dpadFocusable
import com.stefansundin.sshremote.ui.portraitImePadding
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import java.util.UUID

@Composable
fun EditCommandDialog(
    command: Command?,
    onDismiss: () -> Unit,
    onSave: (Command) -> Unit,
    onAddToHomeScreen: (String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(command?.name ?: "") }
    var commandText by rememberSaveable { mutableStateOf(command?.command ?: "") }
    var showOutput by rememberSaveable { mutableStateOf(command?.showOutput ?: true) }
    var renderOutputAsMarkdown by rememberSaveable { mutableStateOf(command?.renderOutputAsMarkdown ?: false) }
    var runInBackground by rememberSaveable { mutableStateOf(command?.runInBackground ?: false) }
    val view = LocalView.current

    fun buildCommand() = Command(
        id = command?.id ?: UUID.randomUUID().toString(),
        name = name.ifBlank { null },
        command = commandText,
        showOutput = showOutput,
        renderOutputAsMarkdown = renderOutputAsMarkdown,
        runInBackground = runInBackground,
    )

    AlertDialog(
        title = { Text(stringResource(if (command == null) R.string.add_command else R.string.edit_command)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name_optional)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .dpadFocusable(),
                )
                TextField(
                    value = commandText,
                    onValueChange = { commandText = it },
                    label = { Text(stringResource(R.string.command)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .dpadFocusable(),
                )
                RowWithCheckbox(
                    checked = showOutput,
                    text = stringResource(R.string.show_output),
                    onCheckedChange = {
                        showOutput = it
                        if (!it) {
                            renderOutputAsMarkdown = false
                        }
                    },
                )
                RowWithCheckbox(
                    checked = renderOutputAsMarkdown,
                    text = stringResource(R.string.render_output_as_markdown),
                    enabled = showOutput,
                    onCheckedChange = { renderOutputAsMarkdown = it },
                )
                RowWithCheckbox(
                    checked = runInBackground,
                    text = stringResource(R.string.run_command_in_background),
                    onCheckedChange = { checked ->
                        runInBackground = checked
                    },
                )
                OutlinedButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        val updatedCommand = buildCommand()
                        onSave(updatedCommand)
                        onAddToHomeScreen(updatedCommand.id)
                        onDismiss()
                    },
                    enabled = commandText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.add_to_home_screen))
                }
            }
        },
        properties = DialogProperties(dismissOnClickOutside = false, decorFitsSystemWindows = false),
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onSave(buildCommand())
                    onDismiss()
                },
                enabled = commandText.isNotBlank(),
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        modifier = Modifier.portraitImePadding(),
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600, name = "Add command")
@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 600,
    name = "Add command (dark and large font)",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun EditCommandDialogPreview_Add() {
    SSHRemoteTheme {
        Surface {
            EditCommandDialog(
                command = null,
                onDismiss = {},
                onSave = {},
                onAddToHomeScreen = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600, name = "Edit command")
@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 600,
    name = "Edit command (dark and large font)",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun EditCommandDialogPreview_Edit() {
    SSHRemoteTheme {
        Surface {
            EditCommandDialog(
                command = Command("uptime", name = "Uptime"),
                onDismiss = {},
                onSave = {},
                onAddToHomeScreen = {},
            )
        }
    }
}
