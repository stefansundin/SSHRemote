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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.StartScreen
import com.stefansundin.sshremote.ui.components.EditCommandDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCommandsScreen(
    commands: List<Command>,
    onSave: (List<Command>, navigateBack: Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToEditRemoteControl: () -> Unit,
    onSetAsDefaultScreen: (StartScreen) -> Unit,
) {
    var editingCommands by remember { mutableStateOf(commands) }
    var showDialog by remember { mutableStateOf(false) }
    var editingCommand by remember { mutableStateOf<Command?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val hasUnsavedChanges = editingCommands != commands
    var showUnsavedBackDialog by remember { mutableStateOf(false) }
    var showUnsavedSwitchDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    BackHandler(enabled = hasUnsavedChanges) {
        showUnsavedBackDialog = true
    }

    if (showUnsavedBackDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedBackDialog = false },
            title = { Text("Unsaved changes") },
            text = { Text("Do you want to save your changes before leaving?") },
            confirmButton = {
                TextButton(
                    onClick = { onSave(editingCommands, true) },
                ) {
                    Text("Save and leave")
                }
            },
            dismissButton = {
                TextButton(onClick = onNavigateBack) {
                    Text("Discard and leave")
                }
            },
        )
    }

    if (showUnsavedSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedSwitchDialog = false },
            title = { Text("Unsaved changes") },
            text = { Text("Do you want to save your changes before switching?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSave(editingCommands, false)
                        onNavigateToEditRemoteControl()
                    },
                ) {
                    Text("Save and switch")
                }
            },
            dismissButton = {
                TextButton(onClick = onNavigateToEditRemoteControl) {
                    Text("Discard and switch")
                }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Commands") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (hasUnsavedChanges) {
                                showUnsavedBackDialog = true
                            } else {
                                onNavigateBack()
                            }
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (hasUnsavedChanges) {
                                showUnsavedSwitchDialog = true
                            } else {
                                onNavigateToEditRemoteControl()
                            }
                        },
                    ) {
                        Icon(Icons.Default.SettingsRemote, contentDescription = "Edit remote control")
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Set as default screen") },
                                onClick = {
                                    onSetAsDefaultScreen(StartScreen.COMMAND_LIST)
                                    showMenu = false
                                },
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                ExtendedFloatingActionButton(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add command") },
                    text = { Text("Add") },
                    onClick = { showDialog = true },
                )
                FloatingActionButton(
                    modifier = Modifier.padding(top = 16.dp),
                    onClick = {
                        onSave(editingCommands, true)
                    },
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save")
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(editingCommands) { command ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = command.name ?: command.command, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            editingCommand = command
                            showDialog = true
                        },
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit command")
                    }
                    IconButton(
                        onClick = {
                            val deletedCommandIndex = editingCommands.indexOf(command)
                            editingCommands = editingCommands.filter { it != command }
                            snackbarHostState.currentSnackbarData?.dismiss()
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Command deleted",
                                    actionLabel = "Undo",
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    val currentCommands = editingCommands.toMutableList()
                                    currentCommands.add(deletedCommandIndex, command)
                                    editingCommands = currentCommands
                                }
                            }
                        },
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete command",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        EditCommandDialog(
            command = editingCommand,
            onDismiss = {
                showDialog = false
                editingCommand = null
            },
            onSave = { command ->
                editingCommands = if (editingCommand != null) {
                    editingCommands.map { if (it == editingCommand) command else it }
                } else {
                    editingCommands + command
                }
                showDialog = false
                editingCommand = null
            },
        )
    }
}
