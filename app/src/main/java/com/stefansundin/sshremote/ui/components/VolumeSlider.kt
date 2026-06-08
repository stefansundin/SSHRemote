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

import android.view.MotionEvent
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

internal fun parseVolumePercent(volume: String?): Float? {
    if (volume == null) return null
    return volume.trimEnd('%').trim().toFloatOrNull()
}

internal class VolumeSetThrottle(private val onVolumeSet: (Int) -> Unit) {
    private var lastSentValue: Int? = null
    private var lastSentAt: Long = 0L

    private fun minIntervalMs(changeSinceLastSent: Int): Long = when {
        changeSinceLastSent >= 10 -> 0L
        changeSinceLastSent >= 5 -> 250L
        else -> 500L
    }

    private fun send(value: Int, now: Long) {
        lastSentValue = value
        lastSentAt = now
        onVolumeSet(value)
    }

    fun onDrag(value: Float) {
        val now = System.currentTimeMillis()
        val intValue = value.roundToInt()
        val previousValue = lastSentValue

        if (previousValue == null) {
            send(intValue, now)
            return
        }

        val changeSinceLastSent = abs(intValue - previousValue)
        val minInterval = minIntervalMs(changeSinceLastSent)
        if (now - lastSentAt >= minInterval) {
            send(intValue, now)
        }
    }

    fun onPress(value: Float) {
        val intValue = value.roundToInt()
        if (intValue != lastSentValue) {
            send(intValue, System.currentTimeMillis())
        }
    }

    fun onFinished(value: Float) {
        val intValue = value.roundToInt()
        if (intValue != lastSentValue) {
            send(intValue, System.currentTimeMillis())
        }
    }
}

@Composable
internal fun HorizontalVolumeSlider(volume: String?, throttle: VolumeSetThrottle) {
    val committedValue = parseVolumePercent(volume)
    var sliderValue by remember { mutableFloatStateOf(committedValue ?: 0f) }
    var sliderWidthPx by remember { mutableIntStateOf(0) }
    var isDragging by remember { mutableStateOf(false) }
    var suppressUpdates by remember { mutableStateOf(false) }
    var hasInteracted by remember { mutableStateOf(false) }

    // When drag finishes, suppress external updates for 1 second to allow network round-trip
    LaunchedEffect(isDragging) {
        if (!isDragging && hasInteracted && !suppressUpdates) {
            suppressUpdates = true
            delay(1000.milliseconds)
            suppressUpdates = false
        }
    }

    // Update slider value from committed value only when not dragging and update suppression window is closed
    LaunchedEffect(committedValue, isDragging, suppressUpdates) {
        if (!isDragging && !suppressUpdates && committedValue != null) {
            sliderValue = committedValue
        }
    }

    Slider(
        enabled = volume != null,
        value = sliderValue,
        onValueChange = {
            hasInteracted = true
            isDragging = true
            suppressUpdates = false
            sliderValue = it
            throttle.onDrag(it)
        },
        onValueChangeFinished = {
            throttle.onFinished(sliderValue)
            isDragging = false
        },
        valueRange = 0f..100f,
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { sliderWidthPx = it.width }
            .pointerInteropFilter { event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN && volume != null && sliderWidthPx > 0) {
                    // Handle immediate response on press-down (before any drag movement).
                    // The Material Slider doesn't respond immediately on press; it waits for drag motion.
                    // We intercept ACTION_DOWN, calculate the position the user tapped, and send it immediately
                    // for better UX - the volume responds right away without requiring any movement.
                    val pressedValue = ((event.x.coerceIn(0f, sliderWidthPx.toFloat())) / sliderWidthPx) * 100f
                    hasInteracted = true
                    isDragging = true
                    suppressUpdates = false
                    sliderValue = pressedValue
                    throttle.onPress(pressedValue)
                }
                false
            },
    )
}

@Composable
internal fun VerticalVolumeSlider(volume: String?, throttle: VolumeSetThrottle) {
    // I tried using VerticalSlider in material3 1.5.0-alpha20 but could not get it working completely
    // For now it is better to just rotate a regular slider
    val committedValue = parseVolumePercent(volume)
    var sliderValue by remember { mutableFloatStateOf(committedValue ?: 0f) }
    var sliderHeightPx by remember { mutableIntStateOf(0) }
    var isDragging by remember { mutableStateOf(false) }
    var suppressUpdates by remember { mutableStateOf(false) }
    var hasInteracted by remember { mutableStateOf(false) }

    // When drag finishes, suppress external updates for 1 second to allow network round-trip
    LaunchedEffect(isDragging) {
        if (!isDragging && hasInteracted && !suppressUpdates) {
            suppressUpdates = true
            delay(1000.milliseconds)
            suppressUpdates = false
        }
    }

    // Update slider value from committed value only when not dragging and update suppression window is closed
    LaunchedEffect(committedValue, isDragging, suppressUpdates) {
        if (!isDragging && !suppressUpdates && committedValue != null) {
            sliderValue = committedValue
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .width(56.dp)
            .fillMaxHeight()
            .onSizeChanged { sliderHeightPx = it.height }
            .pointerInteropFilter { event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN && volume != null && sliderHeightPx > 0) {
                    // Handle immediate response on press-down (before any drag movement).
                    // The Material Slider doesn't respond immediately on press; it waits for drag motion.
                    // We intercept ACTION_DOWN, calculate the position the user tapped, and send it immediately
                    // for better UX - the volume responds right away without requiring any movement.
                    // The slider is rotated -90 degrees, so we invert Y (top = 100%, bottom = 0%).
                    val clampedY = event.y.coerceIn(0f, sliderHeightPx.toFloat())
                    val pressedValue = (1f - (clampedY / sliderHeightPx)) * 100f
                    hasInteracted = true
                    isDragging = true
                    suppressUpdates = false
                    sliderValue = pressedValue
                    throttle.onPress(pressedValue)
                }
                false
            },
        contentAlignment = Alignment.Center,
    ) {
        val sliderLength = (maxHeight - 32.dp).coerceAtLeast(120.dp)
        Slider(
            enabled = volume != null,
            value = sliderValue,
            onValueChange = {
                hasInteracted = true
                isDragging = true
                suppressUpdates = false
                sliderValue = it
                throttle.onDrag(it)
            },
            onValueChangeFinished = {
                throttle.onFinished(sliderValue)
                isDragging = false
            },
            valueRange = 0f..100f,
            modifier = Modifier
                .align(Alignment.Center)
                .requiredWidth(sliderLength)
                .graphicsLayer { rotationZ = -90f },
        )
    }
}
