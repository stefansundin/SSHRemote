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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.ConnectionStatus
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.data.host.SmartVolumeSettings
import com.stefansundin.sshremote.ui.KeyEvent

private val MinDimensionForActionButtons = 500.dp

@Composable
fun RemoteControl(
    onKeyEvent: (KeyEvent) -> Unit,
    modifier: Modifier = Modifier,
    commands: Map<RemoteControlKey, Command>? = null,
    connectionStatus: ConnectionStatus? = null,
    smartVolumeSettings: SmartVolumeSettings? = null,
    volume: String? = null,
    muted: Boolean? = null,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        val showActionButtons = if (isLandscape) {
            maxWidth > MinDimensionForActionButtons
        } else {
            maxHeight > MinDimensionForActionButtons
        }
        val layoutMode = if (!showActionButtons) {
            RemoteLayoutMode.Compact
        } else if (isLandscape) {
            RemoteLayoutMode.Landscape
        } else {
            RemoteLayoutMode.Portrait
        }

        RemoteControlLayout(layoutMode, onKeyEvent, commands, connectionStatus, smartVolumeSettings, volume, muted)
    }
}

private enum class RemoteLayoutMode {
    /** Compact would normally happen when the app is in split screen mode with another app. */
    Compact,
    Landscape,
    Portrait,
}

@Composable
private fun RemoteControlLayout(
    layoutMode: RemoteLayoutMode,
    onKeyEvent: (KeyEvent) -> Unit,
    commands: Map<RemoteControlKey, Command>? = null,
    connectionStatus: ConnectionStatus? = null,
    smartVolumeSettings: SmartVolumeSettings? = null,
    volume: String? = null,
    muted: Boolean? = null,
) {
    val isConnected = connectionStatus == null || connectionStatus == ConnectionStatus.CONNECTED

    when (layoutMode) {
        RemoteLayoutMode.Compact -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Dpad(onKeyEvent, commands, isConnected)
            }
        }
        RemoteLayoutMode.Landscape -> {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Dpad(onKeyEvent, commands, isConnected)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                ) {
                    VolumeStatus(smartVolumeSettings, volume, muted)
                    ActionButtons(onKeyEvent, commands, isConnected)
                }
            }
        }
        RemoteLayoutMode.Portrait -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
            ) {
                VolumeStatus(smartVolumeSettings, volume, muted)
                Dpad(onKeyEvent, commands, isConnected)
                ActionButtons(onKeyEvent, commands, isConnected)
            }
        }
    }
}

@Composable
private fun VolumeStatus(smartVolumeSettings: SmartVolumeSettings?, volume: String?, muted: Boolean?) {
    if (smartVolumeSettings?.readCurrentVolume == true) {
        if (volume != null) {
            Text("Volume: $volume${if (muted == true) " (muted)" else ""}")
        } else {
            // Render an empty Text to prevent components moving once we have the volume
            Text("")
        }
    }
}

@Composable
private fun ActionButtons(
    onKeyEvent: (KeyEvent) -> Unit,
    commands: Map<RemoteControlKey, Command>? = null,
    isConnected: Boolean = true,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val buttonModifier = Modifier
            .weight(1f)
            .height(56.dp)
        val rowModifier = Modifier.fillMaxWidth()

        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RemoteButton(RemoteControlKey.VOLUME_DOWN, onKeyEvent, buttonModifier, commands, isConnected) {
                Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = "Volume Down")
            }
            RemoteButton(RemoteControlKey.MUTE, onKeyEvent, buttonModifier, commands, isConnected) {
                Icon(Icons.AutoMirrored.Filled.VolumeOff, contentDescription = "Mute")
            }
            RemoteButton(RemoteControlKey.VOLUME_UP, onKeyEvent, buttonModifier, commands, isConnected) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Volume Up")
            }
        }
        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RemoteButton(RemoteControlKey.BACK, onKeyEvent, buttonModifier, commands, isConnected) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            RemoteButton(RemoteControlKey.HOME, onKeyEvent, buttonModifier, commands, isConnected) {
                Icon(Icons.Default.Home, contentDescription = "Home")
            }
            RemoteButton(RemoteControlKey.MENU, onKeyEvent, buttonModifier, commands, isConnected) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        }
        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RemoteButton(RemoteControlKey.PREVIOUS, onKeyEvent, buttonModifier, commands, isConnected) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
            }
            RemoteButton(RemoteControlKey.PLAY_PAUSE, onKeyEvent, buttonModifier, commands, isConnected) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play/Pause")
                Icon(Icons.Default.Pause, contentDescription = "Play/Pause")
            }
            RemoteButton(RemoteControlKey.NEXT, onKeyEvent, buttonModifier, commands, isConnected) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next")
            }
        }
    }
}

