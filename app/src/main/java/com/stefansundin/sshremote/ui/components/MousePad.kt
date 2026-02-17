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
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemGestures
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.data.host.ConnectionStatus
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.ui.MouseEvent
import com.stefansundin.sshremote.ui.screens.sampleHost
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MousePad(
    onMouseEvent: (MouseEvent) -> Unit,
    modifier: Modifier = Modifier,
    editing: Boolean = false,
    host: Host? = null,
    connectionStatus: ConnectionStatus? = null,
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val systemGestures = WindowInsets.systemGestures
    val isGestureNavigation = systemGestures.getLeft(density, layoutDirection) > 0 ||
            systemGestures.getRight(density, layoutDirection) > 0

    val isEnabled = editing || connectionStatus == ConnectionStatus.CONNECTED
    val mouseMoveConfigured = host == null ||
            !host.remoteCommands?.get(RemoteControlKey.MOUSE_MOVE)?.command.isNullOrEmpty()
    val leftClickConfigured =
        host == null || !host.remoteCommands?.get(RemoteControlKey.MOUSE_LEFT_CLICK)?.command.isNullOrEmpty()
    val rightClickConfigured =
        host == null || !host.remoteCommands?.get(RemoteControlKey.MOUSE_RIGHT_CLICK)?.command.isNullOrEmpty()
    val leftDownUpConfigured = host == null || (host.remoteCommands != null &&
            !host.remoteCommands[RemoteControlKey.MOUSE_LEFT_DOWN]?.command.isNullOrEmpty() &&
            !host.remoteCommands[RemoteControlKey.MOUSE_LEFT_UP]?.command.isNullOrEmpty()
            )
    val rightDownUpConfigured = host == null || (host.remoteCommands != null &&
            !host.remoteCommands[RemoteControlKey.MOUSE_RIGHT_DOWN]?.command.isNullOrEmpty() &&
            !host.remoteCommands[RemoteControlKey.MOUSE_RIGHT_UP]?.command.isNullOrEmpty()
            )
    val scrollingConfigured =
        host == null ||
                (host.remoteCommands != null &&
                        (!host.remoteCommands[RemoteControlKey.MOUSE_PAN_UP]?.command.isNullOrEmpty() ||
                                !host.remoteCommands[RemoteControlKey.MOUSE_PAN_RIGHT]?.command.isNullOrEmpty() ||
                                !host.remoteCommands[RemoteControlKey.MOUSE_PAN_DOWN]?.command.isNullOrEmpty() ||
                                !host.remoteCommands[RemoteControlKey.MOUSE_PAN_LEFT]?.command.isNullOrEmpty()))

    BackHandler(enabled = mouseMoveConfigured && isGestureNavigation) {
        // Prevent back gesture while this component is active
    }

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
            onLeftDown = { onMouseEvent(MouseEvent.LeftDown) },
            onLeftUp = { onMouseEvent(MouseEvent.LeftUp) },
            editing = editing,
            mouseMoveConfigured = mouseMoveConfigured,
            leftClickConfigured = leftClickConfigured,
            rightClickConfigured = rightClickConfigured,
            scrollingConfigured = scrollingConfigured,
            leftDownUpConfigured = leftDownUpConfigured,
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        ) {
            Button(
                onClick = { },
                enabled = isEnabled && leftDownUpConfigured,
                modifier = Modifier.pointerInput(isEnabled, leftDownUpConfigured) {
                    if (!isEnabled || !leftDownUpConfigured) return@pointerInput
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        onMouseEvent(MouseEvent.LeftDown)
                        waitForUpOrCancellation()
                        onMouseEvent(MouseEvent.LeftUp)
                    }
                },
            ) {
                Text("Left Click")
            }
            Button(
                onClick = { },
                enabled = isEnabled && rightDownUpConfigured,
                modifier = Modifier.pointerInput(isEnabled, rightDownUpConfigured) {
                    if (!isEnabled || !rightDownUpConfigured) return@pointerInput
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        onMouseEvent(MouseEvent.RightDown)
                        waitForUpOrCancellation()
                        onMouseEvent(MouseEvent.RightUp)
                    }
                },
            ) {
                Text("Right Click")
            }
        }
    }
}

private enum class GestureState {
    Undecided,
    LongPressDrag,
    Move,
    Scroll,
    TwoFingerLongPress,
}

