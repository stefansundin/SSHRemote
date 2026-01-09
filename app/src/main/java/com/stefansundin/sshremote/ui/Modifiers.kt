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

package com.stefansundin.sshremote.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.KeyInputModifierNode
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

/**
 * This modifier makes it possible to navigate away from text inputs on Android TV.
 */
fun Modifier.dpadFocusable(): Modifier = this.then(DpadFocusableElement)

private data object DpadFocusableElement : ModifierNodeElement<DpadFocusableNode>() {
    override fun create() = DpadFocusableNode()

    override fun update(node: DpadFocusableNode) {}

    override fun InspectorInfo.inspectableProperties() {
        name = "dpadFocusable"
    }
}

private class DpadFocusableNode : Modifier.Node(), KeyInputModifierNode, CompositionLocalConsumerModifierNode {
    override fun onPreKeyEvent(event: KeyEvent): Boolean {
        if (event.type == KeyEventType.KeyDown) {
            val focusManager = currentValueOf(LocalFocusManager)
            return when (event.key) {
                Key.DirectionUp -> {
                    focusManager.moveFocus(FocusDirection.Up)
                }

                Key.DirectionDown -> {
                    focusManager.moveFocus(FocusDirection.Down)
                }

                Key.DirectionCenter -> {
                    val keyboardController = currentValueOf(LocalSoftwareKeyboardController)
                    if (keyboardController != null) {
                        keyboardController.show()
                        true
                    } else {
                        false
                    }
                }

                else -> false
            }
        }
        return false
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        return false
    }
}
