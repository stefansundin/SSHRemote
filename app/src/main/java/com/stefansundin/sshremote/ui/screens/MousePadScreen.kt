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

package com.stefansundin.sshremote.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

sealed interface MouseEvent {
    data class Move(val dx: Float, val dy: Float) : MouseEvent
    object LeftClick : MouseEvent
    object RightClick : MouseEvent
}

@Composable
fun MousePadScreen(onMouseEvent: (MouseEvent) -> Unit, modifier: Modifier = Modifier) {
    BackHandler(enabled = true) {
        // Prevent back gesture while this screen is active
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
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        ) {
            Button(onClick = { onMouseEvent(MouseEvent.LeftClick) }) {
                Text("Left Click")
            }
            Button(onClick = { onMouseEvent(MouseEvent.RightClick) }) {
                Text("Right Click")
            }
        }
    }
}

@Composable
private fun TouchPad(onMove: (dx: Float, dy: Float) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onMove(dragAmount.x, dragAmount.y)
                }
            },
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("Mouse Pad")
        }
    }
}

@Preview
@Composable
private fun MousePadScreenPreview() {
    MaterialTheme {
        MousePadScreen(
            onMouseEvent = { event ->
                Log.d("MousePadScreen", "Received event: $event")
            },
        )
    }
}
