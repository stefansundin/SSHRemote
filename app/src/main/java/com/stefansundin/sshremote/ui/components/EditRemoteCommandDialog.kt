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
import androidx.compose.material3.MaterialTheme
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
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.data.host.wtypePreset
import com.stefansundin.sshremote.ui.dpadFocusable
import com.stefansundin.sshremote.ui.portraitImePadding
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme

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
    val view = LocalView.current

    AlertDialog(
        title = { Text(stringResource(R.string.edit_command)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    stringResource(R.string.key_title_format, stringResource(key.titleRes)),
                    style = MaterialTheme.typography.titleLarge,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = newCommand,
                        onValueChange = { newCommand = it },
                        label = { Text(stringResource(R.string.command)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .dpadFocusable(),
                    )
                    TextField(
                        value = newLongPressCommand,
                        onValueChange = { newLongPressCommand = it },
                        label = { Text(stringResource(R.string.long_press_command)) },
                        enabled = !repeatCommand,
                        modifier = Modifier
                            .fillMaxWidth()
                            .dpadFocusable(),
                    )
                    RowWithCheckbox(
                        checked = repeatCommand,
                        text = stringResource(R.string.repeat_command_while_pressed),
                        onCheckedChange = { repeatCommand = it },
                    )
                }
            }
        },
        properties = DialogProperties(dismissOnClickOutside = false, decorFitsSystemWindows = false),
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
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

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 600,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun EditRemoteCommandDialogPreview() {
    val key = RemoteControlKey.SELECT

    SSHRemoteTheme {
        Surface {
            EditRemoteCommandDialog(
                command = Pair(key, wtypePreset[key]!!),
                onDismiss = {},
                onSave = { _, _ -> },
            )
        }
    }
}
