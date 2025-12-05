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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.ConnectionStatus
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.data.host.SmartVolumeSettings
import com.stefansundin.sshremote.ui.KeyEvent

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
        RemoteControlLayout(isLandscape, onKeyEvent, commands, connectionStatus, smartVolumeSettings, volume, muted)
    }
}

@Composable
private fun RemoteControlLayout(
    isLandscape: Boolean,
    onKeyEvent: (KeyEvent) -> Unit,
    commands: Map<RemoteControlKey, Command>? = null,
    connectionStatus: ConnectionStatus? = null,
    smartVolumeSettings: SmartVolumeSettings? = null,
    volume: String? = null,
    muted: Boolean? = null,
) {
    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Dpad(onKeyEvent, commands, connectionStatus)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            ) {
                VolumeStatus(smartVolumeSettings, volume, muted)
                ActionButtons(onKeyEvent, commands, connectionStatus)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
        ) {
            VolumeStatus(smartVolumeSettings, volume, muted)
            Dpad(onKeyEvent, commands, connectionStatus)
            ActionButtons(onKeyEvent, commands, connectionStatus)
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
    connectionStatus: ConnectionStatus? = null,
) {
    val isEnabled = connectionStatus == null || connectionStatus == ConnectionStatus.CONNECTED

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
            RepeatingButton(
                onClick = { onKeyEvent(KeyEvent.Click(RemoteControlKey.VOLUME_DOWN)) },
                onPress = { onKeyEvent(KeyEvent.Down(RemoteControlKey.VOLUME_DOWN)) },
                onRelease = { onKeyEvent(KeyEvent.Up(RemoteControlKey.VOLUME_DOWN)) },
                modifier = buttonModifier,
                enabled = isEnabled && (commands == null || !commands[RemoteControlKey.VOLUME_DOWN]?.command.isNullOrEmpty()),
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = "Volume Down")
            }
            RepeatingButton(
                onClick = { onKeyEvent(KeyEvent.Click(RemoteControlKey.MUTE)) },
                onPress = { onKeyEvent(KeyEvent.Down(RemoteControlKey.MUTE)) },
                onRelease = { onKeyEvent(KeyEvent.Up(RemoteControlKey.MUTE)) },
                modifier = buttonModifier,
                enabled = isEnabled && (commands == null || !commands[RemoteControlKey.MUTE]?.command.isNullOrEmpty()),
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeOff, contentDescription = "Mute")
            }
            RepeatingButton(
                onClick = { onKeyEvent(KeyEvent.Click(RemoteControlKey.VOLUME_UP)) },
                onPress = { onKeyEvent(KeyEvent.Down(RemoteControlKey.VOLUME_UP)) },
                onRelease = { onKeyEvent(KeyEvent.Up(RemoteControlKey.VOLUME_UP)) },
                modifier = buttonModifier,
                enabled = isEnabled && (commands == null || !commands[RemoteControlKey.VOLUME_UP]?.command.isNullOrEmpty()),
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Volume Up")
            }
        }
        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RepeatingButton(
                onClick = { onKeyEvent(KeyEvent.Click(RemoteControlKey.BACK)) },
                onPress = { onKeyEvent(KeyEvent.Down(RemoteControlKey.BACK)) },
                onRelease = { onKeyEvent(KeyEvent.Up(RemoteControlKey.BACK)) },
                modifier = buttonModifier,
                enabled = isEnabled && (commands == null || !commands[RemoteControlKey.BACK]?.command.isNullOrEmpty()),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            RepeatingButton(
                onClick = { onKeyEvent(KeyEvent.Click(RemoteControlKey.HOME)) },
                onPress = { onKeyEvent(KeyEvent.Down(RemoteControlKey.HOME)) },
                onRelease = { onKeyEvent(KeyEvent.Up(RemoteControlKey.HOME)) },
                modifier = buttonModifier,
                enabled = isEnabled && (commands == null || !commands[RemoteControlKey.HOME]?.command.isNullOrEmpty()),
            ) {
                Icon(Icons.Default.Home, contentDescription = "Home")
            }
            RepeatingButton(
                onClick = { onKeyEvent(KeyEvent.Click(RemoteControlKey.MENU)) },
                onPress = { onKeyEvent(KeyEvent.Down(RemoteControlKey.MENU)) },
                onRelease = { onKeyEvent(KeyEvent.Up(RemoteControlKey.MENU)) },
                modifier = buttonModifier,
                enabled = isEnabled && (commands == null || !commands[RemoteControlKey.MENU]?.command.isNullOrEmpty()),
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        }
        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RepeatingButton(
                onClick = { onKeyEvent(KeyEvent.Click(RemoteControlKey.PREVIOUS)) },
                onPress = { onKeyEvent(KeyEvent.Down(RemoteControlKey.PREVIOUS)) },
                onRelease = { onKeyEvent(KeyEvent.Up(RemoteControlKey.PREVIOUS)) },
                modifier = buttonModifier,
                enabled = isEnabled && (commands == null || !commands[RemoteControlKey.PREVIOUS]?.command.isNullOrEmpty()),
            ) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
            }
            RepeatingButton(
                onClick = { onKeyEvent(KeyEvent.Click(RemoteControlKey.PLAY_PAUSE)) },
                onPress = { onKeyEvent(KeyEvent.Down(RemoteControlKey.PLAY_PAUSE)) },
                onRelease = { onKeyEvent(KeyEvent.Up(RemoteControlKey.PLAY_PAUSE)) },
                modifier = buttonModifier,
                enabled = isEnabled && (commands == null || !commands[RemoteControlKey.PLAY_PAUSE]?.command.isNullOrEmpty()),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play/Pause")
                Icon(Icons.Default.Pause, contentDescription = "Play/Pause")
            }
            RepeatingButton(
                onClick = { onKeyEvent(KeyEvent.Click(RemoteControlKey.NEXT)) },
                onPress = { onKeyEvent(KeyEvent.Down(RemoteControlKey.NEXT)) },
                onRelease = { onKeyEvent(KeyEvent.Up(RemoteControlKey.NEXT)) },
                modifier = buttonModifier,
                enabled = isEnabled && (commands == null || !commands[RemoteControlKey.NEXT]?.command.isNullOrEmpty()),
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next")
            }
        }
    }
}

