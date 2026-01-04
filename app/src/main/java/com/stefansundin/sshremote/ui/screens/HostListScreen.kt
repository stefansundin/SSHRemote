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

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.ui.components.TextWithInlineIcon
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostListScreen(
    hosts: List<Host>?,
    onConnectClicked: (Host) -> Unit,
    onAdd: () -> Unit,
    onAddFromQrCode: () -> Unit,
    onEdit: (Host) -> Unit,
    onClone: (Host) -> Unit,
    onCreateShortcut: (Host) -> Unit,
    onDelete: (Host) -> Unit,
    onUndoDelete: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    var undoableDeletedHostId by rememberSaveable { mutableStateOf<String?>(null) }
    val view = LocalView.current

    // Long pressing the FAB will launch directly into the QR code scanner
    // It's a secret feature that is not documented
    val interactionSource = remember { MutableInteractionSource() }
    val viewConfiguration = LocalViewConfiguration.current

    LaunchedEffect(interactionSource) {
        var isLongClick = false

        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isLongClick = false
                    delay(viewConfiguration.longPressTimeoutMillis)
                    isLongClick = true
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    onAddFromQrCode()
                }

                is PressInteraction.Release -> {
                    if (isLongClick.not()) {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onAdd()
                    }
                }

                is PressInteraction.Cancel -> {
                    isLongClick = false
                }
            }
        }
    }

    LaunchedEffect(undoableDeletedHostId) {
        val id = undoableDeletedHostId
        if (id != null) {
            val result = snackbarHostState.showSnackbar(
                message = "Host deleted",
                actionLabel = "Undo",
            )
            if (result == SnackbarResult.ActionPerformed) {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                onUndoDelete()

                // Suspend until the hosts list is updated with the restored item
                snapshotFlow { hosts }.first { updatedHosts -> updatedHosts?.any { it.id == id } == true }

                // Now that the list is updated, find the item and scroll to it
                hosts?.indexOfFirst { it.id == id }?.let { index ->
                    if (index != -1) {
                        listState.animateScrollToItem(index)
                    }
                }
            }
            undoableDeletedHostId = null
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Hosts") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            onHelp()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Help,
                            contentDescription = "Help",
                        )
                    }
                    IconButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            onSettings()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                interactionSource = interactionSource,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add SSH Host")
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (hosts == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (hosts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        stringResource(R.string.no_ssh_hosts_added_yet),
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    TextWithInlineIcon(
                        stringResource(R.string.empty_list_add_prompt),
                        "+",
                        Icons.Default.Add,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp),
                    )

                    TextWithInlineIcon(
                        stringResource(R.string.tap_the_help_button_for_help),
                        "?",
                        Icons.AutoMirrored.Filled.Help,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp),
                ) {
                    items(items = hosts, key = { host -> host.id }) { host ->
                        HostItem(
                            host = host,
                            onConnect = { onConnectClicked(host) },
                            onEdit = { onEdit(host) },
                            onClone = { onClone(host) },
                            onCreateShortcut = { onCreateShortcut(host) },
                            onDelete = {
                                onDelete(host)
                                undoableDeletedHostId = host.id
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HostItem(
    host: Host,
    onConnect: () -> Unit,
    onEdit: () -> Unit,
    onClone: () -> Unit,
    onCreateShortcut: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isContextMenuVisible by rememberSaveable { mutableStateOf(false) }
    val view = LocalView.current

    Card(
        modifier = modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onConnect()
                },
                onLongClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    isContextMenuVisible = true
                },
            ),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = host.name,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                val portString = if (host.port != 22) ":${host.port}" else ""
                Text(
                    text = "${host.user}@${host.hostname}${portString}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Box {
                IconButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        isContextMenuVisible = true
                    },
                ) {
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
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            onEdit()
                            isContextMenuVisible = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Clone") },
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            onClone()
                            isContextMenuVisible = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Add to Home Screen") },
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            onCreateShortcut()
                            isContextMenuVisible = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
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
fun HostListScreenPreview() {
    SSHRemoteTheme {
        val sampleHosts = listOf(
            Host("1", "Raspberry Pi", "192.168.1.10", 22, "pi", null),
            Host("2", "Example Host", "example.com", 2222, "admin", null),
        )
        HostListScreen(
            hosts = sampleHosts,
            onConnectClicked = {},
            onAdd = {},
            onAddFromQrCode = {},
            onEdit = {},
            onClone = {},
            onCreateShortcut = {},
            onDelete = {},
            onUndoDelete = {},
            onSettings = {},
            onHelp = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HostListScreenEmptyPreview() {
    SSHRemoteTheme {
        HostListScreen(
            hosts = emptyList(),
            onConnectClicked = {},
            onAdd = {},
            onAddFromQrCode = {},
            onEdit = {},
            onClone = {},
            onCreateShortcut = {},
            onDelete = {},
            onUndoDelete = {},
            onSettings = {},
            onHelp = {},
        )
    }
}
