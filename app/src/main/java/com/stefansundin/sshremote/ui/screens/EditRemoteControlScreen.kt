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

package com.stefansundin.sshremote.ui.screens

import android.content.res.Configuration
import android.view.SoundEffectConstants
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.CommandItem
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.data.host.RemoteControlScreen
import com.stefansundin.sshremote.data.host.SmartVolumeSettings
import com.stefansundin.sshremote.data.host.presets
import com.stefansundin.sshremote.data.host.toItem
import com.stefansundin.sshremote.ui.KeyEvent
import com.stefansundin.sshremote.ui.components.EditCommandDialog
import com.stefansundin.sshremote.ui.components.EditCommandsTab
import com.stefansundin.sshremote.ui.components.EditKeyboardCommandDialog
import com.stefansundin.sshremote.ui.components.EditMouseCommandsDialog
import com.stefansundin.sshremote.ui.components.EditRemoteCommandDialog
import com.stefansundin.sshremote.ui.components.MousePad
import com.stefansundin.sshremote.ui.components.RemoteControl
import com.stefansundin.sshremote.ui.components.ResponsiveTabRow
import com.stefansundin.sshremote.ui.components.SmartVolumeSettingsDialog
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRemoteControlScreen(
    host: Host,
    onSave: (Map<RemoteControlKey, Command>, List<Command>, SmartVolumeSettings?, navigateBack: Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onSetAsDefaultScreen: (RemoteControlScreen) -> Unit,
    onTestSmartVolumeSettings: () -> Unit,
    initialPage: Int = 0,
) {
    var editingCommand by rememberSaveable { mutableStateOf<Pair<RemoteControlKey, Command>?>(null) }
    var editedRemoteCommands by rememberSaveable { mutableStateOf(host.remoteCommands ?: emptyMap()) }
    var editedCommands by rememberSaveable { mutableStateOf(host.commands.map { it.toItem() }) }
    var editedSmartVolumeSettings by rememberSaveable { mutableStateOf(host.smartVolume) }
    var showEditCommandDialog by rememberSaveable { mutableStateOf(false) }
    var editingCommandInList by rememberSaveable { mutableStateOf<CommandItem?>(null) }
    var showEditMouseCommandsDialog by rememberSaveable { mutableStateOf(false) }
    var showEditKeyboardCommandDialog by rememberSaveable { mutableStateOf(false) }
    var showSmartVolumeSettingsDialog by rememberSaveable { mutableStateOf(false) }

    val hasUnsavedChanges =
        editedRemoteCommands != (host.remoteCommands
            ?: emptyMap<RemoteControlKey, Command>()) || editedCommands.map { it.command } != host.commands || editedSmartVolumeSettings != host.smartVolume
    var showUnsavedBackDialog by rememberSaveable { mutableStateOf(false) }
    var showMenu by rememberSaveable { mutableStateOf(false) }
    var resetToPresetKey by rememberSaveable { mutableStateOf("") }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    var showSelectPresetDialog by rememberSaveable { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = initialPage) { 4 }
    val view = LocalView.current

    BackHandler(enabled = hasUnsavedChanges) {
        showUnsavedBackDialog = true
    }

    if (showUnsavedBackDialog) {
        AlertDialog(
            title = { Text("Unsaved changes") },
            text = { Text("Do you want to save your changes before leaving?") },
            properties = DialogProperties(dismissOnClickOutside = false),
            onDismissRequest = { showUnsavedBackDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onSave(editedRemoteCommands, editedCommands.map { it.command }, editedSmartVolumeSettings, true)
                    },
                ) {
                    Text("Save and leave")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onNavigateBack()
                    },
                ) {
                    Text("Discard and leave")
                }
            },
        )
    }

    if (showResetDialog) {
        AlertDialog(
            title = { Text("Reset commands?") },
            text = { Text("This will reset all remote control commands to their default $resetToPresetKey values.") },
            onDismissRequest = { showResetDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        editedRemoteCommands =
                            presets[resetToPresetKey]
                                ?: throw IllegalStateException("Preset '$resetToPresetKey' not found")
                        showResetDialog = false
                    },
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        showResetDialog = false
                    },
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showSelectPresetDialog) {
        AlertDialog(
            title = { Text("Reset to preset") },
            text = {
                Column {
                    presets.keys.forEach { presetKey ->
                        Text(
                            text = presetKey,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    resetToPresetKey = presetKey
                                    showSelectPresetDialog = false
                                    showResetDialog = true
                                }
                                .padding(vertical = 12.dp),
                        )
                    }
                }
            },
            onDismissRequest = { showSelectPresetDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        showSelectPresetDialog = false
                    },
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Remote Control", maxLines = 1) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
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
                    Box {
                        IconButton(
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                showMenu = true
                            },
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Set as default tab") },
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    val startScreen =
                                        RemoteControlScreen.entries.find { it.tabIndex == pagerState.currentPage }
                                            ?: throw IllegalStateException("Could not find RemoteControlScreen for tab index ${pagerState.currentPage}")
                                    onSetAsDefaultScreen(startScreen)
                                    showMenu = false
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Smart volume settings") },
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    showMenu = false
                                    showSmartVolumeSettingsDialog = true
                                },
                            )
                            if (pagerState.currentPage != 3) {
                                DropdownMenuItem(
                                    text = { Text("Reset to preset") },
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        showMenu = false
                                        showSelectPresetDialog = true
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (pagerState.currentPage == 3) {
                    FloatingActionButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            editingCommandInList = null
                            showEditCommandDialog = true
                        },
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add command")
                    }
                }
                ExtendedFloatingActionButton(
                    text = { Text("Save") },
                    icon = { Icon(Icons.Default.Save, contentDescription = "Save") },
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onSave(editedRemoteCommands, editedCommands.map { it.command }, editedSmartVolumeSettings, true)
                    },
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            val tabTitles = listOf("Remote", "Mouse", "Keyboard", "Commands")

            ResponsiveTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 0.dp,
            ) {
                tabTitles.forEachIndexed { index, title ->
                    key(index) {
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                coroutineScope.launch { pagerState.scrollToPage(index) }
                            },
                            text = { Text(text = title, maxLines = 1) },
                        )
                    }
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true,
            ) { page ->
                when (page) {
                    0 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            RemoteControl(
                                onKeyEvent = { event ->
                                    if (event is KeyEvent.Click) {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        val command =
                                            editedRemoteCommands[event.key] ?: Command("", name = event.key.title)
                                        editingCommand = event.key to command
                                    }
                                },
                                editing = true,
                                host = null,
                            )
                        }
                    }

                    1 -> {
                        MousePad(
                            onMouseEvent = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                showEditMouseCommandsDialog = true
                            },
                            editing = true,
                        )
                    }

                    2 -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        ) {
                            Button(
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    showEditKeyboardCommandDialog = true
                                },
                            ) {
                                Text("Edit keyboard commands")
                            }
                        }
                    }

                    3 -> {
                        EditCommandsTab(
                            commandItems = editedCommands,
                            onCommandsChanged = { editedCommands = it },
                            onEditCommand = {
                                editingCommandInList = it
                                showEditCommandDialog = true
                            },
                        )
                    }
                }
            }
        }
    }

    if (showEditCommandDialog) {
        EditCommandDialog(
            commandItem = editingCommandInList,
            onDismiss = {
                showEditCommandDialog = false
                editingCommandInList = null
            },
            onSave = { commandItem ->
                editedCommands = if (editingCommandInList != null) {
                    editedCommands.map { if (it == editingCommandInList) commandItem else it }
                } else {
                    editedCommands + commandItem
                }
                showEditCommandDialog = false
                editingCommandInList = null
            },
        )
    }

    if (showEditMouseCommandsDialog) {
        EditMouseCommandsDialog(
            initialRemoteCommands = editedRemoteCommands,
            onDismiss = { showEditMouseCommandsDialog = false },
            onSave = {
                editedRemoteCommands = it
                showEditMouseCommandsDialog = false
            },
        )
    }

    if (showEditKeyboardCommandDialog) {
        EditKeyboardCommandDialog(
            initialRemoteCommands = editedRemoteCommands,
            onDismiss = { showEditKeyboardCommandDialog = false },
            onSave = {
                editedRemoteCommands = it
                showEditKeyboardCommandDialog = false
            },
        )
    }

    if (showSmartVolumeSettingsDialog) {
        SmartVolumeSettingsDialog(
            settings = editedSmartVolumeSettings,
            onDismiss = { showSmartVolumeSettingsDialog = false },
            onSave = {
                editedSmartVolumeSettings = it
                showSmartVolumeSettingsDialog = false
            },
            onTest = onTestSmartVolumeSettings,
        )
    }

    editingCommand?.let { command ->
        EditRemoteCommandDialog(
            command = command,
            onDismiss = { editingCommand = null },
            onSave = { key, newCommand ->
                editedRemoteCommands = editedRemoteCommands.toMutableMap().apply {
                    this[key] = newCommand
                }
                editingCommand = null
            },
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun EditRemoteControlScreenPreview_RemoteTab() {
    SSHRemoteTheme {
        EditRemoteControlScreen(
            host = sampleHost,
            onSave = { _, _, _, _ -> },
            onNavigateBack = {},
            onSetAsDefaultScreen = {},
            onTestSmartVolumeSettings = {},
            initialPage = 0,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun EditRemoteControlScreenPreview_MouseTab() {
    SSHRemoteTheme {
        EditRemoteControlScreen(
            host = sampleHost,
            onSave = { _, _, _, _ -> },
            onNavigateBack = {},
            onSetAsDefaultScreen = {},
            onTestSmartVolumeSettings = {},
            initialPage = 1,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun EditRemoteControlScreenPreview_KeyboardTab() {
    SSHRemoteTheme {
        EditRemoteControlScreen(
            host = sampleHost,
            onSave = { _, _, _, _ -> },
            onNavigateBack = {},
            onSetAsDefaultScreen = {},
            onTestSmartVolumeSettings = {},
            initialPage = 2,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun EditRemoteControlScreenPreview_CommandsTab() {
    SSHRemoteTheme {
        EditRemoteControlScreen(
            host = sampleHost,
            onSave = { _, _, _, _ -> },
            onNavigateBack = {},
            onSetAsDefaultScreen = {},
            onTestSmartVolumeSettings = {},
            initialPage = 3,
        )
    }
}