@Composable
private fun Dpad(
    onKeyEvent: (KeyEvent) -> Unit,
    commands: Map<RemoteControlKey, Command>? = null,
    connectionStatus: ConnectionStatus? = null,
) {
    val dpadSize = 280.dp
    val iconOffset = dpadSize / 3f
    val iconSize = dpadSize / 3f
    val isEnabled = connectionStatus == null || connectionStatus == ConnectionStatus.CONNECTED

    Box(
        modifier = Modifier.size(dpadSize),
        contentAlignment = Alignment.Center,
    ) {
        val directionalButtonModifier = Modifier.fillMaxSize()

        // UP
        RepeatingButton(
            onClick = { onKeyEvent(KeyEvent.Click(RemoteControlKey.UP)) },
            onPress = { onKeyEvent(KeyEvent.Down(RemoteControlKey.UP)) },
            onRelease = { onKeyEvent(KeyEvent.Up(RemoteControlKey.UP)) },
            shape = ArcShape(225f, 90f),
            modifier = directionalButtonModifier,
            contentPadding = PaddingValues(0.dp),
            enabled = isEnabled && (commands == null || !commands[RemoteControlKey.UP]?.command.isNullOrEmpty()),
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
        RepeatingButton(
            onClick = { onKeyEvent(KeyEvent.Click(RemoteControlKey.RIGHT)) },
            onPress = { onKeyEvent(KeyEvent.Down(RemoteControlKey.RIGHT)) },
            onRelease = { onKeyEvent(KeyEvent.Up(RemoteControlKey.RIGHT)) },
            shape = ArcShape(315f, 90f),
            modifier = directionalButtonModifier,
            contentPadding = PaddingValues(0.dp),
            enabled = isEnabled && (commands == null || !commands[RemoteControlKey.RIGHT]?.command.isNullOrEmpty()),
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
        RepeatingButton(
            onClick = { onKeyEvent(KeyEvent.Click(RemoteControlKey.DOWN)) },
            onPress = { onKeyEvent(KeyEvent.Down(RemoteControlKey.DOWN)) },
            onRelease = { onKeyEvent(KeyEvent.Up(RemoteControlKey.DOWN)) },
            shape = ArcShape(45f, 90f),
            modifier = directionalButtonModifier,
            contentPadding = PaddingValues(0.dp),
            enabled = isEnabled && (commands == null || !commands[RemoteControlKey.DOWN]?.command.isNullOrEmpty()),
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
        RepeatingButton(
            onClick = { onKeyEvent(KeyEvent.Click(RemoteControlKey.LEFT)) },
            onPress = { onKeyEvent(KeyEvent.Down(RemoteControlKey.LEFT)) },
            onRelease = { onKeyEvent(KeyEvent.Up(RemoteControlKey.LEFT)) },
            shape = ArcShape(135f, 90f),
            modifier = directionalButtonModifier,
            contentPadding = PaddingValues(0.dp),
            enabled = isEnabled && (commands == null || !commands[RemoteControlKey.LEFT]?.command.isNullOrEmpty()),
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
            RepeatingButton(
                onClick = { onKeyEvent(KeyEvent.Click(RemoteControlKey.SELECT)) },
                onPress = { onKeyEvent(KeyEvent.Down(RemoteControlKey.SELECT)) },
                onRelease = { onKeyEvent(KeyEvent.Up(RemoteControlKey.SELECT)) },
                modifier = Modifier.size(dpadSize / 2.8f),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                enabled = isEnabled && (commands == null || !commands[RemoteControlKey.SELECT]?.command.isNullOrEmpty()),
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
