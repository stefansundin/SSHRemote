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

import android.content.ClipData
import android.content.res.Configuration
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.KeyPair
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.data.ICryptoManager
import com.stefansundin.sshremote.data.identity.IIdentityListViewModel
import com.stefansundin.sshremote.data.identity.Identity
import com.stefansundin.sshremote.data.identity.IdentityEvent
import com.stefansundin.sshremote.ui.components.PublicKeyDialog
import com.stefansundin.sshremote.ui.components.TextWithInlineIcon
import com.stefansundin.sshremote.ui.dpadFocusable
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityListScreen(
    identityViewModel: IIdentityListViewModel,
    cryptoManager: ICryptoManager,
    onNavigateToAddIdentity: (Boolean) -> Unit,
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

    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val view = LocalView.current
    val resources = LocalResources.current
    val coroutineScope = rememberCoroutineScope()

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
                    undoableDeletedIdentityId = null
                    onNavigateToAddIdentity(true)
                }

                is PressInteraction.Release -> {
                    if (!isLongClick) {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        undoableDeletedIdentityId = null
                        onNavigateToAddIdentity(false)
                    }
                }

                is PressInteraction.Cancel -> {
                    isLongClick = false
                }
            }
        }
    }

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
                message = resources.getString(R.string.ssh_key_deleted),
                actionLabel = resources.getString(R.string.undo),
                duration = SnackbarDuration.Indefinite,
            )
            if (result == SnackbarResult.ActionPerformed) {
                view.playSoundEffect(SoundEffectConstants.CLICK)
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
            title = { Text(stringResource(R.string.error)) },
            text = {
                SelectionContainer {
                    Text(errorMessage!!)
                }
            },
            properties = DialogProperties(dismissOnClickOutside = false),
            onDismissRequest = { errorMessage = null },
            confirmButton = {
                Button(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        errorMessage = null
                    },
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        val clipData =
                            ClipData.newPlainText(resources.getString(R.string.command_output_label), errorMessage)
                        coroutineScope.launch { clipboard.setClipEntry(clipData.toClipEntry()) }
                    },
                ) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = stringResource(R.string.copy),
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.copy))
                }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ssh_keys)) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            onNavigateUp()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
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
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_ssh_key))
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
                    Text(stringResource(R.string.no_ssh_keys_added_yet), style = MaterialTheme.typography.bodyLarge)

                    TextWithInlineIcon(
                        stringResource(R.string.empty_list_add_prompt),
                        "+",
                        Icons.Default.Add,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp),
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
    cryptoManager: ICryptoManager,
    onShowPublicKey: () -> Unit,
    onExportPublicKey: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit,
) {
    var isContextMenuVisible by rememberSaveable { mutableStateOf(false) }
    var isRenameDialogVisible by rememberSaveable { mutableStateOf(false) }
    var newName by rememberSaveable { mutableStateOf(identity.name) }
    val view = LocalView.current
    val resources = LocalResources.current

    val (keyInfo, isEncrypted) = remember(identity, cryptoManager, resources) {
        val privateKey = cryptoManager.decrypt(identity.encryptedPrivateKey)
        try {
            val keyPair = KeyPair.load(JSch(), privateKey, null)
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            var type = keyPair.keyTypeString
            if (keyPair.keyType == KeyPair.RSA) {
                type = resources.getString(R.string.key_list_type_format, type, keyPair.keySize)
            }
            val date = identity.createdAt.format(formatter)
            val info = resources.getString(R.string.identity_info_format, type, date)
            val encrypted = keyPair.isEncrypted
            keyPair.dispose()
            Pair(info, encrypted)
        } catch (e: JSchException) {
            Log.e("IdentityItem", "Invalid key", e)
            Pair(resources.getString(R.string.invalid_key), false)
        }
    }

    ListItem(
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(identity.name)
                if (isEncrypted) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = stringResource(R.string.passphrase_protected),
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
                IconButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        isContextMenuVisible = true
                    },
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options))
                }
                DropdownMenu(
                    expanded = isContextMenuVisible,
                    onDismissRequest = { isContextMenuVisible = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.rename)) },
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            isRenameDialogVisible = true
                            isContextMenuVisible = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.view_public_key)) },
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            onShowPublicKey()
                            isContextMenuVisible = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.export_public_key)) },
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            onExportPublicKey()
                            isContextMenuVisible = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
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
            title = { Text(stringResource(R.string.rename_ssh_key_title)) },
            text = {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text(stringResource(R.string.new_name)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .dpadFocusable(),
                )
            },
            onDismissRequest = { isRenameDialogVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onRename(newName)
                        isRenameDialogVisible = false
                    },
                    enabled = newName.isNotBlank(),
                ) {
                    Text(stringResource(R.string.rename))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        isRenameDialogVisible = false
                    },
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

