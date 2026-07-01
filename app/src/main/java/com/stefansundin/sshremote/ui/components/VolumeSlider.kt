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

import android.os.Build
import android.os.SystemClock
import android.view.HapticFeedbackConstants
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

private const val UPDATE_SUPPRESSION_WINDOW_MS = 750L

private fun dragStartHapticConstant(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        HapticFeedbackConstants.DRAG_START
    } else {
        HapticFeedbackConstants.LONG_PRESS
    }
}

private fun dragTickHapticConstant(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
    } else {
        HapticFeedbackConstants.CLOCK_TICK
    }
}

private fun dragEndHapticConstant(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        HapticFeedbackConstants.GESTURE_END
    } else {
        HapticFeedbackConstants.CLOCK_TICK
    }
}

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

    private fun send(value: Int, now: Long): Boolean {
        lastSentValue = value
        lastSentAt = now
        onVolumeSet(value)
        return true
    }

    fun onDrag(value: Float): Boolean {
        val now = SystemClock.uptimeMillis()
        val intValue = value.roundToInt()
        val previousValue = lastSentValue

        if (previousValue == null) {
            return send(intValue, now)
        } else if (intValue != lastSentValue) {
            val changeSinceLastSent = abs(intValue - previousValue)
            val minInterval = minIntervalMs(changeSinceLastSent)
            if (now - lastSentAt >= minInterval) {
                return send(intValue, now)
            }
        }

        return false
    }

    fun onPress(value: Float): Boolean {
        val intValue = value.roundToInt()
        return if (intValue != lastSentValue) {
            send(intValue, SystemClock.uptimeMillis())
        } else {
            false
        }
    }

    fun onFinished(value: Float): Boolean {
        val intValue = value.roundToInt()
        return if (intValue != lastSentValue) {
            send(intValue, SystemClock.uptimeMillis())
        } else {
            false
        }
    }
}

@Composable
internal fun HorizontalVolumeSlider(volume: String?, throttle: VolumeSetThrottle) {
    val committedValue = parseVolumePercent(volume)
    val view = LocalView.current
    var sliderValue by remember { mutableFloatStateOf(committedValue ?: 0f) }
    var sliderWidthPx by remember { mutableIntStateOf(0) }
    var isDragging by remember { mutableStateOf(false) }
    var hasInteracted by remember { mutableStateOf(false) }
    var lastSliderSetAtMs by remember { mutableLongStateOf(0L) }
    var suppressionWindowEndedSignal by remember { mutableIntStateOf(0) }

    // Force a re-check when the suppression window ends so the latest committed value can be applied.
    LaunchedEffect(lastSliderSetAtMs, hasInteracted) {
        if (!hasInteracted) return@LaunchedEffect
        val elapsedSinceLocalSetMs = SystemClock.uptimeMillis() - lastSliderSetAtMs
        val remainingMs = UPDATE_SUPPRESSION_WINDOW_MS - elapsedSinceLocalSetMs
        if (remainingMs > 0L) {
            delay(remainingMs.milliseconds)
            suppressionWindowEndedSignal++
        }
    }

    // Update slider value from committed value only when not dragging and update suppression window is closed
    LaunchedEffect(committedValue, isDragging, lastSliderSetAtMs, suppressionWindowEndedSignal) {
        val elapsedSinceLocalSetMs = SystemClock.uptimeMillis() - lastSliderSetAtMs
        val suppressUpdates = hasInteracted && elapsedSinceLocalSetMs < UPDATE_SUPPRESSION_WINDOW_MS
        if (!isDragging && !suppressUpdates && committedValue != null) {
            sliderValue = committedValue
        }
    }

    Slider(
        enabled = volume != null,
        value = sliderValue,
        onValueChange = {
            lastSliderSetAtMs = SystemClock.uptimeMillis()
            hasInteracted = true
            isDragging = true
            sliderValue = it
            if (throttle.onDrag(it)) {
                view.performHapticFeedback(dragTickHapticConstant())
            }
        },
        onValueChangeFinished = {
            if (throttle.onFinished(sliderValue)) {
                view.performHapticFeedback(dragEndHapticConstant())
            }
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
                    lastSliderSetAtMs = SystemClock.uptimeMillis()
                    hasInteracted = true
                    isDragging = true
                    sliderValue = pressedValue
                    if (throttle.onPress(pressedValue)) {
                        view.performHapticFeedback(dragStartHapticConstant())
                    }
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
    val view = LocalView.current
    var sliderValue by remember { mutableFloatStateOf(committedValue ?: 0f) }
    var sliderHeightPx by remember { mutableIntStateOf(0) }
    var isDragging by remember { mutableStateOf(false) }
    var hasInteracted by remember { mutableStateOf(false) }
    var lastSliderSetAtMs by remember { mutableLongStateOf(0L) }
    var suppressionWindowEndedSignal by remember { mutableIntStateOf(0) }

    // Force a re-check when the suppression window ends so the latest committed value can be applied.
    LaunchedEffect(lastSliderSetAtMs, hasInteracted) {
        if (!hasInteracted) return@LaunchedEffect
        val elapsedSinceLocalSetMs = SystemClock.uptimeMillis() - lastSliderSetAtMs
        val remainingMs = UPDATE_SUPPRESSION_WINDOW_MS - elapsedSinceLocalSetMs
        if (remainingMs > 0L) {
            delay(remainingMs.milliseconds)
            suppressionWindowEndedSignal++
        }
    }

    // Update slider value from committed value only when not dragging and update suppression window is closed
    LaunchedEffect(committedValue, isDragging, lastSliderSetAtMs, suppressionWindowEndedSignal) {
        val elapsedSinceLocalSetMs = SystemClock.uptimeMillis() - lastSliderSetAtMs
        val suppressUpdates = hasInteracted && elapsedSinceLocalSetMs < UPDATE_SUPPRESSION_WINDOW_MS
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
                    lastSliderSetAtMs = SystemClock.uptimeMillis()
                    hasInteracted = true
                    isDragging = true
                    sliderValue = pressedValue
                    if (throttle.onPress(pressedValue)) {
                        view.performHapticFeedback(dragStartHapticConstant())
                    }
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
                lastSliderSetAtMs = SystemClock.uptimeMillis()
                hasInteracted = true
                isDragging = true
                sliderValue = it
                if (throttle.onDrag(it)) {
                    view.performHapticFeedback(dragTickHapticConstant())
                }
            },
            onValueChangeFinished = {
                if (throttle.onFinished(sliderValue)) {
                    view.performHapticFeedback(dragEndHapticConstant())
                }
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
