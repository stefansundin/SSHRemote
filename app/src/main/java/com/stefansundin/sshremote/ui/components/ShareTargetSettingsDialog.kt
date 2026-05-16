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
fun ShareTargetSettingsDialog(
    initialRemoteCommands: Map<RemoteControlKey, Command>,
    onDismiss: () -> Unit,
    onSave: (Map<RemoteControlKey, Command>) -> Unit,
) {
    val view = LocalView.current
    val keyboardTypeTemplate = initialRemoteCommands[RemoteControlKey.KEYBOARD_TYPE_INPUT]
    val keyboardTypeCommand = keyboardTypeTemplate?.command.orEmpty()
    val initialShareTargetTemplate = initialRemoteCommands[RemoteControlKey.SHARE_TEXT]
    val initialShareTargetCommand = initialShareTargetTemplate?.command
    val initialRunInBackground = initialShareTargetTemplate?.runInBackground ?: keyboardTypeTemplate?.runInBackground ?: false

    var useKeyboardTypeCommand by rememberSaveable { mutableStateOf(initialShareTargetCommand == null) }
    var customShareTargetCommand by rememberSaveable {
        mutableStateOf(initialShareTargetCommand ?: keyboardTypeCommand)
    }
    var customShareTargetShowOutput by rememberSaveable {
        mutableStateOf(initialShareTargetTemplate?.showOutput ?: false)
    }
    var runInBackground by rememberSaveable { mutableStateOf(initialRunInBackground) }

    AlertDialog(
        title = { Text(stringResource(R.string.share_target_settings)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RowWithCheckbox(
                    checked = useKeyboardTypeCommand,
                    text = stringResource(R.string.use_keyboard_type_command),
                    onCheckedChange = { checked ->
                        useKeyboardTypeCommand = checked
                    },
                )
                TextField(
                    label = { Text(stringResource(R.string.custom_share_command)) },
                    placeholder = { Text(stringResource(R.string.share_command_placeholder)) },
                    value = if (useKeyboardTypeCommand) keyboardTypeCommand else customShareTargetCommand,
                    onValueChange = { value ->
                        customShareTargetCommand = value
                    },
                    enabled = !useKeyboardTypeCommand,
                    modifier = Modifier
                        .fillMaxWidth()
                        .dpadFocusable(),
                )
                RowWithCheckbox(
                    checked = !useKeyboardTypeCommand && customShareTargetShowOutput,
                    text = stringResource(R.string.show_output),
                    enabled = !useKeyboardTypeCommand,
                    onCheckedChange = { checked ->
                        customShareTargetShowOutput = checked
                    },
                )
                RowWithCheckbox(
                    checked = runInBackground,
                    text = stringResource(R.string.run_command_in_background),
                    onCheckedChange = { checked ->
                        runInBackground = checked
                    },
                )
            }
        },
        properties = DialogProperties(dismissOnClickOutside = false, decorFitsSystemWindows = false),
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    val updated = initialRemoteCommands.toMutableMap()
                    if (useKeyboardTypeCommand) {
                        keyboardTypeTemplate?.let {
                            updated[RemoteControlKey.KEYBOARD_TYPE_INPUT] = it.copy(runInBackground = runInBackground)
                        }
                        updated.remove(RemoteControlKey.SHARE_TEXT)
                    } else {
                        updated[RemoteControlKey.SHARE_TEXT] =
                            (updated[RemoteControlKey.SHARE_TEXT] ?: Command(""))
                                .copy(
                                    command = customShareTargetCommand,
                                    showOutput = customShareTargetShowOutput,
                                    runInBackground = runInBackground,
                                )
                    }
                    onSave(updated)
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
private fun ShareTargetSettingsDialogPreview() {
    SSHRemoteTheme {
        Surface {
            ShareTargetSettingsDialog(
                initialRemoteCommands = wtypePreset,
                onDismiss = {},
                onSave = {},
            )
        }
    }
}
