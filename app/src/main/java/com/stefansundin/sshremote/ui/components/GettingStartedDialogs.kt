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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.data.host.presets
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme

@Composable
fun GettingStartedDialog(onDismiss: () -> Unit, onConfirm: () -> Unit, onHelp: () -> Unit) {
    val view = LocalView.current

    AlertDialog(
        title = { Text(stringResource(R.string.getting_started_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.getting_started_text))
            }
        },
        properties = DialogProperties(dismissOnClickOutside = false),
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onConfirm()
                },
            ) {
                Text(stringResource(R.string.select_preset))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onHelp()
                },
            ) {
                Text(stringResource(R.string.help))
            }
        },
    )
}

@Composable
fun SelectPresetDialog(onDismiss: () -> Unit, onPresetSelected: (Map<RemoteControlKey, Command>?) -> Unit) {
    val view = LocalView.current
    val noPreset = stringResource(R.string.no_preset)

    AlertDialog(
        title = { Text(stringResource(R.string.select_preset)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                (presets.keys + noPreset).forEach { presetKey ->
                    ListItem(
                        headlineContent = { Text(presetKey) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                onPresetSelected(presets[presetKey])
                            },
                    )
                    HorizontalDivider()
                }
            }
        },
        properties = DialogProperties(dismissOnClickOutside = false),
        onDismissRequest = onDismiss,
        confirmButton = {},
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
private fun GettingStartedDialogPreview() {
    SSHRemoteTheme {
        Surface {
            GettingStartedDialog(onDismiss = {}, onConfirm = {}, onHelp = {})
        }
    }
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
private fun SelectPresetDialogPreview() {
    SSHRemoteTheme {
        Surface {
            SelectPresetDialog(onDismiss = {}, onPresetSelected = {})
        }
    }
}
