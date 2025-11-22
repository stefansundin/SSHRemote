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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.data.host.RemoteUiState
import com.stefansundin.sshremote.performHapticFeedback
import com.stefansundin.sshremote.ui.components.RemoteControl
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteControlScreen(
    uiState: RemoteUiState,
    onRunCommand: (Command) -> Unit,
    onMouseMove: (Float, Float, String) -> Unit,
    onMousePan: (String) -> Unit,
    onDisconnect: () -> Unit,
    onSwitchToCommandList: () -> Unit,
    onAdHocCommandClicked: () -> Unit,
    onEditRemoteControlClicked: () -> Unit,
    onCopyPublicKeyClicked: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = onClearError,
            title = { Text("Connection Error") },
            text = { Text(uiState.error) },
            confirmButton = {
                TextButton(onClick = onClearError) {
                    Text("OK")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.host?.name ?: "Remote") },
                navigationIcon = {
                    IconButton(onClick = onDisconnect) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Disconnect")
                    }
                },
                actions = {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(24.dp),
                        )
                    }
                    ConnectionStatusIndicator(
                        connectionStatus = uiState.connectionStatus,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    IconButton(onClick = onSwitchToCommandList) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Switch to command list")
                    }
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ad-hoc command") },
                            onClick = {
                                showMenu = false
                                onAdHocCommandClicked()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Edit remote control") },
                            onClick = {
                                showMenu = false
                                onEditRemoteControlClicked()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Copy public key") },
                            onClick = {
                                showMenu = false
                                onCopyPublicKeyClicked()
                            },
                        )
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        val commands = uiState.host?.remoteCommands ?: emptyMap()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            RemoteControl(
                onKeyClicked = { key ->
                    performHapticFeedback(context, uiState.hapticFeedback)
                    commands[key]?.let { command ->
                        onRunCommand(Command(command, command, false))
                    }
                },
                onMouseEvent = { event ->
                    if (event !is MouseEvent.Move) {
                        performHapticFeedback(context, uiState.hapticFeedback)
                    }
                    val key = when (event) {
                        is MouseEvent.Move -> RemoteControlKey.MOUSE_MOVE
                        MouseEvent.LeftClick -> RemoteControlKey.MOUSE_LEFT_CLICK
                        MouseEvent.RightClick -> RemoteControlKey.MOUSE_RIGHT_CLICK
                        is MouseEvent.Pan -> {
                            if (abs(event.dx) > abs(event.dy)) {
                                if (event.dx > 0) RemoteControlKey.MOUSE_PAN_RIGHT else RemoteControlKey.MOUSE_PAN_LEFT
                            } else {
                                if (event.dy > 0) RemoteControlKey.MOUSE_PAN_DOWN else RemoteControlKey.MOUSE_PAN_UP
                            }
                        }
                    }
                    commands[key]?.let { commandTemplate ->
                        when (event) {
                            is MouseEvent.Move -> {
                                onMouseMove(event.dx, event.dy, commandTemplate)
                            }

                            is MouseEvent.Pan -> {
                                onMousePan(commandTemplate)
                            }

                            else -> {
                                onRunCommand(Command(key.title, commandTemplate, false))
                            }
                        }
                    }
                },
            )
        }
    }
}
