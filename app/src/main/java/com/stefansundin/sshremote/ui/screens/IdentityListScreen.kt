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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import com.stefansundin.sshremote.data.CryptoManager
import com.stefansundin.sshremote.data.identity.Identity
import com.stefansundin.sshremote.data.identity.IdentityViewModel
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityListScreen(
    identityViewModel: IdentityViewModel,
    cryptoManager: CryptoManager,
    onNavigateToAddIdentity: () -> Unit,
    onNavigateUp: () -> Unit,
    onShowPublicKey: (Identity) -> Unit,
    onExportPublicKey: (Identity) -> Unit,
    onDelete: (Identity) -> Unit,
    onRename: (Identity, String) -> Unit,
    onUndoDelete: () -> Unit,
) {
    val identities by identityViewModel.identities.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("SSH Keys") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddIdentity) {
                Icon(Icons.Default.Add, contentDescription = "Add SSH Key")
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            if (identities.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
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
                    items(identities) { identity ->
                        IdentityItem(
                            identity = identity,
                            cryptoManager = cryptoManager,
                            onShowPublicKey = { onShowPublicKey(identity) },
                            onExportPublicKey = { onExportPublicKey(identity) },
                            onDelete = {
                                onDelete(identity)
                                scope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    val result = snackbarHostState.showSnackbar(
                                        message = "SSH key deleted",
                                        actionLabel = "Undo",
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        onUndoDelete()
                                    }
                                }
                            },
                            onRename = { newName -> onRename(identity, newName) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IdentityItem(
    identity: Identity,
    cryptoManager: CryptoManager,
    onShowPublicKey: () -> Unit,
    onExportPublicKey: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit,
) {
    var isContextMenuVisible by remember { mutableStateOf(false) }
    var isRenameDialogVisible by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(identity.name) }

    val privateKey = cryptoManager.decrypt(identity.encryptedPrivateKey)
    val keypair = KeyPair.load(JSch(), privateKey, null)
    val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    val keyInfo = "${keypair.keyTypeString} - ${identity.createdAt.format(formatter)}"
    val isEncrypted = keypair.isEncrypted
    keypair.dispose()

    ListItem(
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(identity.name)
                if (isEncrypted) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Passphrase protected",
                        modifier = Modifier.padding(start = 8.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
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
                    onDismissRequest = { isContextMenuVisible = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            isRenameDialogVisible = true
                            isContextMenuVisible = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("View public key") },
                        onClick = {
                            onShowPublicKey()
                            isContextMenuVisible = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Export public key") },
                        onClick = {
                            onExportPublicKey()
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
        },
    )

    if (isRenameDialogVisible) {
        AlertDialog(
            onDismissRequest = { isRenameDialogVisible = false },
            title = { Text("Rename SSH Key") },
            text = {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRename(newName)
                        isRenameDialogVisible = false
                    },
                    enabled = newName.isNotBlank(),
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { isRenameDialogVisible = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}
