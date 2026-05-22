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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.data.host.SmartVolumeSettings
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme

@Composable
fun SmartVolumeSettingsDialog(
    settings: SmartVolumeSettings?,
    onDismiss: () -> Unit,
    onSave: (SmartVolumeSettings) -> Unit,
    onTest: () -> Unit,
) {
    var readCurrentVolume by rememberSaveable { mutableStateOf(settings?.readCurrentVolume ?: false) }
    var showSlider by rememberSaveable { mutableStateOf(settings?.showSlider ?: false) }
    var controlVolumeWithHardwareButtons by rememberSaveable {
        mutableStateOf(
            settings?.controlVolumeWithHardwareButtons ?: false,
        )
    }
    val view = LocalView.current

    AlertDialog(
        title = { Text(stringResource(R.string.smart_volume_settings)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.pactl_installation_requirement),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                RowWithCheckbox(
                    checked = readCurrentVolume,
                    text = stringResource(R.string.read_current_volume),
                    onCheckedChange = { readCurrentVolume = it },
                )
                RowWithCheckbox(
                    enabled = readCurrentVolume,
                    checked = readCurrentVolume && showSlider,
                    text = stringResource(R.string.show_volume_slider),
                    onCheckedChange = { showSlider = it },
                )
                RowWithCheckbox(
                    checked = controlVolumeWithHardwareButtons,
                    text = stringResource(R.string.control_volume_with_hardware_buttons),
                    onCheckedChange = { controlVolumeWithHardwareButtons = it },
                )

                Button(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onTest()
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.test_pactl))
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
                        SmartVolumeSettings(
                            readCurrentVolume = readCurrentVolume,
                            showSlider = showSlider,
                            controlVolumeWithHardwareButtons = controlVolumeWithHardwareButtons,
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
private fun SmartVolumeSettingsDialogPreview() {
    SSHRemoteTheme {
        Surface {
            SmartVolumeSettingsDialog(
                settings = SmartVolumeSettings(
                    readCurrentVolume = true,
                    controlVolumeWithHardwareButtons = true,
                ),
                onDismiss = {},
                onSave = {},
                onTest = {},
            )
        }
    }
}