@Composable
private fun TouchPad(
    onMove: (dx: Float, dy: Float) -> Unit,
    onPan: (dx: Float, dy: Float) -> Unit,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
    onLeftDown: () -> Unit,
    onLeftUp: () -> Unit,
    editing: Boolean,
    mouseMoveConfigured: Boolean,
    leftClickConfigured: Boolean,
    rightClickConfigured: Boolean,
    scrollingConfigured: Boolean,
    leftDownUpConfigured: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(leftClickConfigured, rightClickConfigured, leftDownUpConfigured) {
                coroutineScope {
                    awaitEachGesture {
                        var state = GestureState.Undecided
                        val down = awaitFirstDown(requireUnconsumed = false)

                        val longPressJob = if (leftDownUpConfigured) {
                            launch {
                                delay(viewConfiguration.longPressTimeoutMillis)
                                state = GestureState.LongPressDrag
                                onLeftDown()
                            }
                        } else null

                        var twoFingerLongPressJob: Job? = null
                        var initialTwoFingerCentroid = Offset.Unspecified

                        do {
                            val event = awaitPointerEvent()
                            val pressedChanges = event.changes.filter { it.pressed }

                            if (state == GestureState.Undecided) {
                                val pointer = pressedChanges.find { it.id == down.id }
                                if (pointer != null) {
                                    val distance = pointer.position - down.position
                                    if (pressedChanges.size > 1) {
                                        longPressJob?.cancel()
                                        state = GestureState.TwoFingerLongPress
                                        initialTwoFingerCentroid = pressedChanges
                                            .map { it.position }
                                            .fold(Offset.Zero) { acc, offset -> acc + offset } / pressedChanges.size.toFloat()
                                        if (rightClickConfigured) {
                                            twoFingerLongPressJob = launch {
                                                delay(viewConfiguration.longPressTimeoutMillis)
                                                onRightClick()
                                            }
                                        }
                                    } else if (distance.getDistanceSquared() > viewConfiguration.touchSlop * viewConfiguration.touchSlop) {
                                        longPressJob?.cancel()
                                        state = GestureState.Move
                                    }
                                }
                            }

                            if (state == GestureState.TwoFingerLongPress) {
                                if (pressedChanges.size < 2) {
                                    twoFingerLongPressJob?.cancel()
                                    twoFingerLongPressJob = null
                                } else {
                                    val currentCentroid = pressedChanges
                                        .map { it.position }
                                        .fold(Offset.Zero) { acc, offset -> acc + offset } / pressedChanges.size.toFloat()
                                    val totalDistance = (currentCentroid - initialTwoFingerCentroid).getDistance()
                                    if (totalDistance > viewConfiguration.touchSlop) {
                                        twoFingerLongPressJob?.cancel()
                                        twoFingerLongPressJob = null
                                        state = GestureState.Scroll
                                    }
                                }
                            }

                            if (state == GestureState.Move || state == GestureState.LongPressDrag) {
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
                        } while (event.changes.any { it.pressed })

                        longPressJob?.cancel()
                        twoFingerLongPressJob?.cancel()

                        when (state) {
                            GestureState.Undecided -> if (leftClickConfigured) onLeftClick()
                            GestureState.LongPressDrag -> if (leftDownUpConfigured) onLeftUp()
                            else -> {}
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
                if (editing) {
                    Text("Mouse Pad", textAlign = TextAlign.Center)
                    Text("Tap to edit mouse commands", textAlign = TextAlign.Center)
                } else {
                    if (mouseMoveConfigured || leftClickConfigured || rightClickConfigured || scrollingConfigured) {
                        Text("Mouse Pad", textAlign = TextAlign.Center)
                        if (leftClickConfigured) {
                            Text("Tap to left click", textAlign = TextAlign.Center)
                        }
                        if (leftDownUpConfigured) {
                            Text("Long press to click and drag", textAlign = TextAlign.Center)
                        }
                        if (rightClickConfigured) {
                            Text("Long press with two fingers to right click", textAlign = TextAlign.Center)
                        }
                        if (scrollingConfigured) {
                            Text("Scroll using two fingers", textAlign = TextAlign.Center)
                        }
                    } else {
                        Text("Mouse Pad Disabled", textAlign = TextAlign.Center)
                        Text("Mouse commands not configured", textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun MousePadPreview() {
    SSHRemoteTheme {
        Surface {
            MousePad(
                onMouseEvent = { event ->
                    Log.d("MousePad", "Received event: $event")
                },
                connectionStatus = ConnectionStatus.CONNECTED,
                host = sampleHost,
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun MousePadPreview_Editing() {
    SSHRemoteTheme {
        Surface {
            MousePad(
                onMouseEvent = { event ->
                    Log.d("MousePad", "Received event: $event")
                },
                editing = true,
            )
        }
    }
}