@Composable
private fun Dpad(
    onKeyEvent: (KeyEvent) -> Unit,
    commands: Map<RemoteControlKey, Command>? = null,
    isConnected: Boolean = true,
) {
    val dpadSize = 280.dp
    val iconOffset = dpadSize / 3f
    val iconSize = dpadSize / 3f

    Box(
        modifier = Modifier.size(dpadSize),
        contentAlignment = Alignment.Center,
    ) {
        val directionalButtonModifier = Modifier.fillMaxSize()

        // UP
        RemoteButton(
            key = RemoteControlKey.UP,
            onKeyEvent = onKeyEvent,
            modifier = directionalButtonModifier,
            commands = commands,
            isConnected = isConnected,
            shape = ArcShape(225f, 90f),
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Up",
                modifier = Modifier
                    .size(iconSize)
                    .offset(y = -iconOffset),
            )
        }

        // RIGHT
        RemoteButton(
            key = RemoteControlKey.RIGHT,
            onKeyEvent = onKeyEvent,
            modifier = directionalButtonModifier,
            commands = commands,
            isConnected = isConnected,
            shape = ArcShape(315f, 90f),
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Right",
                modifier = Modifier
                    .size(iconSize)
                    .offset(x = iconOffset),
            )
        }

        // DOWN
        RemoteButton(
            key = RemoteControlKey.DOWN,
            onKeyEvent = onKeyEvent,
            modifier = directionalButtonModifier,
            commands = commands,
            isConnected = isConnected,
            shape = ArcShape(45f, 90f),
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Down",
                modifier = Modifier
                    .size(iconSize)
                    .offset(y = iconOffset),
            )
        }

        // LEFT
        RemoteButton(
            key = RemoteControlKey.LEFT,
            onKeyEvent = onKeyEvent,
            modifier = directionalButtonModifier,
            commands = commands,
            isConnected = isConnected,
            shape = ArcShape(135f, 90f),
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Left",
                modifier = Modifier
                    .size(iconSize)
                    .offset(x = -iconOffset),
            )
        }

        // CENTER
        if (commands == null || commands[RemoteControlKey.SELECT] == null || !commands[RemoteControlKey.SELECT]?.command?.isEmpty()!!) {
            RemoteButton(
                key = RemoteControlKey.SELECT,
                onKeyEvent = onKeyEvent,
                modifier = Modifier.size(dpadSize / 2.8f),
                commands = commands,
                isConnected = isConnected,
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = Color.Transparent,
                ),
            ) {
                Icon(
                    Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Select",
                    modifier = Modifier.fillMaxSize(0.8f),
                )
            }
        }
    }
}

@Composable
private fun RemoteButton(
    key: RemoteControlKey,
    onKeyEvent: (KeyEvent) -> Unit,
    modifier: Modifier = Modifier,
    commands: Map<RemoteControlKey, Command>? = null,
    isConnected: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    content: @Composable RowScope.() -> Unit,
) {
    val command = commands?.get(key)

    RepeatingButton(
        onClick = { onKeyEvent(KeyEvent.Click(key)) },
        modifier = modifier,
        repeating = command?.repeat == true,
        onLongClick = if (command?.longPressCommand.isNullOrEmpty()) null else {
            { onKeyEvent(KeyEvent.LongPress(key)) }
        },
        enabled = isConnected && (commands == null || !command?.command.isNullOrEmpty()),
        shape = shape,
        contentPadding = contentPadding,
        colors = colors,
        content = content,
    )
}

private class ArcShape(private val startAngle: Float, private val sweepAngle: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path().apply {
            moveTo(size.width / 2f, size.height / 2f)
            arcTo(
                rect = Rect(0f, 0f, size.width, size.height),
                startAngleDegrees = startAngle,
                sweepAngleDegrees = sweepAngle,
                forceMoveTo = false,
            )
            close()
        }
        return Outline.Generic(path)
    }
}

@Preview(widthDp = 400, heightDp = 800)
@Composable
private fun PortraitPreview() {
    RemoteControlLayout(RemoteLayoutMode.Portrait, {})
}

@Preview(widthDp = 800, heightDp = 400)
@Composable
private fun LandscapePreview() {
    RemoteControlLayout(RemoteLayoutMode.Landscape, {})
}

@Preview(widthDp = 400, heightDp = 400)
@Composable
private fun CompactPreview() {
    RemoteControlLayout(RemoteLayoutMode.Compact, {})
}
