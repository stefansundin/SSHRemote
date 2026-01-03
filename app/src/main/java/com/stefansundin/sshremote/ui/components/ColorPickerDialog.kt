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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlin.math.roundToInt

@Composable
fun ColorPickerDialog(
    title: String = "Choose a color",
    initialColor: Color,
    onColorChanged: (Color) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    // Initialize color values only once when the dialog enters the composition.
    // We intentionally ignore changes to initialColor to prevent the sliders from resetting while the user is dragging them.
    val initialHsl = remember {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(initialColor.toArgb(), hsl)
        hsl
    }

    var isHslMode by remember { mutableStateOf(true) }

    // HSL State
    var hue by remember { mutableStateOf(initialHsl[0]) }
    var saturation by remember { mutableStateOf(initialHsl[1]) }
    var lightness by remember { mutableStateOf(initialHsl[2]) }

    // RGB State
    var red by remember { mutableStateOf(initialColor.red) }
    var green by remember { mutableStateOf(initialColor.green) }
    var blue by remember { mutableStateOf(initialColor.blue) }

    val hslToColor = { h: Float, s: Float, l: Float ->
        Color.hsl(h, s, l)
    }

    val selectedColor = if (isHslMode) {
        hslToColor(hue, saturation, lightness)
    } else {
        Color(red, green, blue)
    }

    LaunchedEffect(selectedColor) {
        onColorChanged(selectedColor)
    }

    fun updateHslFromColor(color: Color) {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(color.toArgb(), hsl)
        hue = hsl[0]
        saturation = hsl[1]
        lightness = hsl[2]
    }

    fun updateRgbFromColor(color: Color) {
        red = color.red
        green = color.green
        blue = color.blue
    }

    AlertDialog(
        title = { Text(title) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(selectedColor)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(16.dp),
                                ),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val hexColor = String.format("#%06X", (0xFFFFFF and selectedColor.toArgb()))
                        Text(
                            text = hexColor,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                SecondaryTabRow(selectedTabIndex = if (isHslMode) 0 else 1) {
                    Tab(
                        selected = isHslMode,
                        onClick = {
                            if (!isHslMode) {
                                updateHslFromColor(selectedColor)
                                isHslMode = true
                            }
                        },
                        text = { Text("HSL") },
                    )
                    Tab(
                        selected = !isHslMode,
                        onClick = {
                            if (isHslMode) {
                                updateRgbFromColor(selectedColor)
                                isHslMode = false
                            }
                        },
                        text = { Text("RGB") },
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (isHslMode) {
                    Text("Hue: ${hue.toInt()}")
                    Slider(
                        value = hue,
                        onValueChange = { hue = it },
                        valueRange = 0f..360f,
                    )
                    Text("Saturation: ${(saturation * 100).toInt()}%")
                    Slider(
                        value = saturation,
                        onValueChange = { saturation = it },
                        valueRange = 0f..1f,
                    )
                    Text("Lightness: ${(lightness * 100).toInt()}%")
                    Slider(
                        value = lightness,
                        onValueChange = { lightness = it },
                        valueRange = 0f..1f,
                    )
                } else {
                    Text("Red: ${(red * 255).roundToInt()}")
                    Slider(
                        value = red,
                        onValueChange = { red = it },
                        valueRange = 0f..1f,
                    )
                    Text("Green: ${(green * 255).roundToInt()}")
                    Slider(
                        value = green,
                        onValueChange = { green = it },
                        valueRange = 0f..1f,
                    )
                    Text("Blue: ${(blue * 255).roundToInt()}")
                    Slider(
                        value = blue,
                        onValueChange = { blue = it },
                        valueRange = 0f..1f,
                    )
                }
            }
        },
        onDismissRequest = onDismiss,
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

@Preview(showBackground = true)
@Composable
private fun ColorPickerDialogPreview() {
    SSHRemoteTheme {
        ColorPickerDialog(
            initialColor = Color.Red,
            onColorChanged = {},
            onConfirm = {},
            onDismiss = {},
        )
    }
}