private class FakeIdentityListViewModel(initialIdentities: List<Identity>?) : IIdentityListViewModel {
    override val identities = MutableStateFlow(initialIdentities)
    override val eventFlow = MutableSharedFlow<IdentityEvent>()
    override fun showPublicKeyFor(identity: Identity) {}
    override fun exportPublicKeyFor(identity: Identity) {}
}

private val fakeCryptoManager = object : ICryptoManager {
    override fun encrypt(data: ByteArray): ByteArray = data
    override fun encrypt(data: String): ByteArray = data.toByteArray()
    override fun decrypt(encryptedDataWithIv: ByteArray): ByteArray = encryptedDataWithIv
    override fun decryptToString(encryptedDataWithIv: ByteArray): String =
        encryptedDataWithIv.toString(Charsets.UTF_8)
}

@Suppress("SpellCheckingInspection")
private val dummyEncryptedKey = """
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAACmFlczI1Ni1jdHIAAAAGYmNyeXB0AAAAGAAAABDSThtB3E
LSKB+A2XMaPS0kAAAAGAAAAAEAAAAzAAAAC3NzaC1lZDI1NTE5AAAAINTmexYmb9t4XKWd
FRagjF6/QusbtZwcVCsXwLgMZcL5AAAAoPQ9a73wEivjghvyhyKAHzvLA2LmQQEtWXhObR
4FXwvPxrRJ4C89yGcdh+fyEujC6tTNt9b2iIQdkvcXPSy/IJjYG1SMsA2sFFC0HsGTeSa7
aiAf1tEOJGq0UeyyI+SskFK+FCX5g9blOoXJRDQW1qjP7Ige1HoT7rpNwEz/xjlMVAS/Me
bTweZs5vUHXl3DwGDMbnWc5GZN+figL3dxWVg=
-----END OPENSSH PRIVATE KEY-----
""".trimIndent().toByteArray()

@Suppress("SpellCheckingInspection")
private val dummyUnencryptedKey = """
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW
QyNTUxOQAAACBhHaIoIcFIKPqdxPzTGFZCLO3cyIepqVfEJOxBanbwrQAAAJiExnhehMZ4
XgAAAAtzc2gtZWQyNTUxOQAAACBhHaIoIcFIKPqdxPzTGFZCLO3cyIepqVfEJOxBanbwrQ
AAAED3XbOirI60/hldspI2oaMWBL9k50YX8z5uzaCqo0nSlWEdoighwUgo+p3E/NMYVkIs
7dzIh6mpV8Qk7EFqdvCtAAAAFUR1bW15IFVuZW5jcnlwdGVkIEtleQ==
-----END OPENSSH PRIVATE KEY-----
""".trimIndent().toByteArray()

