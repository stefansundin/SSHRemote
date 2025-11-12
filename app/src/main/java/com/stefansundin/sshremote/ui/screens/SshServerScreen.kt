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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.data.sshserver.SshServer
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SshServerScreen(
    servers: List<SshServer>,
    onConnectClicked: (SshServer) -> Unit,
    onAddServerClicked: () -> Unit,
    onEditServerClicked: (SshServer) -> Unit,
    onCloneServerClicked: (SshServer) -> Unit,
    onDeleteServerClicked: (SshServer) -> Unit,
    onSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Hosts") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = onSettingsClicked) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddServerClicked) {
                Icon(Icons.Filled.Add, contentDescription = "Add SSH Host")
            }
        },
    ) { innerPadding ->
        if (servers.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("No SSH hosts added yet.", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Tap the + button to add one.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
        } else {
            LazyColumn(
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp),
            ) {
                items(items = servers, key = { server -> server.id }) { server ->
                    SshServerItem(
                        server = server,
                        onConnect = { onConnectClicked(server) },
                        onEdit = { onEditServerClicked(server) },
                        onClone = { onCloneServerClicked(server) },
                        onDelete = { onDeleteServerClicked(server) },
                    )
                }
            }
        }
    }
}


@Composable
fun SshServerItem(
    server: SshServer,
    onConnect: () -> Unit,
    onEdit: () -> Unit,
    onClone: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isContextMenuVisible by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .clickable(onClick = onConnect),
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = server.name,
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(2.dp))

                val portString = if (server.port != 22) ":${server.port}" else ""
                Text(
                    text = "${server.user}@${server.host}${portString}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Box {
                IconButton(onClick = { isContextMenuVisible = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                    )
                }

                DropdownMenu(
                    expanded = isContextMenuVisible,
                    onDismissRequest = { isContextMenuVisible = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            onEdit()
                            isContextMenuVisible = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Clone") },
                        onClick = {
                            onClone()
                            isContextMenuVisible = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            onDelete()
                            isContextMenuVisible = false
                        },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SshServerScreenPreview() {
    SSHRemoteTheme {
        val sampleServers = listOf(
            SshServer(1, "Raspberry Pi", "192.168.1.10", 22, "pi", null),
            SshServer(2, "Example Server", "example.com", 2222, "admin", null),
        )
        SshServerScreen(
            servers = sampleServers,
            onConnectClicked = {},
            onAddServerClicked = {},
            onEditServerClicked = {},
            onCloneServerClicked = {},
            onDeleteServerClicked = {},
            onSettingsClicked = {},
        )
    }
}
