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

import android.os.Build
import android.os.VibrationEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.HapticFeedback
import com.stefansundin.sshremote.getVibrator

@Composable
fun HapticFeedbackSettingDialog(
    currentHapticFeedback: HapticFeedback,
    onHapticFeedbackSelected: (HapticFeedback) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val vibrator = remember { getVibrator(context) }

    DisposableEffect(Unit) {
        onDispose {
            vibrator.cancel()
        }
    }

    var hasUserEdited by rememberSaveable { mutableStateOf(!HapticFeedback.presets.contains(currentHapticFeedback)) }
    var customDurationString by rememberSaveable { mutableStateOf(if (hasUserEdited) currentHapticFeedback.duration.toString() else "") }

    // This effect runs on initial composition and whenever the selection changes.
    // It updates the text field, but only if the user hasn't manually edited it.
    LaunchedEffect(currentHapticFeedback) {
        if (!hasUserEdited) {
            customDurationString = currentHapticFeedback.duration.toString()
        }
    }

    val onSelection = { hapticFeedback: HapticFeedback ->
        onHapticFeedbackSelected(hapticFeedback)
        // Perform vibration using the local vibrator instance
        if (hapticFeedback.duration > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        hapticFeedback.duration,
                        VibrationEffect.DEFAULT_AMPLITUDE,
                    ),
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(hapticFeedback.duration)
            }
        } else {
            vibrator.cancel()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Haptic feedback") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "Vibration duration in milliseconds",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                HapticFeedback.presets.forEach { hapticFeedback ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (hapticFeedback == currentHapticFeedback),
                                onClick = { onSelection(hapticFeedback) },
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = (hapticFeedback == currentHapticFeedback),
                            onClick = null,
                        )
                        Text(
                            text = hapticFeedback.label,
                            modifier = Modifier.padding(start = 16.dp),
                        )
                    }
                }
                // Row for the "Custom" option
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = currentHapticFeedback is HapticFeedback.Custom,
                            onClick = {
                                val duration = customDurationString.toLongOrNull() ?: 0L
                                onSelection(HapticFeedback.Custom(duration))
                            },
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = currentHapticFeedback is HapticFeedback.Custom,
                            onClick = null,
                        )
                        Text(
                            text = "Custom",
                            modifier = Modifier.padding(start = 16.dp),
                        )
                    }
                    TextField(
                        value = customDurationString,
                        onValueChange = {
                            hasUserEdited = true // Lock the text field from preset updates
                            customDurationString = it.filter { char -> char.isDigit() }
                            val duration = customDurationString.toLongOrNull() ?: 0L
                            onSelection(HapticFeedback.Custom(duration))
                        },
                        enabled = currentHapticFeedback is HapticFeedback.Custom,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                        label = { Text("ms") },
                        singleLine = true,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
