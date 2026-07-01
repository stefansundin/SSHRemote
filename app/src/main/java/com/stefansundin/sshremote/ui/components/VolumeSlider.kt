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
import android.os.Handler
import android.os.Looper
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

private const val UPDATE_SUPPRESSION_WINDOW_MS = 750L
private val SliderVisualEdgeInset = 16.dp

private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

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

internal fun horizontalSliderValueForX(x: Float, sliderWidthPx: Int, edgePaddingPx: Float): Float {
    return sliderFractionForPosition(x, sliderWidthPx, edgePaddingPx) * 100f
}

internal fun verticalSliderValueForY(y: Float, sliderHeightPx: Int, edgePaddingPx: Float): Float {
    return (1f - sliderFractionForPosition(y, sliderHeightPx, edgePaddingPx)) * 100f
}

private fun sliderFractionForPosition(position: Float, sizePx: Int, edgePaddingPx: Float): Float {
    if (sizePx <= 0) return 0f

    val size = sizePx.toFloat()
    val clampedPosition = position.coerceIn(0f, size)
    val clampedPadding = edgePaddingPx.coerceIn(0f, size / 2f)
    val activeEnd = size - clampedPadding

    if (activeEnd <= clampedPadding) {
        return if (clampedPosition >= size / 2f) 1f else 0f
    }

    return when {
        clampedPosition <= clampedPadding -> 0f
        clampedPosition >= activeEnd -> 1f
        else -> (clampedPosition - clampedPadding) / (activeEnd - clampedPadding)
    }
}

internal fun interface CancelScheduledSend {
    fun cancel()
}

internal class VolumeSetThrottle(
    private val onVolumeSet: (Int) -> Unit,
    private val nowMs: () -> Long = SystemClock::uptimeMillis,
    private val scheduleDelayedSend: (delayMs: Long, callback: () -> Unit) -> CancelScheduledSend = { delayMs, callback ->
        val runnable = Runnable(callback)
        mainHandler.postDelayed(runnable, delayMs)
        CancelScheduledSend { mainHandler.removeCallbacks(runnable) }
    },
) {
    private var lastSentValue: Int? = null
    private var lastSentAt: Long = 0L
    private var pendingValue: Int? = null
    private var pendingSendAt: Long? = null
    private var pendingSendCancellation: CancelScheduledSend? = null

    private fun minIntervalMs(changeSinceLastSent: Int): Long = when {
        changeSinceLastSent >= 10 -> 0L
        changeSinceLastSent >= 5 -> 250L
        else -> 500L
    }

    private fun shouldSendImmediately(value: Int): Boolean = value == 0 || value == 100

    private fun cancelPendingSend() {
        pendingSendCancellation?.cancel()
        pendingSendCancellation = null
        pendingSendAt = null
    }

    private fun clearPendingValue() {
        cancelPendingSend()
        pendingValue = null
    }

    private fun send(value: Int, now: Long): Boolean {
        clearPendingValue()
        lastSentValue = value
        lastSentAt = now
        onVolumeSet(value)
        return true
    }

    private fun schedulePendingSend(value: Int, now: Long): Boolean {
        val previousValue = lastSentValue ?: return send(value, now)
        if (value == previousValue) {
            clearPendingValue()
            return false
        }
        if (shouldSendImmediately(value)) {
            return send(value, now)
        }

        pendingValue = value
        val minInterval = minIntervalMs(abs(value - previousValue))
        val sendAt = lastSentAt + minInterval
        val delayMs = (sendAt - now).coerceAtLeast(0L)

        if (delayMs == 0L) {
            return send(value, now)
        }

        if (pendingSendAt != sendAt) {
            cancelPendingSend()
            pendingSendAt = sendAt
            pendingSendCancellation = scheduleDelayedSend(delayMs) {
                flushPending()
            }
        }

        return false
    }

    private fun flushPending(): Boolean {
        cancelPendingSend()
        val value = pendingValue ?: return false
        val now = nowMs()
        val previousValue = lastSentValue ?: return send(value, now)
        if (value == previousValue) {
            pendingValue = null
            return false
        }
        if (shouldSendImmediately(value)) {
            return send(value, now)
        }

        val minInterval = minIntervalMs(abs(value - previousValue))
        return if (now - lastSentAt >= minInterval) {
            send(value, now)
        } else {
            schedulePendingSend(value, now)
        }
    }

    fun dispose() {
        clearPendingValue()
    }

    fun onDrag(value: Float): Boolean {
        val now = nowMs()
        val intValue = value.roundToInt()

        if (lastSentValue == null) {
            return send(intValue, now)
        }

        return schedulePendingSend(intValue, now)
    }

    fun onPress(value: Float): Boolean {
        val intValue = value.roundToInt()
        return if (intValue != lastSentValue) {
            send(intValue, nowMs())
        } else {
            clearPendingValue()
            false
        }
    }

    fun onFinished(value: Float): Boolean {
        val intValue = value.roundToInt()
        return if (intValue != lastSentValue) {
            send(intValue, nowMs())
        } else {
            clearPendingValue()
            false
        }
    }
}

@Composable
internal fun HorizontalVolumeSlider(volume: String?, throttle: VolumeSetThrottle) {
    val committedValue = parseVolumePercent(volume)
    val density = LocalDensity.current
    val view = LocalView.current
    val edgePaddingPx = with(density) { SliderVisualEdgeInset.toPx() }
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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { sliderWidthPx = it.width }
            .pointerInteropFilter { event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN && volume != null && sliderWidthPx > 0) {
                    // Handle immediate response on press-down (before any drag movement).
                    // The Material Slider doesn't respond immediately on press; it waits for drag motion.
                    // We intercept ACTION_DOWN, calculate the position the user tapped, and send it immediately
                    // for better UX - the volume responds right away without requiring any movement.
                    val pressedValue = horizontalSliderValueForX(event.x, sliderWidthPx, edgePaddingPx)
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
        val sliderWidth = (maxWidth - SliderVisualEdgeInset * 2).coerceAtLeast(120.dp)
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
            modifier = Modifier.requiredWidth(sliderWidth),
        )
    }
}

@Composable
internal fun VerticalVolumeSlider(volume: String?, throttle: VolumeSetThrottle) {
    // I tried using VerticalSlider in material3 1.5.0-alpha20 but could not get it working completely
    // For now it is better to just rotate a regular slider
    val committedValue = parseVolumePercent(volume)
    val density = LocalDensity.current
    val view = LocalView.current
    val edgePaddingPx = with(density) { SliderVisualEdgeInset.toPx() }
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
                    val pressedValue = verticalSliderValueForY(event.y, sliderHeightPx, edgePaddingPx)
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
        val sliderLength = (maxHeight - SliderVisualEdgeInset * 2).coerceAtLeast(120.dp)
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