@Suppress("SpellCheckingInspection")
private val dummyUnencryptedRsaKey = """
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAlwAAAAdzc2gtcn
NhAAAAAwEAAQAAAIEAxs5vbAoNQ+OucUpdnOP7L8GTtm7PwK+LPnfGiWsVoji16NapJGZM
Xee9cgUZrD4BFAtraVPJRYwm1Niba7RH3ic2yd7p6i92hV2OBvV5ag0WBXX/+IWYJu9Ue8
1LUF4scq+zU9x1iAZbiZ6+yxFG0zjWVEGKVSJj/3zJespJMaEAAAIQwyl04MMpdOAAAAAH
c3NoLXJzYQAAAIEAxs5vbAoNQ+OucUpdnOP7L8GTtm7PwK+LPnfGiWsVoji16NapJGZMXe
e9cgUZrD4BFAtraVPJRYwm1Niba7RH3ic2yd7p6i92hV2OBvV5ag0WBXX/+IWYJu9Ue81L
UF4scq+zU9x1iAZbiZ6+yxFG0zjWVEGKVSJj/3zJespJMaEAAAADAQABAAAAgHGW35jGQY
AJpdD7IXOT2yAVJVW2CKPaaN+/RcOcWJcAegdYJvoyLO32i4qLGXkNUEZoo+1hPv2qr0Er
pdmq/ugOjZknszu4CWW4nba3iqI3e2P6vNiOQJ1Ueeso37fZ/FdMMW2Gpm4SEciNM6bJVN
pRzXdDY73mdBXvEQc0sJPlAAAAQQD5ZSttfTEkY8sujsmY2D4f1ZngZwYS0B+KEL8bC1cu
tusqNxjEPLdIv0MS4UVFrsQmZyerGYOcuwrZpzsEfhTyAAAAQQD/YL/aqQI92zSwWGYhgt
tHNKg/UBN6tHcnfTtC91mblRePKyoVB49B9+KimQ6JGOgrnW3SSOM0toO+d619nAtzAAAA
QQDHSmiSqMJm0i5ndyRZvbuWMnNz166n7VYI4+jJNLVbAdXlkvlTq8VBjnPcOboeZA2yDn
Gd80I0/AcKP4TOHfGbAAAAGEluc2VjdXJlIFVuZW5jcnlwdGVkIEtleQEC
-----END OPENSSH PRIVATE KEY-----
""".trimIndent().toByteArray()

@Suppress("SpellCheckingInspection")
private val dummyUnencryptedEcdsaKey = """
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAaAAAABNlY2RzYS
1zaGEyLW5pc3RwMjU2AAAACG5pc3RwMjU2AAAAQQS/v1hDGGzttbYcLBuJWR34sauBObjT
eh+sq8D4GyInLu9304mU8WO4VVLaFHKWX/jyQBW0ip1nFMf60bbJDqC8AAAAsCtN36orTd
+qAAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBL+/WEMYbO21thws
G4lZHfixq4E5uNN6H6yrwPgbIicu73fTiZTxY7hVUtoUcpZf+PJAFbSKnWcUx/rRtskOoL
wAAAAgbAr04j8rkq2AmkHd2XAaMX2Tux9VAw34+ZYK7HAhWzAAAAAVRHVtbXkgVW5lbmNy
eXB0ZWQgS2V5AQID
-----END OPENSSH PRIVATE KEY-----
""".trimIndent().toByteArray()

val sampleIdentities = listOf(
    Identity(
        id = "1",
        createdAt = OffsetDateTime.now().minusDays(10),
        name = "Work Key",
        encryptedPrivateKey = dummyEncryptedKey,
    ),
    Identity(
        id = "2",
        createdAt = OffsetDateTime.now().minusMonths(3),
        name = "Personal NAS",
        encryptedPrivateKey = dummyUnencryptedKey,
    ),
    Identity(
        id = "3",
        createdAt = OffsetDateTime.now().minusMonths(12),
        name = "Raspberry Pi",
        encryptedPrivateKey = dummyUnencryptedRsaKey,
    ),
    Identity(
        id = "4",
        createdAt = OffsetDateTime.now().minusMonths(18),
        name = "Legacy Key",
        encryptedPrivateKey = dummyUnencryptedEcdsaKey,
    ),
)

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun IdentityListScreenPreview() {
    SSHRemoteTheme {
        IdentityListScreen(
            identityViewModel = FakeIdentityListViewModel(sampleIdentities),
            cryptoManager = fakeCryptoManager,
            onNavigateToAddIdentity = {},
            onNavigateUp = {},
            onDelete = {},
            onRename = { _, _ -> },
            onUndoDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun IdentityListScreenPreview_Empty() {
    SSHRemoteTheme {
        IdentityListScreen(
            identityViewModel = FakeIdentityListViewModel(emptyList()),
            cryptoManager = fakeCryptoManager,
            onNavigateToAddIdentity = {},
            onNavigateUp = {},
            onDelete = {},
            onRename = { _, _ -> },
            onUndoDelete = {},
        )
    }
}
