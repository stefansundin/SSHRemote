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
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.data.host.ConnectionStatus
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.data.host.SmartVolumeSettings
import com.stefansundin.sshremote.data.host.macosVlcPreset
import com.stefansundin.sshremote.data.host.wtypePreset
import com.stefansundin.sshremote.ui.KeyEvent
import com.stefansundin.sshremote.ui.screens.sampleHost
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlin.math.roundToInt

private val MinDimensionForActionButtons = 400.dp

@Composable
fun RemoteControl(
    onKeyEvent: (KeyEvent) -> Unit,
    modifier: Modifier = Modifier,
    editing: Boolean = false,
    host: Host?,
    connectionStatus: ConnectionStatus? = null,
    volume: String? = null,
    muted: Boolean? = null,
    onVolumeSet: (Int) -> Unit,
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

        RemoteControlLayout(layoutMode, onKeyEvent, host, editing, connectionStatus, volume, muted, onVolumeSet)
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
    host: Host? = null,
    editing: Boolean = false,
    connectionStatus: ConnectionStatus? = null,
    volume: String? = null,
    muted: Boolean? = null,
    onVolumeSet: (Int) -> Unit,
) {
    val context = LocalContext.current
    val isConnected = editing || connectionStatus == ConnectionStatus.CONNECTED
    val tapToEditButtonCommand = stringResource(R.string.tap_to_edit_button_command)

    LaunchedEffect(editing) {
        if (editing) {
            Toast.makeText(context, tapToEditButtonCommand, Toast.LENGTH_SHORT).show()
        }
    }

    val showSlider = !editing && host?.smartVolume?.showSlider == true && connectionStatus == ConnectionStatus.CONNECTED
    val volumeThrottle = remember(onVolumeSet) { VolumeSetThrottle(onVolumeSet) }

    when (layoutMode) {
        RemoteLayoutMode.Compact -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Dpad(onKeyEvent, host, editing, isConnected)
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
                Dpad(onKeyEvent, host, editing, isConnected)
                if (showSlider) {
                    VerticalVolumeSlider(volume, volumeThrottle)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                ) {
                    StatusText(host, editing, volume, muted)
                    ActionButtons(onKeyEvent, host, editing, isConnected, muted)
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
                StatusText(host, editing, volume, muted)
                Dpad(onKeyEvent, host, editing, isConnected)
                if (showSlider) {
                    HorizontalVolumeSlider(volume, volumeThrottle)
                }
                ActionButtons(onKeyEvent, host, editing, isConnected, muted)
            }
        }
    }
}

