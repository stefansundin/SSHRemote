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
import android.os.Build
import android.view.SoundEffectConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme

@Composable
fun ColorSettingDialog(
    useDynamicColors: Boolean,
    onUseDynamicColorsChange: (Boolean) -> Unit,
    backgroundColor: Color?,
    onBackgroundColorChange: (Color?) -> Unit,
    primaryColor: Color?,
    onPrimaryColorChange: (Color?) -> Unit,
    onPrimaryColorColor: Color?,
    onOnPrimaryColorChange: (Color?) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var showBackgroundColorPicker by remember { mutableStateOf(false) }
    var showPrimaryColorPicker by remember { mutableStateOf(false) }
    var showOnPrimaryColorPicker by remember { mutableStateOf(false) }
    var originalColor by remember { mutableStateOf<Color?>(null) }
    val view = LocalView.current

    if (showBackgroundColorPicker) {
        ColorPickerDialog(
            title = stringResource(R.string.choose_background_color),
            initialColor = backgroundColor ?: MaterialTheme.colorScheme.background,
            onColorChanged = onBackgroundColorChange,
            onConfirm = {
                showBackgroundColorPicker = false
            },
            onDismiss = {
                onBackgroundColorChange(originalColor)
                showBackgroundColorPicker = false
            },
        )
    }

    if (showPrimaryColorPicker) {
        ColorPickerDialog(
            title = stringResource(R.string.choose_primary_color),
            initialColor = primaryColor ?: MaterialTheme.colorScheme.primary,
            onColorChanged = onPrimaryColorChange,
            onConfirm = {
                showPrimaryColorPicker = false
            },
            onDismiss = {
                onPrimaryColorChange(originalColor)
                showPrimaryColorPicker = false
            },
        )
    }

    if (showOnPrimaryColorPicker) {
        ColorPickerDialog(
            title = stringResource(R.string.choose_on_primary_color),
            initialColor = onPrimaryColorColor ?: MaterialTheme.colorScheme.onPrimary,
            onColorChanged = onOnPrimaryColorChange,
            onConfirm = {
                showOnPrimaryColorPicker = false
            },
            onDismiss = {
                onOnPrimaryColorChange(originalColor)
                showOnPrimaryColorPicker = false
            },
        )
    }

    AlertDialog(
        title = { Text(stringResource(R.string.color_options)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .toggleable(
                                value = useDynamicColors,
                                onValueChange = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    onUseDynamicColorsChange(it)
                                },
                                role = Role.Switch,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.dynamic_colors),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = useDynamicColors,
                            onCheckedChange = null,
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.color_overrides),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clickable {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            originalColor = backgroundColor
                            showBackgroundColorPicker = true
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.background_color),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    if (backgroundColor != null) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(backgroundColor),
                        )
                    }
                    TextButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            onBackgroundColorChange(null)
                        },
                        enabled = backgroundColor != null,
                    ) {
                        Text(stringResource(R.string.clear))
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clickable {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            originalColor = primaryColor
                            showPrimaryColorPicker = true
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.primary_color),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    if (primaryColor != null) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(primaryColor),
                        )
                    }
                    TextButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            onPrimaryColorChange(null)
                        },
                        enabled = primaryColor != null,
                    ) {
                        Text(stringResource(R.string.clear))
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clickable {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            originalColor = onPrimaryColorColor
                            showOnPrimaryColorPicker = true
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.text_on_primary_color),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    if (onPrimaryColorColor != null) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(onPrimaryColorColor),
                        )
                    }
                    TextButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            onOnPrimaryColorChange(null)
                        },
                        enabled = onPrimaryColorColor != null,
                    ) {
                        Text(stringResource(R.string.clear))
                    }
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onConfirm()
                },
            ) {
                Text(stringResource(R.string.ok))
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
private fun ColorSettingDialogPreview() {
    SSHRemoteTheme {
        Surface {
            ColorSettingDialog(
                useDynamicColors = false,
                onUseDynamicColorsChange = {},
                backgroundColor = Color.Black,
                onBackgroundColorChange = {},
                primaryColor = Color.Red,
                onPrimaryColorChange = {},
                onPrimaryColorColor = Color.White,
                onOnPrimaryColorChange = {},
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}
