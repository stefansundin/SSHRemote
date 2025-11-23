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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.data.host.StartScreen
import com.stefansundin.sshremote.data.host.cecClientPreset
import com.stefansundin.sshremote.data.host.macosVlcPreset
import com.stefansundin.sshremote.data.host.wtypePreset
import com.stefansundin.sshremote.data.host.xdotoolPreset
import com.stefansundin.sshremote.ui.KeyEvent
import com.stefansundin.sshremote.ui.components.EditCommandDialog
import com.stefansundin.sshremote.ui.components.EditMouseCommandsDialog
import com.stefansundin.sshremote.ui.components.RemoteControl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRemoteControlScreen(
    commands: Map<RemoteControlKey, Command>,
    onSave: (Map<RemoteControlKey, Command>, navigateBack: Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToEditCommands: () -> Unit,
    onSetAsDefaultScreen: (StartScreen) -> Unit,
) {
    val initialCommands = (if (commands.values.any { it.command.contains("wtype") }) {
        wtypePreset
    } else if (commands.values.any { it.command.contains("cec-client") }) {
        cecClientPreset
    } else if (commands.values.any { it.command.contains("osascript") }) {
        macosVlcPreset
    } else {
        xdotoolPreset
    }) + commands

    var editingCommand by remember { mutableStateOf<Triple<RemoteControlKey, String, Boolean>?>(null) }
    var editedCommands by remember { mutableStateOf(initialCommands) }
    val hasUnsavedChanges = editedCommands != initialCommands
    var showUnsavedBackDialog by remember { mutableStateOf(false) }
    var showUnsavedSwitchDialog by remember { mutableStateOf(false) }
    var activeMenu by remember { mutableStateOf("") } // "main" or "reset"
    var showResetDialog by remember { mutableStateOf(false) }
    var resetToPreset by remember { mutableStateOf("") }
    var showEditMouseCommandsDialog by remember { mutableStateOf(false) }

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
                    onClick = {
                        onSave(editedCommands, true)
                    },
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
                        onSave(editedCommands, false)
                        onNavigateToEditCommands()
                    },
                ) {
                    Text("Save and switch")
                }
            },
            dismissButton = {
                TextButton(onClick = onNavigateToEditCommands) {
                    Text("Discard and switch")
                }
            },
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset commands?") },
            text = { Text("This will reset all remote control commands to their default $resetToPreset values.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        editedCommands = when (resetToPreset) {
                            "wtype" -> wtypePreset
                            "cec-client" -> cecClientPreset
                            "macOS VLC" -> macosVlcPreset
                            else -> xdotoolPreset
                        }
                        showResetDialog = false
                    },
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showEditMouseCommandsDialog) {
        EditMouseCommandsDialog(
            commands = editedCommands,
            onDismiss = { showEditMouseCommandsDialog = false },
            onSave = {
                editedCommands = it
                showEditMouseCommandsDialog = false
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Remote Control") },
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
                                onNavigateToEditCommands()
                            }
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Edit commands")
                    }
                    Box {
                        IconButton(onClick = { activeMenu = "main" }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = activeMenu.isNotEmpty(),
                            onDismissRequest = { activeMenu = "" },
                        ) {
                            if (activeMenu == "main") {
                                DropdownMenuItem(
                                    text = { Text("Set as default screen") },
                                    onClick = {
                                        onSetAsDefaultScreen(StartScreen.REMOTE_CONTROL)
                                        activeMenu = ""
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Reset to preset") },
                                    onClick = { activeMenu = "reset" },
                                )
                            } else if (activeMenu == "reset") {
                                DropdownMenuItem(
                                    text = { Text("wtype") },
                                    onClick = {
                                        resetToPreset = "wtype"
                                        activeMenu = ""
                                        showResetDialog = true
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("xdotool") },
                                    onClick = {
                                        resetToPreset = "xdotool"
                                        activeMenu = ""
                                        showResetDialog = true
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("cec-client") },
                                    onClick = {
                                        resetToPreset = "cec-client"
                                        activeMenu = ""
                                        showResetDialog = true
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("macOS VLC") },
                                    onClick = {
                                        resetToPreset = "macOS VLC"
                                        activeMenu = ""
                                        showResetDialog = true
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onSave(editedCommands, true)
                },
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            RemoteControl(
                onKeyEvent = { event ->
                    if (event is KeyEvent.Click) {
                        val command = editedCommands[event.key] ?: Command("", event.key.title)
                        editingCommand = Triple(event.key, command.command, command.repeat)
                    }
                },
                onMouseEvent = {
                    showEditMouseCommandsDialog = true
                },
            )
        }

        editingCommand?.let { (key, command, repeat) ->
            EditCommandDialog(
                command = Triple(key, command, repeat),
                onDismiss = { editingCommand = null },
                onSave = { editedKey, newCommand, newRepeat ->
                    editedCommands = editedCommands.toMutableMap().apply {
                        this[editedKey] = (this[editedKey] ?: Command("", editedKey.title)).copy(
                            command = newCommand,
                            repeat = newRepeat,
                        )
                    }
                    editingCommand = null
                },
            )
        }
    }
}
