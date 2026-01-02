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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RepeatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    repeating: Boolean = false,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit,
) {
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongClick by rememberUpdatedState(onLongClick)
    val scope = rememberCoroutineScope()

    val gestureModifier = if (enabled) {
        Modifier
            .clip(shape)
            .pointerInput(repeating) {
                if (repeating) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downPress = PressInteraction.Press(down.position)
                        val heldButtonJob = scope.launch {
                            interactionSource.emit(downPress)
                            currentOnClick()
                            delay(500)
                            while (enabled && down.pressed) {
                                currentOnClick()
                                delay(100)
                            }
                        }
                        val up = waitForUpOrCancellation()
                        heldButtonJob.cancel()
                        val releaseOrCancel = when (up) {
                            null -> PressInteraction.Cancel(downPress)
                            else -> PressInteraction.Release(downPress)
                        }
                        scope.launch {
                            interactionSource.emit(releaseOrCancel)
                        }
                    }
                } else if (currentOnLongClick == null) {
                    detectTapGestures(
                        onPress = { offset ->
                            currentOnClick()
                            val press = PressInteraction.Press(offset)
                            interactionSource.emit(press)
                            try {
                                tryAwaitRelease()
                                interactionSource.emit(PressInteraction.Release(press))
                            } catch (_: CancellationException) {
                                interactionSource.emit(PressInteraction.Cancel(press))
                            }
                        },
                    )
                } else {
                    detectTapGestures(
                        onPress = { offset ->
                            val press = PressInteraction.Press(offset)
                            interactionSource.emit(press)
                            try {
                                tryAwaitRelease()
                                interactionSource.emit(PressInteraction.Release(press))
                            } catch (_: CancellationException) {
                                interactionSource.emit(PressInteraction.Cancel(press))
                            }
                        },
                        onTap = { currentOnClick() },
                        onLongPress = currentOnLongClick?.let { { it() } },
                    )
                }
            }
    } else {
        Modifier
    }

    Box(modifier = modifier) {
        Button(
            onClick = {},
            modifier = Modifier.matchParentSize(),
            enabled = enabled,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            content = content,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .then(gestureModifier),
        )
    }
}
