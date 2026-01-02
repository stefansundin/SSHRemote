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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.stefansundin.sshremote.data.host.ConnectionStatus
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.RemoteControlKey

@Composable
fun KeyboardInput(
    isCurrentlySelected: Boolean,
    onKey: (String) -> Unit,
    onType: (String) -> Unit,
    modifier: Modifier = Modifier,
    host: Host? = null,
    connectionStatus: ConnectionStatus? = null,
) {
    val keyboardConfigured = host == null ||
            (host.remoteCommands != null &&
                    (!host.remoteCommands[RemoteControlKey.KEYBOARD_KEY_INPUT]?.command.isNullOrEmpty() ||
                            !host.remoteCommands[RemoteControlKey.KEYBOARD_TYPE_INPUT]?.command.isNullOrEmpty()))
    val isEnabled = connectionStatus == ConnectionStatus.CONNECTED && keyboardConfigured
    val zeroWidthSpace = "\u200B"
    var text by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(zeroWidthSpace, selection = TextRange(zeroWidthSpace.length)))
    }
    val focusRequester = remember { FocusRequester() }

    // This TextField is used to bring up the keyboard and capture the input.
    // The text field itself is not the primary way of handling input, the onKeyEvent modifier is.
    TextField(
        value = if (keyboardConfigured) text else TextFieldValue("Keyboard commands not configured"),
        enabled = isEnabled,
        keyboardOptions = KeyboardOptions(autoCorrectEnabled = false),
        onValueChange = {
            val previousText = text.text
            val newText = it.text

            if (newText.isEmpty()) {
                // The user deleted the zero-width space, which is our signal for a backspace
                onKey("BackSpace")
                // Keep the zero-width space in the field to detect the next backspace
                text = TextFieldValue(zeroWidthSpace, TextRange(zeroWidthSpace.length))
                return@TextField
            }

            if (newText.length > previousText.length) {
                // Detect and override the software keyboard's double-space to period-space feature:
                if (newText.endsWith(". ") && previousText.endsWith(" ") && newText.length == previousText.length + 1) {
                    onType(" ")
                    val correctedText = "$previousText "
                    text = TextFieldValue(correctedText, TextRange(correctedText.length))
                    return@TextField
                }

                val typed = if (previousText == zeroWidthSpace) {
                    newText.substring(1)
                } else {
                    newText.substring(previousText.length)
                }
                onType(typed)
            } else if (newText.length < previousText.length) {
                val deletedChars = previousText.length - newText.length
                repeat(deletedChars) {
                    onKey("BackSpace")
                }
            }
            text = if (previousText == zeroWidthSpace) {
                val typedText = newText.substring(1)
                TextFieldValue(typedText, TextRange(typedText.length))
            } else {
                // Block moving of the cursor position:
                it.copy(selection = TextRange(it.text.length))
            }
        },
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester),
    )

    // Bring up the virtual keyboard when the Keyboard tab is focused:
    LaunchedEffect(isCurrentlySelected, isEnabled) {
        if (isCurrentlySelected && isEnabled) {
            focusRequester.requestFocus()
        }
    }
}
