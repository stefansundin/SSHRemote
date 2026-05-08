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
import android.view.SoundEffectConstants
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun EditCommandsTab(
    commands: List<Command>,
    onCommandsChanged: (List<Command>) -> Unit,
    onEditCommand: (Command) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val commandsListState = rememberLazyListState()
    var undoableDeletedCommand by rememberSaveable { mutableStateOf<Pair<Int, Command>?>(null) }
    val view = LocalView.current
    val hapticFeedback = LocalHapticFeedback.current
    val reorderableLazyListState = rememberReorderableLazyListState(commandsListState) { from, to ->
        val newCommands = commands.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        onCommandsChanged(newCommands)
    }

    val commandDeletedMsg = stringResource(R.string.command_deleted)
    val undoLabel = stringResource(R.string.undo)

    LaunchedEffect(undoableDeletedCommand) {
        val deletedCommand = undoableDeletedCommand
        if (deletedCommand != null) {
            val (index, command) = deletedCommand
            val result = snackbarHostState.showSnackbar(
                message = commandDeletedMsg,
                actionLabel = undoLabel,
            )
            if (result == SnackbarResult.ActionPerformed) {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                val currentCommands = commands.toMutableList()
                currentCommands.add(index, command)
                onCommandsChanged(currentCommands)
            } else {
                undoableDeletedCommand = null
            }
        }
    }

    LaunchedEffect(commands, undoableDeletedCommand) {
        val deletedCommand = undoableDeletedCommand
        if (deletedCommand != null) {
            val (index, command) = deletedCommand
            if (index < commands.size && commands[index].id == command.id) {
                commandsListState.animateScrollToItem(index)
                undoableDeletedCommand = null
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = commandsListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 160.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(commands, key = { it.id }) { command ->
                ReorderableItem(reorderableLazyListState, key = command.id) {
                    val interactionSource = remember { MutableInteractionSource() }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            modifier = Modifier
                                .draggableHandle(
                                    onDragStarted = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                    },
                                    onDragStopped = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                    },
                                    interactionSource = interactionSource,
                                )
                                .clearAndSetSemantics { },
                            onClick = {},
                        ) {
                            Icon(Icons.Rounded.DragHandle, contentDescription = stringResource(R.string.reorder))
                        }
                        Text(
                            text = command.name ?: command.command,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                onEditCommand(command)
                            },
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                        }
                        IconButton(
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                val deletedCommandIndex = commands.indexOfFirst { it.id == command.id }
                                if (deletedCommandIndex >= 0) {
                                    val newCommands = commands.toMutableList().apply { removeAt(deletedCommandIndex) }
                                    onCommandsChanged(newCommands)
                                    undoableDeletedCommand = deletedCommandIndex to command
                                }
                            },
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 160.dp),
        )
    }
}

val sampleCommands = listOf(
    Command("uptime", name = "Uptime"),
    Command("whoami"),
)

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun EditCommandsTabPreview() {
    SSHRemoteTheme {
        Surface {
            EditCommandsTab(
                commands = sampleCommands,
                onCommandsChanged = {},
                onEditCommand = {},
            )
        }
    }
}
