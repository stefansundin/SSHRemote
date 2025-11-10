/*
SSH Remote
Copyright (C) 2025  Stefan Sundin

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.stefansundin.sshremote.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import com.stefansundin.sshremote.CryptoManager
import com.stefansundin.sshremote.data.SshKey
import com.stefansundin.sshremote.data.SshKeyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SshKeysScreen(
    sshKeyViewModel: SshKeyViewModel,
    cryptoManager: CryptoManager,
    onNavigateToAddSshKey: () -> Unit,
    onNavigateUp: () -> Unit,
    onShowPublicKey: (SshKey) -> Unit,
    onExportPublicKey: (SshKey) -> Unit,
    onDeleteKey: (SshKey) -> Unit
) {
    val sshKeys by sshKeyViewModel.sshKeys.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SSH Keys") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddSshKey) {
                Icon(Icons.Default.Add, contentDescription = "Add SSH Key")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            if (sshKeys.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No SSH keys added yet.", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Tap the + button to add one.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            } else {
                LazyColumn {
                    items(sshKeys) { sshKey ->
                        SshKeyItem(
                            sshKey = sshKey,
                            cryptoManager = cryptoManager,
                            onShowPublicKey = { onShowPublicKey(sshKey) },
                            onExportPublicKey = { onExportPublicKey(sshKey) },
                            onDelete = { onDeleteKey(sshKey) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SshKeyItem(
    sshKey: SshKey,
    cryptoManager: CryptoManager,
    onShowPublicKey: () -> Unit,
    onExportPublicKey: () -> Unit,
    onDelete: () -> Unit
) {
    var isContextMenuVisible by remember { mutableStateOf(false) }

    val privateKey = cryptoManager.decrypt(sshKey.encryptedPrivateKey)
    val keypair = KeyPair.load(JSch(), privateKey, null)
    val keyInfo = "${keypair.keyTypeString} - ${sshKey.createdAt}"
    keypair.dispose()

    ListItem(
        headlineContent = { Text(sshKey.name) },
        supportingContent = {
            Text(keyInfo)
        },
        trailingContent = {
            Box {
                IconButton(onClick = { isContextMenuVisible = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(
                    expanded = isContextMenuVisible,
                    onDismissRequest = { isContextMenuVisible = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("View public key") },
                        onClick = {
                            onShowPublicKey()
                            isContextMenuVisible = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Export public key") },
                        onClick = {
                            onExportPublicKey()
                            isContextMenuVisible = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = {
                            onDelete()
                            isContextMenuVisible = false
                        }
                    )
                }
            }
        }
    )
}
