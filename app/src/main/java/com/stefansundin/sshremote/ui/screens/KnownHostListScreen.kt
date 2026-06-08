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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.data.knownhost.IKnownHostViewModel
import com.stefansundin.sshremote.data.knownhost.KnownHost
import com.stefansundin.sshremote.ui.components.NoWrapOnSpecialCharactersVisualTransformation
import com.stefansundin.sshremote.ui.components.PublicKeyDialog
import com.stefansundin.sshremote.ui.components.TextWithInlineIcon
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnownHostListScreen(
    knownHostViewModel: IKnownHostViewModel,
    onNavigateUp: () -> Unit,
) {
    val knownHosts by knownHostViewModel.knownHosts.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val resources = LocalResources.current
    val view = LocalView.current

    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showAddDialogScanOnStart by rememberSaveable { mutableStateOf(false) }
    var showFormatHelp by rememberSaveable { mutableStateOf(false) }
    var showHostKeyDialog by rememberSaveable { mutableStateOf(false) }
    var hostKeyToShow by rememberSaveable { mutableStateOf("") }
    var undoableDeletedKnownHostLine by rememberSaveable { mutableStateOf<String?>(null) }

    // Long pressing the FAB will launch directly into the QR code scanner.
    // It's a secret feature that is not documented.
    val interactionSource = remember { MutableInteractionSource() }
    val viewConfiguration = LocalViewConfiguration.current

    LaunchedEffect(interactionSource) {
        var isLongClick = false

        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isLongClick = false
                    delay(viewConfiguration.longPressTimeoutMillis.milliseconds)
                    isLongClick = true
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    undoableDeletedKnownHostLine = null
                    showAddDialogScanOnStart = true
                    showAddDialog = true
                }

                is PressInteraction.Release -> {
                    if (!isLongClick) {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        undoableDeletedKnownHostLine = null
                        showAddDialogScanOnStart = false
                        showAddDialog = true
                    }
                }

                is PressInteraction.Cancel -> {
                    isLongClick = false
                }
            }
        }
    }

    LaunchedEffect(undoableDeletedKnownHostLine) {
        val line = undoableDeletedKnownHostLine ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = resources.getString(R.string.known_host_deleted),
            actionLabel = resources.getString(R.string.undo),
            duration = SnackbarDuration.Indefinite,
        )
        if (result == SnackbarResult.ActionPerformed) {
            view.playSoundEffect(SoundEffectConstants.CLICK)
            knownHostViewModel.undoDeleteKnownHost()

            // Suspend until the known hosts list is updated with the restored item.
            snapshotFlow { knownHosts }
                .first { updatedKnownHosts -> updatedKnownHosts.any { it.line == line } }

            knownHosts.indexOfFirst { it.line == line }.let { index ->
                if (index != -1) {
                    listState.animateScrollToItem(index)
                }
            }
        }
        undoableDeletedKnownHostLine = null
    }

    if (showFormatHelp) {
        KnownHostFormatHelpDialog(onDismiss = { showFormatHelp = false })
    }

    if (showHostKeyDialog) {
        PublicKeyDialog(
            publicKey = hostKeyToShow,
            onDismiss = { showHostKeyDialog = false },
        )
    }

    if (showAddDialog) {
        AddKnownHostDialog(
            scanQrCodeOnStart = showAddDialogScanOnStart,
            onDismiss = {
                showAddDialog = false
                showAddDialogScanOnStart = false
            },
            onAdd = { lines ->
                knownHostViewModel.addKnownHosts(lines)
                showAddDialog = false
                showAddDialogScanOnStart = false
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.known_hosts)) },
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
                actions = {
                    IconButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            showFormatHelp = true
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Help,
                            contentDescription = stringResource(R.string.known_host_help_title),
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
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_entries))
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            if (knownHosts.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        stringResource(R.string.no_known_hosts_added_yet),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )

                    TextWithInlineIcon(
                        stringResource(R.string.empty_list_add_prompt),
                        "+",
                        Icons.Default.Add,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp),
                    )

                    TextWithInlineIcon(
                        stringResource(R.string.tap_the_help_button_for_help),
                        "?",
                        Icons.AutoMirrored.Filled.Help,
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
                    items(knownHosts, key = { it.line }) { entry ->
                        KnownHostItem(
                            entry = entry,
                            onViewHostKey = {
                                hostKeyToShow = entry.line
                                showHostKeyDialog = true
                            },
                            onDelete = {
                                knownHostViewModel.deleteKnownHost(entry)
                                undoableDeletedKnownHostLine = entry.line
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KnownHostItem(
    entry: KnownHost,
    onViewHostKey: () -> Unit,
    onDelete: () -> Unit,
) {
    var isContextMenuVisible by rememberSaveable { mutableStateOf(false) }
    val view = LocalView.current
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

    ListItem(
        headlineContent = {
            Text(
                entry.line,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(entry.createdAt.format(formatter))
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
                        text = { Text(stringResource(R.string.view_host_key)) },
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            onViewHostKey()
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
}

@Composable
private fun KnownHostFormatHelpDialog(onDismiss: () -> Unit) {
    val view = LocalView.current
    AlertDialog(
        title = { Text(stringResource(R.string.known_host_help_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.known_host_help_text))
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.ok))
            }
        },
    )
}

@Composable
private fun AddKnownHostDialog(
    scanQrCodeOnStart: Boolean = false,
    onDismiss: () -> Unit,
    onAdd: (List<String>) -> Unit,
) {
    var lineInput by rememberSaveable { mutableStateOf("") }
    var scanQrCode by rememberSaveable { mutableStateOf(scanQrCodeOnStart) }
    var importMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val view = LocalView.current
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    lineInput = inputStream.reader().readText().trim()
                }
            }
        },
    )

    val qrScanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            lineInput = result.contents.trim()
        }
    }

    val scanQrCodePrompt = stringResource(R.string.scan_qr_code_known_host_prompt)
    LaunchedEffect(scanQrCode) {
        if (scanQrCode) {
            scanQrCode = false
            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            options.setPrompt(scanQrCodePrompt)
            options.setBeepEnabled(false)
            options.setOrientationLocked(false)
            qrScanLauncher.launch(options)
        }
    }

    val parsedKnownHostLines = parseKnownHostLines(lineInput)
    val hasValidInput = parsedKnownHostLines.isNotEmpty()

    AlertDialog(
        title = { Text(stringResource(R.string.known_host_add_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = lineInput,
                    onValueChange = { lineInput = it },
                    label = { Text(stringResource(R.string.known_host_input_label)) },
                    visualTransformation = NoWrapOnSpecialCharactersVisualTransformation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                )
                if (lineInput.isNotBlank()) {
                    Text(
                        text = stringResource(
                            R.string.known_host_import_summary,
                            parsedKnownHostLines.size,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    FilledTonalButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            importMenuExpanded = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.tab_import))
                    }
                    DropdownMenu(
                        expanded = importMenuExpanded,
                        onDismissRequest = { importMenuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.select_file)) },
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                importMenuExpanded = false
                                filePicker.launch("*/*")
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.paste_from_clipboard)) },
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                importMenuExpanded = false
                                coroutineScope.launch {
                                    val clipEntry = clipboard.getClipEntry() ?: return@launch
                                    val text = getClipEntryText(clipEntry.clipData) ?: return@launch
                                    lineInput = text.trim()
                                }
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.scan_qr_code)) },
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                importMenuExpanded = false
                                scanQrCode = true
                            },
                        )
                    }
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onAdd(parsedKnownHostLines)
                },
                enabled = hasValidInput,
            ) {
                Text(stringResource(R.string.add_entries))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

private class FakeKnownHostViewModel(initialEntries: List<KnownHost>) : IKnownHostViewModel {
    override val knownHosts: StateFlow<List<KnownHost>> = MutableStateFlow(initialEntries)
    override fun addKnownHost(line: String) {}
    override fun addKnownHosts(lines: List<String>) {}
    override fun deleteKnownHost(knownHost: KnownHost) {}
    override fun undoDeleteKnownHost() {}
    override fun clearKnownHosts() {}
}

private val sampleKnownHosts = listOf(
    KnownHost(
        line = "@cert-authority *.example.com ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIAfA61oe0k19ti1BWcNcOeehAO28ih1hILIGRQx/1q5p",
        createdAt = OffsetDateTime.now().minusDays(3),
    ),
    KnownHost(
        line = "host.example.com ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIN6AyvsVa1vi58G1DWTU7flhhWCpP5F2anq5Kj09uhDe",
        createdAt = OffsetDateTime.now().minusHours(1),
    ),
)

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun KnownHostListScreenPreview() {
    SSHRemoteTheme {
        KnownHostListScreen(
            knownHostViewModel = FakeKnownHostViewModel(sampleKnownHosts),
            onNavigateUp = {},
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun KnownHostListScreenPreview_Empty() {
    SSHRemoteTheme {
        KnownHostListScreen(
            knownHostViewModel = FakeKnownHostViewModel(emptyList()),
            onNavigateUp = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 600,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun AddKnownHostDialogPreview() {
    SSHRemoteTheme {
        AddKnownHostDialog(
            onDismiss = {},
            onAdd = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 600,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun KnownHostFormatHelpDialogPreview() {
    SSHRemoteTheme {
        KnownHostFormatHelpDialog(onDismiss = {})
    }
}
