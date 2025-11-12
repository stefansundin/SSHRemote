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

package com.stefansundin.sshremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.stefansundin.sshremote.data.CryptoManager
import com.stefansundin.sshremote.data.sshkey.SshKeyEvent
import com.stefansundin.sshremote.data.sshkey.SshKeyViewModel
import com.stefansundin.sshremote.data.sshkey.SshKeyViewModelFactory
import com.stefansundin.sshremote.data.settings.SettingsViewModel
import com.stefansundin.sshremote.data.settings.SettingsViewModelFactory
import com.stefansundin.sshremote.data.sshserver.SshServer
import com.stefansundin.sshremote.data.sshserver.SshServerViewModel
import com.stefansundin.sshremote.data.sshserver.SshServerViewModelFactory
import com.stefansundin.sshremote.ui.screens.AddEditSshServerScreen
import com.stefansundin.sshremote.ui.screens.AddSshKeyScreen
import com.stefansundin.sshremote.ui.screens.EditCommandsScreen
import com.stefansundin.sshremote.ui.components.PublicKeyDialog
import com.stefansundin.sshremote.ui.screens.SettingsScreen
import com.stefansundin.sshremote.ui.screens.SshKeysScreen
import com.stefansundin.sshremote.ui.screens.SshServerScreen
import com.stefansundin.sshremote.ui.screens.SshTerminalScreen
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class Screen {
    LIST,
    EDIT,
    TERMINAL,
    SETTINGS,
    SSH_KEYS,
    ADD_SSH_KEY,
    EDIT_COMMANDS,
}

enum class Theme {
    SYSTEM,
    LIGHT,
    DARK
}

class MainActivity : ComponentActivity() {
    private val cryptoManager = CryptoManager()

    private val sshRepository: SshRepository by lazy {
        val app = (application as SshRemoteApplication)
        SshRepository(app.settingsRepository)
    }

    private val sshServerViewModel: SshServerViewModel by viewModels {
        val app = (application as SshRemoteApplication)
        SshServerViewModelFactory(
            app.sshServerRepository,
            app.sshKeyRepository,
            sshRepository,
            cryptoManager,
        )
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        val app = (application as SshRemoteApplication)
        SettingsViewModelFactory(app.settingsRepository)
    }

