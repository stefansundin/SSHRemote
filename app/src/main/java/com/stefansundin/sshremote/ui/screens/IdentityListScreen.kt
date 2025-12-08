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

import android.content.ClipData
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.unit.dp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.KeyPair
import com.stefansundin.sshremote.data.CryptoManager
import com.stefansundin.sshremote.data.identity.Identity
import com.stefansundin.sshremote.data.identity.IdentityEvent
import com.stefansundin.sshremote.data.identity.IdentityViewModel
import com.stefansundin.sshremote.ui.components.PublicKeyDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
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
    onDelete: (Identity) -> Unit,
    onRename: (Identity, String) -> Unit,
    onUndoDelete: () -> Unit,
) {
    val identities by identityViewModel.identities.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    var showPublicKeyDialog by rememberSaveable { mutableStateOf(false) }
    var publicKeyToShow by rememberSaveable { mutableStateOf("") }
    var fileToExport by rememberSaveable { mutableStateOf<Pair<String, String>?>(null) }
    var undoableDeletedIdentityId by rememberSaveable { mutableStateOf<String?>(null) }
    var scrollToTopOnNextUpdate by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val fileSaverLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("attachment/plain"),
        onResult = { uri ->
            uri?.let {
                fileToExport?.let { (_, content) ->
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(content.toByteArray())
                    }
                }
            }
            fileToExport = null
        },
    )

    LaunchedEffect(undoableDeletedIdentityId) {
        val id = undoableDeletedIdentityId
        if (id != null) {
            val result = snackbarHostState.showSnackbar(
                message = "SSH key deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Indefinite,
            )
            if (result == SnackbarResult.ActionPerformed) {
                onUndoDelete()

                // Suspend until the identities list is updated with the restored item
                snapshotFlow { identities }
                    .first { updatedIdentities -> updatedIdentities?.any { it.id == id } == true }

                // Now that the list is updated, find the item and scroll to it
                identities?.indexOfFirst { it.id == id }?.let { index ->
                    if (index != -1) {
                        listState.animateScrollToItem(index)
                    }
                }
            }
            undoableDeletedIdentityId = null
        }
    }

    LaunchedEffect(identities) {
        if (scrollToTopOnNextUpdate && identities != null) {
            listState.animateScrollToItem(0)
            scrollToTopOnNextUpdate = false
        }
    }

    LaunchedEffect(Unit) {
        identityViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is IdentityEvent.ShowPublicKey -> {
                    publicKeyToShow = event.publicKey
                    showPublicKeyDialog = true
                }

                is IdentityEvent.ExportPublicKey -> {
                    fileToExport = event.filename to "${event.content}\n"
                    fileSaverLauncher.launch(event.filename)
                }

                is IdentityEvent.Error -> {
                    errorMessage = event.message
                }

                is IdentityEvent.KeyAdded -> {
                    scrollToTopOnNextUpdate = true
                }
            }
        }
    }

    if (showPublicKeyDialog) {
        PublicKeyDialog(
            publicKey = publicKeyToShow,
            onDismiss = { showPublicKeyDialog = false },
        )
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Error") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                Button(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val clipData = ClipData.newPlainText("Command output", errorMessage)
                        coroutineScope.launch { clipboard.setClipEntry(clipData.toClipEntry()) }
                    },
                ) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Copy")
                }
            },
        )
    }

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
            val identitiesList = identities
            if (identitiesList == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (identitiesList.isEmpty()) {
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
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 80.dp),
                ) {
                    items(identitiesList, key = { it.id }) { identity ->
                        IdentityItem(
                            identity = identity,
                            cryptoManager = cryptoManager,
                            onShowPublicKey = { identityViewModel.showPublicKeyFor(identity) },
                            onExportPublicKey = { identityViewModel.exportPublicKeyFor(identity) },
                            onDelete = {
                                onDelete(identity)
                                undoableDeletedIdentityId = identity.id
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
    var isContextMenuVisible by rememberSaveable { mutableStateOf(false) }
    var isRenameDialogVisible by rememberSaveable { mutableStateOf(false) }
    var newName by rememberSaveable { mutableStateOf(identity.name) }

    val (keyInfo, isEncrypted) = remember(identity, cryptoManager) {
        val privateKey = cryptoManager.decrypt(identity.encryptedPrivateKey)
        try {
            val keypair = KeyPair.load(JSch(), privateKey, null)
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            val info = "${keypair.keyTypeString} - ${identity.createdAt.format(formatter)}"
            val encrypted = keypair.isEncrypted
            keypair.dispose()
            Pair(info, encrypted)
        } catch (e: JSchException) {
            Log.e("IdentityItem", "Invalid key", e)
            Pair("Invalid key", false)
        }
    }

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
