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
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommand
import com.stefansundin.sshremote.data.host.RemoteUiState
import com.stefansundin.sshremote.ui.components.CommandOutputDialog
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme

@OptIn(ExperimentalMaterial3Api::class)
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
    val view = LocalView.current

    val executeAndStay: () -> Unit = {
        view.playSoundEffect(SoundEffectConstants.CLICK)
        onExecuteCommand(commandText, false)
    }
    val executeAndGoBack: () -> Unit = {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        onExecuteCommand(commandText, true)
    }

    uiState.commandOutput?.let { output ->
        CommandOutputDialog(
            output = output,
            onDismiss = onClearCommandOutput,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ad_hoc_command_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            onNavigateUp()
                        },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.key_back),
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
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more))
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(R.string.clear_history),
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                },
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
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
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        if (commandText == command.command) {
                                            executeAndStay()
                                        } else {
                                            commandText = command.command
                                        }
                                    },
                                    onLongClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
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
                                text = {
                                    Text(
                                        stringResource(R.string.delete),
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                },
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
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
                    label = { Text(stringResource(R.string.command)) },
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
                    contentDescription = stringResource(R.string.execute),
                    modifier = Modifier.combinedClickable(
                        onClick = executeAndStay,
                        onLongClick = executeAndGoBack,
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
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