    private val sshKeyViewModel: SshKeyViewModel by viewModels {
        val app = (application as SshRemoteApplication)
        SshKeyViewModelFactory(
            app.sshKeyRepository,
            cryptoManager,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val theme by settingsViewModel.theme.collectAsState()
            val useDarkTheme = when (theme) {
                Theme.SYSTEM -> isSystemInDarkTheme()
                Theme.LIGHT -> false
                Theme.DARK -> true
            }

            SSHRemoteTheme(darkTheme = useDarkTheme) {
                var selectedServer by remember { mutableStateOf<SshServer?>(null) }
                var currentScreen by remember { mutableStateOf(Screen.LIST) }

                var showPublicKeyDialog by remember { mutableStateOf(false) }
                var publicKeyToShow by remember { mutableStateOf("") }
                var fileToExport by remember { mutableStateOf<Pair<String, String>?>(null) }

                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
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

                val hostKeyVerification by sshRepository.hostKeyVerification.collectAsState()
                hostKeyVerification?.let { verification ->
                    AlertDialog(
                        onDismissRequest = { sshRepository.onHostKeyVerificationComplete(false) },
                        title = { Text("Host Key Verification") },
                        text = { Text(verification.message) },
                        confirmButton = {
                            TextButton(onClick = { sshRepository.onHostKeyVerificationComplete(true) }) {
                                Text("Accept")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { sshRepository.onHostKeyVerificationComplete(false) }) {
                                Text("Reject")
                            }
                        },
                    )
                }

                val message by sshRepository.message.collectAsState()
                message?.let { message ->
                    AlertDialog(
                        onDismissRequest = { sshRepository.onMessageDismissed() },
                        title = { Text("Message") },
                        text = { Text(message.message) },
                        confirmButton = {
                            TextButton(onClick = { sshRepository.onMessageDismissed() }) {
                                Text("OK")
                            }
                        }
                    )
                }

                val passwordPrompt by sshRepository.passwordPrompt.collectAsState()
                passwordPrompt?.let { prompt ->
                    var password by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { sshRepository.onPasswordPromptComplete(null) },
                        title = { Text(prompt.message) },
                        text = {
                            TextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = { sshRepository.onPasswordPromptComplete(password) }) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { sshRepository.onPasswordPromptComplete(null) }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                val passphrasePrompt by sshRepository.passphrasePrompt.collectAsState()
                passphrasePrompt?.let { prompt ->
                    var passphrase by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { sshRepository.onPassphrasePromptComplete(null) },
                        title = { Text(prompt.message) },
                        text = {
                            TextField(
                                value = passphrase,
                                onValueChange = { passphrase = it },
                                label = { Text("Passphrase") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = { sshRepository.onPassphrasePromptComplete(passphrase) }) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { sshRepository.onPassphrasePromptComplete(null) }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                LaunchedEffect(Unit) {
                    sshKeyViewModel.eventFlow.collectLatest { event ->
                        when (event) {
                            is SshKeyEvent.ShowPublicKey -> {
                                publicKeyToShow = event.publicKey
                                showPublicKeyDialog = true
                            }

                            is SshKeyEvent.ExportPublicKey -> {
                                fileToExport = event.filename to event.content
                                fileSaverLauncher.launch(event.filename)
                            }

                            is SshKeyEvent.Error -> {
                                scope.launch { snackbarHostState.showSnackbar(event.message) }
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

                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                ) { paddingValues ->
                    when (currentScreen) {
                        Screen.LIST -> {
                            val servers by sshServerViewModel.allServers.collectAsState()
                            SshServerScreen(
                                servers = servers,
                                onConnectClicked = { server: SshServer ->
                                    selectedServer = server
                                    currentScreen = Screen.TERMINAL
                                    sshServerViewModel.connectToServer(server)
                                },
                                onAddServerClicked = {
                                    selectedServer = null
                                    currentScreen = Screen.EDIT
                                },
                                onEditServerClicked = { server ->
                                    selectedServer = server
                                    currentScreen = Screen.EDIT
                                },
                                onDeleteServerClicked = { server ->
                                    sshServerViewModel.delete(
                                        server,
                                    )
                                },
                                onSettingsClicked = {
                                    currentScreen = Screen.SETTINGS
                                },
                            )
                        }

                        Screen.EDIT -> {
                            val sshKeys by sshKeyViewModel.sshKeys.collectAsState()
                            AddEditSshServerScreen(
                                server = selectedServer,
                                sshKeys = sshKeys,
                                onServerSaved = { server ->
                                    sshServerViewModel.upsert(server)
                                    selectedServer = null
                                    currentScreen = Screen.LIST
                                },
                                onNavigateUp = {
                                    selectedServer = null
                                    currentScreen = Screen.LIST
                                },
                                cryptoManager = cryptoManager,
                            )
                        }

                        Screen.TERMINAL -> {
                            val uiState by sshServerViewModel.uiState.collectAsState()
                            SshTerminalScreen(
                                uiState = uiState,
                                onRunCommand = { sshServerViewModel.runCommand(it) },
                                onDisconnect = {
                                    sshServerViewModel.disconnect()
                                    selectedServer = null
                                    currentScreen = Screen.LIST
                                },
                                onClearCommandOutput = { sshServerViewModel.clearCommandOutput() },
                                onEditCommands = { currentScreen = Screen.EDIT_COMMANDS },
                            )
                        }

                        Screen.EDIT_COMMANDS -> {
                            val uiState by sshServerViewModel.uiState.collectAsState()
                            EditCommandsScreen(
                                commands = uiState.commands,
                                onSave = {
                                    selectedServer?.let { server ->
                                        sshServerViewModel.upsert(server.copy(commands = it))
                                    }
                                    currentScreen = Screen.TERMINAL
                                },
                                onNavigateBack = { currentScreen = Screen.TERMINAL },
                            )
                        }

                        Screen.SETTINGS -> {
                            SettingsScreen(
                                settingsViewModel = settingsViewModel,
                                onNavigateToSshKeys = { currentScreen = Screen.SSH_KEYS },
                                onNavigateUp = {
                                    selectedServer = null
                                    currentScreen = Screen.LIST
                                },
                            )
                        }

                        Screen.SSH_KEYS -> {
                            SshKeysScreen(
                                cryptoManager = cryptoManager,
                                sshKeyViewModel = sshKeyViewModel,
                                onNavigateToAddSshKey = { currentScreen = Screen.ADD_SSH_KEY },
                                onNavigateUp = {
                                    currentScreen = Screen.SETTINGS
                                },
                                onShowPublicKey = { key -> sshKeyViewModel.showPublicKeyFor(key) },
                                onExportPublicKey = { key -> sshKeyViewModel.exportPublicKeyFor(key) },
                                onDeleteKey = { key -> sshKeyViewModel.delete(key) },
                            )
                        }

                        Screen.ADD_SSH_KEY -> {
                            AddSshKeyScreen(
                                onKeySaved = { name, privateKey ->
                                    sshKeyViewModel.insert(name, privateKey)
                                    currentScreen = Screen.SSH_KEYS
                                },
                                onKeyGenerated = { name, type, comment ->
                                    sshKeyViewModel.generateAndInsert(name, type, comment)
                                    currentScreen = Screen.SSH_KEYS
                                },
                                onNavigateUp = { currentScreen = Screen.SSH_KEYS },
                            )
                        }
                    }
                }
            }
        }
    }
}
