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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.data.host.RemoteControlScreen
import com.stefansundin.sshremote.data.host.SmartVolumeSettings
import com.stefansundin.sshremote.data.host.presets
import com.stefansundin.sshremote.ui.KeyEvent
import com.stefansundin.sshremote.ui.components.AddCommandShortcutDialog
import com.stefansundin.sshremote.ui.components.EditCommandDialog
import com.stefansundin.sshremote.ui.components.EditCommandsTab
import com.stefansundin.sshremote.ui.components.EditKeyboardCommandDialog
import com.stefansundin.sshremote.ui.components.EditMouseCommandsDialog
import com.stefansundin.sshremote.ui.components.EditRemoteCommandDialog
import com.stefansundin.sshremote.ui.components.MousePad
import com.stefansundin.sshremote.ui.components.RemoteControl
import com.stefansundin.sshremote.ui.components.ResponsiveTabRow
import com.stefansundin.sshremote.ui.components.ShareTargetSettingsDialog
import com.stefansundin.sshremote.ui.components.SmartVolumeSettingsDialog
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRemoteControlScreen(
    host: Host,
    onSave: (Map<RemoteControlKey, Command>, List<Command>, Boolean, SmartVolumeSettings?, navigateBack: Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onSetAsDefaultScreen: (RemoteControlScreen) -> Unit,
    onTestSmartVolumeSettings: () -> Unit,
    onAddCommandShortcut: (Host, String, String, Boolean) -> Unit,
    onAddRemoteCommandShortcut: (Host, RemoteControlKey) -> Unit,
    shareTargetEnabled: Boolean,
    initialPage: Int = 0,
) {
    var showEditCommandDialog by rememberSaveable(host.id) { mutableStateOf(false) }
    var showEditMouseCommandsDialog by rememberSaveable(host.id) { mutableStateOf(false) }
    var showEditKeyboardCommandDialog by rememberSaveable(host.id) { mutableStateOf(false) }
    var showSmartVolumeSettingsDialog by rememberSaveable(host.id) { mutableStateOf(false) }
    var showShareTargetSettingsDialog by rememberSaveable(host.id) { mutableStateOf(false) }

    // These values are not Bundle-saveable; keep them in composition memory to avoid parcel crashes.
    var editingCommand by remember(host.id) { mutableStateOf<Pair<RemoteControlKey, Command>?>(null) }
    var editedRemoteCommands by remember(host.id) { mutableStateOf(host.remoteCommands ?: emptyMap()) }
    var editedCommands by remember(host.id) { mutableStateOf(host.commands) }
    var editedShareInBackground by remember(host.id) { mutableStateOf(host.shareInBackground) }
    var editedSmartVolumeSettings by remember(host.id) { mutableStateOf(host.smartVolume) }
    var editingCommandInList by remember(host.id) { mutableStateOf<Command?>(null) }
    var pendingCommandShortcutId by rememberSaveable(host.id) { mutableStateOf<String?>(null) }

    val hasUnsavedChanges =
        editedRemoteCommands != (host.remoteCommands
            ?: emptyMap<RemoteControlKey, Command>()) || editedCommands != host.commands ||
                editedShareInBackground != host.shareInBackground || editedSmartVolumeSettings != host.smartVolume
    var showUnsavedBackDialog by rememberSaveable(host.id) { mutableStateOf(false) }
    var showMenu by rememberSaveable(host.id) { mutableStateOf(false) }
    var resetToPresetKey by rememberSaveable(host.id) { mutableStateOf("") }
    var showResetDialog by rememberSaveable(host.id) { mutableStateOf(false) }
    var showSelectPresetDialog by rememberSaveable(host.id) { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = initialPage) { 4 }
    val view = LocalView.current

    BackHandler(enabled = hasUnsavedChanges) {
        showUnsavedBackDialog = true
    }

    if (showUnsavedBackDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.unsaved_changes_title)) },
            text = { Text(stringResource(R.string.unsaved_changes_remote_control_text)) },
            properties = DialogProperties(dismissOnClickOutside = false),
            onDismissRequest = { showUnsavedBackDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onSave(
                            editedRemoteCommands,
                            editedCommands,
                            editedShareInBackground,
                            editedSmartVolumeSettings,
                            true,
                        )
                    },
                ) {
                    Text(stringResource(R.string.save_and_leave))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onNavigateBack()
                    },
                ) {
                    Text(stringResource(R.string.discard_and_leave))
                }
            },
        )
    }

    if (showResetDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.reset_commands_title)) },
            text = { Text(stringResource(R.string.reset_commands_text, resetToPresetKey)) },
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
                    Text(stringResource(R.string.reset))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        showResetDialog = false
                    },
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showSelectPresetDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.reset_to_preset_title)) },
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
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_remote_control), maxLines = 1) },
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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
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
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options))
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.set_as_default_tab)) },
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
                                text = { Text(stringResource(R.string.smart_volume_settings)) },
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    showMenu = false
                                    showSmartVolumeSettingsDialog = true
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.share_target_settings)) },
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    showMenu = false
                                    showShareTargetSettingsDialog = true
                                },
                            )
                            if (pagerState.currentPage != 3) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.reset_to_preset_title)) },
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
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_command))
                    }
                }
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.save)) },
                    icon = { Icon(Icons.Default.Save, contentDescription = stringResource(R.string.save)) },
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onSave(
                            editedRemoteCommands,
                            editedCommands,
                            editedShareInBackground,
                            editedSmartVolumeSettings,
                            true,
                        )
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
            val tabTitles = listOf(
                stringResource(R.string.tab_remote),
                stringResource(R.string.tab_mouse),
                stringResource(R.string.tab_keyboard),
                stringResource(R.string.tab_commands),
            )

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
                                        val command = editedRemoteCommands[event.key] ?: Command("")
                                        editingCommand = event.key to command
                                    }
                                },
                                editing = true,
                                host = null,
                                onVolumeSet = {},
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
                                Text(stringResource(R.string.edit_keyboard_commands))
                            }
                        }
                    }

                    3 -> {
                        EditCommandsTab(
                            commands = editedCommands,
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
            command = editingCommandInList,
            onDismiss = {
                showEditCommandDialog = false
                editingCommandInList = null
            },
            onSave = { command ->
                val editingId = editingCommandInList?.id
                editedCommands = if (editingId != null) {
                    editedCommands.map { if (it.id == editingId) command else it }
                } else {
                    editedCommands + command
                }
            },
            onAddToHomeScreen = { commandId ->
                pendingCommandShortcutId = commandId
            },
        )
    }

    pendingCommandShortcutId?.let { commandId ->
        val commandLabel = editedCommands.find { it.id == commandId }?.let { it.name ?: it.command }
            ?: stringResource(R.string.command)

        AddCommandShortcutDialog(
            initialShortcutLabel = stringResource(R.string.run_command_on_host_short, commandLabel, host.name),
            onDismiss = {
                pendingCommandShortcutId = null
            },
            onConfirm = { shortcutLabel, runInBackground ->
                onAddCommandShortcut(
                    host.copy(
                        commands = editedCommands,
                        remoteCommands = editedRemoteCommands,
                        shareInBackground = editedShareInBackground,
                        smartVolume = editedSmartVolumeSettings,
                    ),
                    commandId,
                    shortcutLabel,
                    runInBackground,
                )
                pendingCommandShortcutId = null
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

    if (showShareTargetSettingsDialog) {
        if (shareTargetEnabled) {
            ShareTargetSettingsDialog(
                initialRemoteCommands = editedRemoteCommands,
                initialShareInBackground = editedShareInBackground,
                onDismiss = { showShareTargetSettingsDialog = false },
                onSave = { remoteCommands, shareInBackground ->
                    editedRemoteCommands = remoteCommands
                    editedShareInBackground = shareInBackground
                    showShareTargetSettingsDialog = false
                },
            )
        } else {
            AlertDialog(
                title = { Text(stringResource(R.string.share_target_disabled_title)) },
                text = { Text(stringResource(R.string.share_target_disabled_text)) },
                onDismissRequest = { showShareTargetSettingsDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            showShareTargetSettingsDialog = false
                        },
                    ) {
                        Text(stringResource(R.string.ok))
                    }
                },
            )
        }
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
            },
            onAddToHomeScreen = { key ->
                onAddRemoteCommandShortcut(
                    host.copy(
                        commands = editedCommands,
                        remoteCommands = editedRemoteCommands,
                        shareInBackground = editedShareInBackground,
                        smartVolume = editedSmartVolumeSettings,
                    ),
                    key,
                )
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
            onSave = { _, _, _, _, _ -> },
            onNavigateBack = {},
            onSetAsDefaultScreen = {},
            onTestSmartVolumeSettings = {},
            onAddCommandShortcut = { _, _, _, _ -> },
            onAddRemoteCommandShortcut = { _, _ -> },
            shareTargetEnabled = true,
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
            onSave = { _, _, _, _, _ -> },
            onNavigateBack = {},
            onSetAsDefaultScreen = {},
            onTestSmartVolumeSettings = {},
            onAddCommandShortcut = { _, _, _, _ -> },
            onAddRemoteCommandShortcut = { _, _ -> },
            shareTargetEnabled = true,
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
            onSave = { _, _, _, _, _ -> },
            onNavigateBack = {},
            onSetAsDefaultScreen = {},
            onTestSmartVolumeSettings = {},
            onAddCommandShortcut = { _, _, _, _ -> },
            onAddRemoteCommandShortcut = { _, _ -> },
            shareTargetEnabled = true,
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
            onSave = { _, _, _, _, _ -> },
            onNavigateBack = {},
            onSetAsDefaultScreen = {},
            onTestSmartVolumeSettings = {},
            onAddCommandShortcut = { _, _, _, _ -> },
            onAddRemoteCommandShortcut = { _, _ -> },
            shareTargetEnabled = true,
            initialPage = 3,
        )
    }
}