@Composable
private fun StatusText(
    host: Host?,
    editing: Boolean,
    volume: String?,
    muted: Boolean?,
) {
    if (!editing && host != null) {
        if (host.smartVolume != null) {
            VolumeStatus(host.smartVolume, volume, muted)
        } else if (host.remoteCommands == null) {
            // This text is only displayed when the user picks "No preset" upon first connection
            Text(
                stringResource(R.string.configure_remote_commands_prompt),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun VolumeStatus(smartVolumeSettings: SmartVolumeSettings?, volume: String?, muted: Boolean?) {
    if (smartVolumeSettings?.readCurrentVolume == true) {
        if (volume != null) {
            Text(
                if (muted == true) stringResource(R.string.volume_muted_format, volume)
                else stringResource(R.string.volume_format, volume),
            )
        } else {
            // Render an empty Text to prevent components moving once we have the volume
            Text("")
        }
    }
}

private fun parseVolumePercent(volume: String?): Float? {
    if (volume == null) return null
    return volume.trimEnd('%').trim().toFloatOrNull()
}

private class VolumeSetThrottle(private val onVolumeSet: (Int) -> Unit) {
    private var lastSentValue: Int? = null
    private var lastCallTime: Long = 0L

    fun onDrag(value: Float) {
        val now = System.currentTimeMillis()
        if (now - lastCallTime >= 500L) {
            lastCallTime = now
            val intValue = value.roundToInt()
            lastSentValue = intValue
            onVolumeSet(intValue)
        }
    }

    fun onFinished(value: Float) {
        val intValue = value.roundToInt()
        if (intValue != lastSentValue) {
            lastSentValue = intValue
            onVolumeSet(intValue)
        }
    }
}

@Composable
private fun HorizontalVolumeSlider(volume: String?, throttle: VolumeSetThrottle) {
    val committedValue = parseVolumePercent(volume)
    var sliderValue by remember(committedValue) { mutableFloatStateOf(committedValue ?: 0f) }

    Slider(
        enabled = volume != null,
        value = sliderValue,
        onValueChange = {
            sliderValue = it
            throttle.onDrag(it)
        },
        onValueChangeFinished = { throttle.onFinished(sliderValue) },
        valueRange = 0f..100f,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun VerticalVolumeSlider(volume: String?, throttle: VolumeSetThrottle) {
    // I tried using VerticalSlider in material3 1.5.0-alpha20 but could not get it working completely
    // For now it is better to just rotate a regular slider
    val committedValue = parseVolumePercent(volume)
    var sliderValue by remember(committedValue) { mutableFloatStateOf(committedValue ?: 0f) }

    BoxWithConstraints(
        modifier = Modifier
            .width(56.dp)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        val sliderLength = (maxHeight - 32.dp).coerceAtLeast(120.dp)
        Slider(
            enabled = volume != null,
            value = sliderValue,
            onValueChange = {
                sliderValue = it
                throttle.onDrag(it)
            },
            onValueChangeFinished = { throttle.onFinished(sliderValue) },
            valueRange = 0f..100f,
            modifier = Modifier
                .align(Alignment.Center)
                .requiredWidth(sliderLength)
                .graphicsLayer { rotationZ = -90f },
        )
    }
}

@Composable
private fun ActionButtons(
    onKeyEvent: (KeyEvent) -> Unit,
    host: Host?,
    editing: Boolean,
    isConnected: Boolean,
    muted: Boolean? = null,
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
            RemoteButton(RemoteControlKey.VOLUME_DOWN, onKeyEvent, host, editing, isConnected, buttonModifier) {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeDown,
                    contentDescription = stringResource(R.string.key_volume_down),
                )
            }
            RemoteButton(
                key = RemoteControlKey.MUTE,
                onKeyEvent = onKeyEvent,
                host = host,
                editing = editing,
                isConnected = isConnected,
                modifier = buttonModifier,
                colors = if (muted == true) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                } else {
                    ButtonDefaults.buttonColors()
                },
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeOff, contentDescription = stringResource(R.string.key_mute))
            }
            RemoteButton(RemoteControlKey.VOLUME_UP, onKeyEvent, host, editing, isConnected, buttonModifier) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = stringResource(R.string.key_volume_up))
            }
        }
        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RemoteButton(RemoteControlKey.BACK, onKeyEvent, host, editing, isConnected, buttonModifier) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.key_back))
            }
            RemoteButton(RemoteControlKey.HOME, onKeyEvent, host, editing, isConnected, buttonModifier) {
                Icon(Icons.Default.Home, contentDescription = stringResource(R.string.key_home))
            }
            RemoteButton(RemoteControlKey.MENU, onKeyEvent, host, editing, isConnected, buttonModifier) {
                Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.key_menu))
            }
        }
        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RemoteButton(RemoteControlKey.PREVIOUS, onKeyEvent, host, editing, isConnected, buttonModifier) {
                Icon(Icons.Default.SkipPrevious, contentDescription = stringResource(R.string.key_previous))
            }
            RemoteButton(RemoteControlKey.PLAY_PAUSE, onKeyEvent, host, editing, isConnected, buttonModifier) {
                Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.key_play_pause))
                Icon(Icons.Default.Pause, contentDescription = stringResource(R.string.key_play_pause))
            }
            RemoteButton(RemoteControlKey.NEXT, onKeyEvent, host, editing, isConnected, buttonModifier) {
                Icon(Icons.Default.SkipNext, contentDescription = stringResource(R.string.key_next))
            }
        }
    }
}

