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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.ConnectionStatus
import com.stefansundin.sshremote.data.host.RemoteUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandListScreen(
    uiState: RemoteUiState,
    onRunCommand: (Command) -> Unit,
    onDisconnect: () -> Unit,
    onEditCommands: () -> Unit,
    onAdHocCommandClicked: () -> Unit,
    onSwitchToRemoteControl: () -> Unit,
    onCopyPublicKeyClicked: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

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
                    IconButton(onClick = onSwitchToRemoteControl) {
                        Icon(Icons.Default.SettingsRemote, contentDescription = "Switch to remote control")
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
                            text = { Text("Edit commands") },
                            onClick = {
                                showMenu = false
                                onEditCommands()
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
        if (uiState.commands.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("No commands have been added.", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Use the menu to add commands.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.commands.forEach { command ->
                    Button(
                        onClick = { onRunCommand(command) },
                        enabled = uiState.connectionStatus == ConnectionStatus.CONNECTED,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(command.name ?: command.command)
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusIndicator(connectionStatus: ConnectionStatus, modifier: Modifier = Modifier) {
    val color = when (connectionStatus) {
        ConnectionStatus.CONNECTED -> Color.Green
        ConnectionStatus.CONNECTING -> Color.Yellow
        ConnectionStatus.DISCONNECTED -> Color.Red
    }
    Box(
        modifier = modifier
            .size(12.dp)
            .background(color, shape = CircleShape),
    )
}
