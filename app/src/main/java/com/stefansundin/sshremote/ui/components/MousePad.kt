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

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.ConnectionStatus
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.ui.MouseEvent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MousePad(
    onMouseEvent: (MouseEvent) -> Unit,
    modifier: Modifier = Modifier,
    commands: Map<RemoteControlKey, Command>? = null,
    connectionStatus: ConnectionStatus? = null,
) {
    BackHandler(enabled = true) {
        // Prevent back gesture while this component is active
    }

    val isEnabled = connectionStatus == null || connectionStatus == ConnectionStatus.CONNECTED
    val leftClickEnabled =
        isEnabled && (commands == null || !commands[RemoteControlKey.MOUSE_LEFT_CLICK]?.command.isNullOrEmpty())
    val rightClickEnabled =
        isEnabled && (commands == null || !commands[RemoteControlKey.MOUSE_RIGHT_CLICK]?.command.isNullOrEmpty())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TouchPad(
            onMove = { dx, dy -> onMouseEvent(MouseEvent.Move(dx, dy)) },
            onPan = { dx, dy -> onMouseEvent(MouseEvent.Pan(dx, dy)) },
            onLeftClick = { onMouseEvent(MouseEvent.LeftClick) },
            onRightClick = { onMouseEvent(MouseEvent.RightClick) },
            leftClickEnabled = leftClickEnabled,
            rightClickEnabled = rightClickEnabled,
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        ) {
            Button(
                onClick = { onMouseEvent(MouseEvent.LeftClick) },
                enabled = leftClickEnabled,
            ) {
                Text("Left Click")
            }
            Button(
                onClick = { onMouseEvent(MouseEvent.RightClick) },
                enabled = rightClickEnabled,
            ) {
                Text("Right Click")
            }
        }
    }
}

private enum class GestureState {
    Undecided,
    LongPress,
    Move,
    Scroll,
}