@Composable
private fun Dpad(
    onKeyEvent: (KeyEvent) -> Unit,
    host: Host?,
    editing: Boolean,
    isConnected: Boolean,
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
            RemoteControlKey.UP,
            onKeyEvent,
            host,
            editing,
            isConnected,
            directionalButtonModifier,
            ArcShape(225f, 90f),
            PaddingValues(0.dp),
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = stringResource(R.string.key_up),
                modifier = Modifier
                    .size(iconSize)
                    .offset(y = -iconOffset),
            )
        }

        // RIGHT
        RemoteButton(
            RemoteControlKey.RIGHT,
            onKeyEvent,
            host,
            editing,
            isConnected,
            directionalButtonModifier,
            ArcShape(315f, 90f),
            PaddingValues(0.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.key_right),
                modifier = Modifier
                    .size(iconSize)
                    .offset(x = iconOffset),
            )
        }

        // DOWN
        RemoteButton(
            RemoteControlKey.DOWN,
            onKeyEvent,
            host,
            editing,
            isConnected,
            directionalButtonModifier,
            ArcShape(45f, 90f),
            PaddingValues(0.dp),
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.key_down),
                modifier = Modifier
                    .size(iconSize)
                    .offset(y = iconOffset),
            )
        }

        // LEFT
        RemoteButton(
            RemoteControlKey.LEFT,
            onKeyEvent,
            host,
            editing,
            isConnected,
            directionalButtonModifier,
            ArcShape(135f, 90f),
            PaddingValues(0.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.key_left),
                modifier = Modifier
                    .size(iconSize)
                    .offset(x = -iconOffset),
            )
        }

        // CENTER
        if (host == null || host.remoteCommands == null || host.remoteCommands[RemoteControlKey.SELECT]?.command?.isEmpty() == false) {
            RemoteButton(
                RemoteControlKey.SELECT,
                onKeyEvent,
                host,
                editing,
                isConnected,
                Modifier.size(dpadSize / 2.8f),
                CircleShape,
                PaddingValues(0.dp),
                ButtonDefaults.buttonColors(
                    disabledContainerColor = Color.Transparent,
                ),
            ) {
                Icon(
                    Icons.Default.RadioButtonUnchecked,
                    contentDescription = stringResource(R.string.key_select),
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
    host: Host?,
    editing: Boolean,
    isConnected: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape = ButtonDefaults.shape,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    content: @Composable RowScope.() -> Unit,
) {
    val command = host?.remoteCommands?.get(key)

    RepeatingButton(
        onClick = { onKeyEvent(KeyEvent.Click(key)) },
        modifier = modifier,
        repeating = command?.repeat == true,
        onLongClick = if (command?.longPressCommand.isNullOrEmpty()) null else {
            { onKeyEvent(KeyEvent.LongPress(key)) }
        },
        enabled = editing || (isConnected && command?.command?.isNotEmpty() == true),
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

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun RemoteControlPreview_Portrait() {
    SSHRemoteTheme {
        Surface {
            RemoteControlLayout(
                RemoteLayoutMode.Portrait,
                {},
                host = sampleHost.copy(smartVolume = SmartVolumeSettings(true)),
                volume = "42%",
                muted = true,
                connectionStatus = ConnectionStatus.CONNECTED,
                onVolumeSet = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun RemoteControlPreview_Portrait_NoPreset() {
    SSHRemoteTheme {
        Surface {
            RemoteControlLayout(
                RemoteLayoutMode.Portrait,
                {},
                host = sampleHost.copy(remoteCommands = null),
                volume = "42%",
                muted = true,
                connectionStatus = ConnectionStatus.CONNECTED,
                onVolumeSet = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun RemoteControlPreview_Portrait_VLC() {
    SSHRemoteTheme {
        Surface {
            RemoteControlLayout(
                RemoteLayoutMode.Portrait,
                {},
                host = sampleHost.copy(remoteCommands = macosVlcPreset),
                connectionStatus = ConnectionStatus.CONNECTED,
                onVolumeSet = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Preview(
    showBackground = true,
    widthDp = 800,
    heightDp = 400,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun RemoteControlPreview_Landscape() {
    SSHRemoteTheme {
        Surface {
            RemoteControlLayout(
                RemoteLayoutMode.Landscape,
                {},
                host = sampleHost,
                volume = "42%",
                connectionStatus = ConnectionStatus.CONNECTED,
                onVolumeSet = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 400)
@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 400,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun RemoteControlPreview_Compact() {
    SSHRemoteTheme {
        Surface {
            RemoteControlLayout(
                RemoteLayoutMode.Compact,
                {},
                host = sampleHost,
                connectionStatus = ConnectionStatus.CONNECTED,
                onVolumeSet = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 400)
@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 400,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun RemoteControlPreview_Compact_WithoutSelect() {
    SSHRemoteTheme {
        Surface {
            RemoteControlLayout(
                RemoteLayoutMode.Compact,
                {},
                host = sampleHost.copy(remoteCommands = wtypePreset - RemoteControlKey.SELECT),
                connectionStatus = ConnectionStatus.CONNECTED,
                onVolumeSet = {},
            )
        }
    }
}
