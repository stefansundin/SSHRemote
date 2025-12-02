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

import android.util.Log
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    val leftClickEnabled = isEnabled && (commands == null || !commands[RemoteControlKey.MOUSE_LEFT_CLICK]?.command.isNullOrEmpty())
    val rightClickEnabled = isEnabled && (commands == null || !commands[RemoteControlKey.MOUSE_RIGHT_CLICK]?.command.isNullOrEmpty())

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
                    awaitEachGesture {
                        var state = GestureState.Undecided
                        val down = awaitFirstDown(requireUnconsumed = false)

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
            connectionStatus = ConnectionStatus.CONNECTED
        )
    }
}