@SuppressLint("ClickableViewAccessibility")
@Composable
private fun TouchPad(
    onMove: (dx: Float, dy: Float) -> Unit,
    onPan: (dx: Float, dy: Float) -> Unit,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
    leftClickEnabled: Boolean,
    rightClickEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(leftClickEnabled, rightClickEnabled) {
                coroutineScope {

                    // This coroutine handles mouse hover and scroll events.
                    // It runs on the Initial pass to catch events before they are consumed by gesture detectors.
                    launch {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val change = event.changes.firstOrNull() ?: continue
                                if (change.type != PointerType.Mouse) continue

//                                if (event.buttons.isSecondaryPressed) {
//                                    onRightClick()
//                                }

                                // Handle mouse scroll wheel for panning
                                if (event.changes.any { it.scrollDelta != Offset.Zero }) {
                                    val scroll = event.changes.sumOf { it.scrollDelta.y.toDouble() }.toFloat()
                                    onPan(0f, -scroll * 20f) // Adjust multiplier for sensitivity
                                }
                            }
                        }
                    }

                    launch {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var state = GestureState.Undecided

                            val longPressJob = launch {
                                if (!rightClickEnabled) return@launch
                                delay(viewConfiguration.longPressTimeoutMillis)
                                state = GestureState.LongPress
                                onRightClick()
                            }

                            do {
                                val event = awaitPointerEvent()
                                val pressedChanges = event.changes.filter { it.pressed }

                                if (state == GestureState.Undecided) {
                                    val pointer = pressedChanges.find { it.id == down.id }
                                    if (pointer != null) {
                                        val distance = pointer.position - down.position
                                        if (pressedChanges.size > 1) {
                                            longPressJob.cancel()
                                            state = GestureState.Scroll
                                        } else if (distance.getDistanceSquared() > viewConfiguration.touchSlop * viewConfiguration.touchSlop) {
                                            longPressJob.cancel()
                                            state = GestureState.Move
                                        }
                                    }
                                }

                                if (state == GestureState.Move && pressedChanges.size > 1) {
                                    state = GestureState.Scroll
                                }

                                if (state == GestureState.Move) {
                                    val pointer = pressedChanges.find { it.id == down.id }
                                    if (pointer != null) {
                                        val change = pointer.positionChange()
                                        onMove(change.x, change.y)
                                    }
                                } else if (state == GestureState.Scroll) {
                                    if (pressedChanges.size >= 2) {
                                        val centroid = pressedChanges
                                            .map { it.position }
                                            .fold(Offset.Zero) { acc, offset -> acc + offset } / pressedChanges.size.toFloat()
                                        val prevCentroid = pressedChanges
                                            .map { it.previousPosition }
                                            .fold(Offset.Zero) { acc, offset -> acc + offset } / pressedChanges.size.toFloat()
                                        val pan = centroid - prevCentroid
                                        onPan(pan.x, pan.y)
                                    }
                                }
                                event.changes.forEach { it.consume() }
                            } while (event.changes.any { it.pressed } && state != GestureState.LongPress)

                            longPressJob.cancel()

                            if (state == GestureState.Undecided) {
                                if (leftClickEnabled) {
                                    onLeftClick()
                                }
                            }
                        }
                    }
                }
            },
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Mouse Pad")
                Text("Tap for left click")
                Text("Long press for right click")
                Text("Scroll using two fingers")
            }

            // Mouse Capture Interop View
            val view = LocalView.current
            AndroidView(
                modifier = Modifier.matchParentSize(),
                factory = { context ->
                    object : View(context) {
                        override fun onCapturedPointerEvent(event: MotionEvent): Boolean {
                            // During capture, x and y are relative deltas
                            val dx = event.x
                            val dy = event.y

                            if (dx != 0f || dy != 0f) {
                                onMove(dx, dy)
                            }

                            val vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                            val hScroll = event.getAxisValue(MotionEvent.AXIS_HSCROLL)
                            if (vScroll != 0f || hScroll != 0f) {
                                val config = android.view.ViewConfiguration.get(context)
                                val vScale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    config.scaledVerticalScrollFactor
                                } else {
                                    64f
                                }
                                val hScale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    config.scaledHorizontalScrollFactor
                                } else {
                                    64f
                                }
                                onPan(hScroll * hScale, vScroll * vScale)
                            }

                            if (event.actionMasked == MotionEvent.ACTION_BUTTON_PRESS) {
                                if (event.actionButton == MotionEvent.BUTTON_PRIMARY) {
                                    if (leftClickEnabled) {
                                        onLeftClick()
                                    }
                                } else if (event.actionButton == MotionEvent.BUTTON_SECONDARY) {
                                    if (rightClickEnabled) {
                                        onRightClick()
                                    }
                                }
                            }

                            // Release capture if the user releases the button
                            if (event.action == MotionEvent.ACTION_UP ||
                                event.action == MotionEvent.ACTION_BUTTON_RELEASE
                            ) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    releasePointerCapture()
                                }
                            }
                            return true
                        }
                    }.apply {
                        isFocusable = true
                        isFocusableInTouchMode = true

                        setOnTouchListener { v, event ->
                            // Check for Mouse input specifically
                            if (event.isFromSource(InputDevice.SOURCE_MOUSE)) {
                                if (event.action == MotionEvent.ACTION_DOWN) {

                                    if (event.isButtonPressed(MotionEvent.BUTTON_PRIMARY)) {
                                        // Start capture when mouse button is depressed
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            v.requestPointerCapture()
                                        }
                                    } else if (event.isButtonPressed(MotionEvent.BUTTON_SECONDARY)) {
                                        onRightClick()
                                    }

                                    return@setOnTouchListener true
                                }
                            }
                            // Return false to let standard touch events (fingers) pass through
                            // to the Compose pointerInput handler
                            false
                        }
                    }
                },
            )
        }
    }
}

@Preview
@Composable
private fun MousePadPreview() {
    MaterialTheme {
        MousePad(
            onMouseEvent = { event ->
                Log.d("MousePad", "Received event: $event")
            },
            connectionStatus = ConnectionStatus.CONNECTED,
        )
    }
}
