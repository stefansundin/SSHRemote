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
import android.view.KeyEvent
import android.view.SoundEffectConstants
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.data.host.ConnectionStatus
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private data class KeyData(
    val label: String,
    val keyCode: Int,
    val icon: ImageVector? = null,
)

@Composable
fun SpecialKeysRow(
    onKey: (Int) -> Unit,
    onKeyDown: (Int) -> Unit,
    onKeyUp: (Int) -> Unit,
    pressedKeys: Set<Int>,
    modifier: Modifier = Modifier,
    host: Host? = null,
    connectionStatus: ConnectionStatus? = null,
) {
    val keyboardConfigured = host == null ||
            (host.remoteCommands != null && !host.remoteCommands[RemoteControlKey.KEYBOARD_KEY_INPUT]?.command.isNullOrEmpty())
    val isEnabled = connectionStatus == ConnectionStatus.CONNECTED && keyboardConfigured

    val modifiers = setOf(
        KeyEvent.KEYCODE_SHIFT_LEFT,
        KeyEvent.KEYCODE_CTRL_LEFT,
        KeyEvent.KEYCODE_META_LEFT,
        KeyEvent.KEYCODE_ALT_LEFT,
    )

    val repeatableKeys = setOf(
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_LEFT,
        KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_PAGE_UP,
        KeyEvent.KEYCODE_PAGE_DOWN,
        KeyEvent.KEYCODE_FORWARD_DEL,
        KeyEvent.KEYCODE_TAB,
    )

    val view = LocalView.current

    val runKey = { keyData: KeyData ->
        view.playSoundEffect(SoundEffectConstants.CLICK)
        if (modifiers.contains(keyData.keyCode)) {
            if (pressedKeys.contains(keyData.keyCode)) {
                onKeyUp(keyData.keyCode)
            } else {
                onKeyDown(keyData.keyCode)
            }
        } else {
            onKey(keyData.keyCode)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // First row: Main actions and Modifiers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val row1 = listOf(
                KeyData(stringResource(R.string.key_esc), KeyEvent.KEYCODE_ESCAPE),
                KeyData(stringResource(R.string.key_tab), KeyEvent.KEYCODE_TAB),
                KeyData(stringResource(R.string.key_ctrl), KeyEvent.KEYCODE_CTRL_LEFT),
                KeyData(stringResource(R.string.key_alt), KeyEvent.KEYCODE_ALT_LEFT),
                KeyData(stringResource(R.string.key_shift), KeyEvent.KEYCODE_SHIFT_LEFT),
                KeyData(stringResource(R.string.key_super), KeyEvent.KEYCODE_META_LEFT),
                KeyData(stringResource(R.string.key_caps), KeyEvent.KEYCODE_CAPS_LOCK),
            )
            row1.forEach { keyData ->
                KeyButton(
                    keyData = keyData,
                    isPressed = pressedKeys.contains(keyData.keyCode),
                    enabled = isEnabled,
                    onClick = { runKey(keyData) },
                    repeatable = repeatableKeys.contains(keyData.keyCode),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Second row: Navigation and Secondary Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val row2 = listOf(
                KeyData(stringResource(R.string.key_del), KeyEvent.KEYCODE_FORWARD_DEL),
                KeyData(stringResource(R.string.key_home), KeyEvent.KEYCODE_MOVE_HOME),
                KeyData(stringResource(R.string.key_end), KeyEvent.KEYCODE_MOVE_END),
                KeyData(stringResource(R.string.key_pageup), KeyEvent.KEYCODE_PAGE_UP),
                KeyData(stringResource(R.string.key_pagedown), KeyEvent.KEYCODE_PAGE_DOWN),
                KeyData(stringResource(R.string.key_left), KeyEvent.KEYCODE_DPAD_LEFT, Icons.AutoMirrored.Filled.ArrowBack),
                KeyData(stringResource(R.string.key_up), KeyEvent.KEYCODE_DPAD_UP, Icons.Default.ArrowUpward),
                KeyData(stringResource(R.string.key_down), KeyEvent.KEYCODE_DPAD_DOWN, Icons.Default.ArrowDownward),
                KeyData(stringResource(R.string.key_right), KeyEvent.KEYCODE_DPAD_RIGHT, Icons.AutoMirrored.Filled.ArrowForward),
            )
            row2.forEach { keyData ->
                KeyButton(
                    keyData = keyData,
                    isPressed = pressedKeys.contains(keyData.keyCode),
                    enabled = isEnabled,
                    onClick = { runKey(keyData) },
                    repeatable = repeatableKeys.contains(keyData.keyCode),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun KeyButton(
    keyData: KeyData,
    isPressed: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    repeatable: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHeld by interactionSource.collectIsPressedAsState()

    if (repeatable && enabled && isHeld) {
        LaunchedEffect(keyData.keyCode) {
            onClick()
            delay(500.milliseconds)
            while (true) {
                onClick()
                delay(100.milliseconds)
            }
        }
    }

    val containerColor = if (isPressed || isHeld) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (isPressed || isHeld) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = { if (!repeatable) onClick() },
        enabled = enabled,
        shape = RoundedCornerShape(4.dp),
        color = containerColor,
        contentColor = contentColor,
        interactionSource = interactionSource,
        modifier = modifier.height(36.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            if (keyData.icon != null) {
                Icon(
                    imageVector = keyData.icon,
                    contentDescription = keyData.label,
                    modifier = Modifier.size(18.dp),
                )
            } else {
                Text(
                    text = keyData.label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun SpecialKeysRowPreview() {
    SSHRemoteTheme {
        Surface {
            SpecialKeysRow(
                onKey = {},
                onKeyDown = {},
                onKeyUp = {},
                pressedKeys = emptySet(),
                connectionStatus = ConnectionStatus.CONNECTED,
            )
        }
    }
}
