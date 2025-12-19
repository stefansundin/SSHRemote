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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommand
import com.stefansundin.sshremote.data.host.RemoteUiState
import com.stefansundin.sshremote.ui.components.CommandOutputDialog
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AdHocCommandScreen(
    uiState: RemoteUiState,
    commands: List<AdHocCommand>,
    onExecuteCommand: (command: String, popUpToPrevious: Boolean) -> Unit,
    onDeleteCommand: (AdHocCommand) -> Unit,
    onClearHistory: () -> Unit,
    onNavigateUp: () -> Unit,
    onClearCommandOutput: () -> Unit,
) {
    var commandText by rememberSaveable { mutableStateOf("") }
    var showMenu by rememberSaveable { mutableStateOf(false) }
    var showContextMenuFor by rememberSaveable { mutableStateOf<AdHocCommand?>(null) }

    val executeAndStay: () -> Unit = { onExecuteCommand(commandText, false) }
    val executeAndGoBack: () -> Unit = { onExecuteCommand(commandText, true) }

    uiState.commandOutput?.let { output ->
        CommandOutputDialog(
            output = output,
            onDismiss = onClearCommandOutput,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ad-Hoc Command") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Clear history", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    onClearHistory()
                                    showMenu = false
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .imePadding(),
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(commands, key = { it.command }) { command ->
                    Box {
                        Text(
                            text = command.command,
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        if (commandText == command.command) {
                                            executeAndStay()
                                        } else {
                                            commandText = command.command
                                        }
                                    },
                                    onLongClick = {
                                        showContextMenuFor = command
                                    },
                                )
                                .padding(16.dp),
                        )
                        DropdownMenu(
                            expanded = showContextMenuFor == command,
                            onDismissRequest = { showContextMenuFor = null },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    onDeleteCommand(command)
                                    showContextMenuFor = null
                                },
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TextField(
                    value = commandText,
                    onValueChange = { commandText = it },
                    label = { Text("Command") },
                    modifier = Modifier
                        .weight(1f)
                        .onPreviewKeyEvent {
                            if (it.type == KeyEventType.KeyDown && it.key == Key.Enter) {
                                if (it.isShiftPressed || it.isCtrlPressed) {
                                    executeAndGoBack()
                                } else {
                                    executeAndStay()
                                }
                                true
                            } else {
                                false
                            }
                        },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { executeAndStay() }),
                    trailingIcon = {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                    },
                )
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Execute",
                    modifier = Modifier.combinedClickable(
                        onClick = executeAndStay,
                        onLongClick = executeAndGoBack,
                    ),
                )
            }
        }
    }
}

@Preview(name = "Normal Font")
@Preview(fontScale = 2.0f, name = "Large Font")
@Composable
private fun AdHocCommandScreenPreview() {
    SSHRemoteTheme {
        AdHocCommandScreen(
            uiState = RemoteUiState(),
            commands = listOf(
                AdHocCommand("ls -la"),
                AdHocCommand("uptime"),
            ),
            onExecuteCommand = { _, _ -> },
            onDeleteCommand = {},
            onClearHistory = {},
            onNavigateUp = {},
            onClearCommandOutput = {},
        )
    }
}
