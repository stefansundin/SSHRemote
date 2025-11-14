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
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommandViewModel
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommandViewModelFactory
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.HostViewModel
import com.stefansundin.sshremote.data.host.HostViewModelFactory
import com.stefansundin.sshremote.data.identity.IdentityEvent
import com.stefansundin.sshremote.data.identity.IdentityViewModel
import com.stefansundin.sshremote.data.identity.IdentityViewModelFactory
import com.stefansundin.sshremote.data.settings.SettingsViewModel
import com.stefansundin.sshremote.data.settings.SettingsViewModelFactory
import com.stefansundin.sshremote.ui.components.CommandOutputDialog
import com.stefansundin.sshremote.ui.components.PublicKeyDialog
import com.stefansundin.sshremote.ui.screens.AdHocCommandScreen
import com.stefansundin.sshremote.ui.screens.AddIdentityScreen
import com.stefansundin.sshremote.ui.screens.CommandListScreen
import com.stefansundin.sshremote.ui.screens.EditCommandsScreen
import com.stefansundin.sshremote.ui.screens.EditHostScreen
import com.stefansundin.sshremote.ui.screens.HostScreen
import com.stefansundin.sshremote.ui.screens.IdentityListScreen
import com.stefansundin.sshremote.ui.screens.SettingsScreen
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class Screen {
    HOST_LIST,
    HOST_EDIT,
    COMMAND_LIST,
    EDIT_COMMANDS,
    AD_HOC_COMMAND,
    SETTINGS,
    IDENTITY_LIST,
    ADD_IDENTITY,
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

    private val hostViewModel: HostViewModel by viewModels {
        val app = (application as SshRemoteApplication)
        HostViewModelFactory(
            app.hostRepository,
            app.identityRepository,
            sshRepository,
            cryptoManager,
        )
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        val app = (application as SshRemoteApplication)
        SettingsViewModelFactory(app.settingsRepository, app.hostRepository, app.adHocCommandRepository)
    }

    private val identityViewModel: IdentityViewModel by viewModels {
        val app = (application as SshRemoteApplication)
        IdentityViewModelFactory(
            app.identityRepository,
            cryptoManager,
        )
    }

    private val adHocCommandViewModel: AdHocCommandViewModel by viewModels {
        val app = (application as SshRemoteApplication)
        AdHocCommandViewModelFactory(app.adHocCommandRepository)
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
                var selectedHost by remember { mutableStateOf<Host?>(null) }
                var currentScreen by remember { mutableStateOf(Screen.HOST_LIST) }

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
                        },
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
                                singleLine = true,
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
                        },
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
                                singleLine = true,
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
                        },
                    )
                }

                val uiState by hostViewModel.uiState.collectAsState()
                uiState.commandOutput?.let { output ->
                    CommandOutputDialog(
                        output = output,
                        onDismiss = { hostViewModel.clearCommandOutput() },
                    )
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
                        Screen.HOST_LIST -> {
                            val hosts by hostViewModel.allHosts.collectAsState()
                            HostScreen(
                                hosts = hosts,
                                onConnectClicked = { host: Host ->
                                    selectedHost = host
                                    currentScreen = Screen.COMMAND_LIST
                                    hostViewModel.connect(host)
                                },
                                onAddClicked = {
                                    selectedHost = null
                                    currentScreen = Screen.HOST_EDIT
                                },
                                onEditClicked = { host ->
                                    selectedHost = host
                                    currentScreen = Screen.HOST_EDIT
                                },
                                onCloneClicked = { host ->
                                    selectedHost = host.copy(id = 0, name = "Copy of ${host.name}")
                                    currentScreen = Screen.HOST_EDIT
                                },
                                onDeleteClicked = { host -> hostViewModel.delete(host) },
                                onUndoDeleteClicked = { hostViewModel.undoDelete() },
                                onSettingsClicked = {
                                    currentScreen = Screen.SETTINGS
                                },
                            )
                        }

                        Screen.HOST_EDIT -> {
                            val identities by identityViewModel.identities.collectAsState()
                            EditHostScreen(
                                host = selectedHost,
                                identities = identities,
                                onSave = { host ->
                                    hostViewModel.upsert(host)
                                    selectedHost = null
                                    currentScreen = Screen.HOST_LIST
                                },
                                onNavigateUp = {
                                    selectedHost = null
                                    currentScreen = Screen.HOST_LIST
                                },
                                cryptoManager = cryptoManager,
                            )
                        }

                        Screen.COMMAND_LIST -> {
                            CommandListScreen(
                                uiState = uiState,
                                onRunCommand = { hostViewModel.runCommand(it) },
                                onDisconnect = {
                                    hostViewModel.disconnect()
                                    selectedHost = null
                                    currentScreen = Screen.HOST_LIST
                                },
                                onEditCommands = { currentScreen = Screen.EDIT_COMMANDS },
                                onAdHocCommandClicked = { currentScreen = Screen.AD_HOC_COMMAND },
                                onClearError = { hostViewModel.clearError() },
                            )
                        }

                        Screen.EDIT_COMMANDS -> {
                            EditCommandsScreen(
                                commands = uiState.commands,
                                onSave = {
                                    selectedHost?.let { host ->
                                        hostViewModel.upsert(host.copy(commands = it))
                                    }
                                    currentScreen = Screen.COMMAND_LIST
                                },
                                onNavigateBack = { currentScreen = Screen.COMMAND_LIST },
                            )
                        }

                        Screen.AD_HOC_COMMAND -> {
                            val adHocCommands by adHocCommandViewModel.adHocCommands.collectAsState()
                            AdHocCommandScreen(
                                commands = adHocCommands,
                                onExecuteCommand = { command, popUpToPrevious ->
                                    adHocCommandViewModel.addAdHocCommand(command)
                                    hostViewModel.runCommand(Command(command, command, true))
                                    if (popUpToPrevious) {
                                        currentScreen = Screen.COMMAND_LIST
                                    }
                                },
                                onDeleteCommand = { adHocCommandViewModel.deleteAdHocCommand(it) },
                                onClearHistory = { adHocCommandViewModel.clearAdHocCommands() },
                                onNavigateUp = { currentScreen = Screen.COMMAND_LIST },
                            )
                        }

                        Screen.SETTINGS -> {
                            SettingsScreen(
                                settingsViewModel = settingsViewModel,
                                onNavigateToIdentityList = { currentScreen = Screen.IDENTITY_LIST },
                                onNavigateUp = {
                                    selectedHost = null
                                    currentScreen = Screen.HOST_LIST
                                },
                            )
                        }

                        Screen.IDENTITY_LIST -> {
                            IdentityListScreen(
                                cryptoManager = cryptoManager,
                                identityViewModel = identityViewModel,
                                onNavigateToAddIdentity = { currentScreen = Screen.ADD_IDENTITY },
                                onNavigateUp = {
                                    currentScreen = Screen.SETTINGS
                                },
                                onShowPublicKey = { key -> identityViewModel.showPublicKeyFor(key) },
                                onExportPublicKey = { key -> identityViewModel.exportPublicKeyFor(key) },
                                onDelete = { key -> identityViewModel.delete(key) },
                                onRename = { key, newName -> identityViewModel.rename(key, newName) },
                                onUndoDelete = { identityViewModel.undoDelete() },
                            )
                        }

                        Screen.ADD_IDENTITY -> {
                            AddIdentityScreen(
                                onKeySaved = { name, privateKey ->
                                    identityViewModel.insert(name, privateKey)
                                    currentScreen = Screen.IDENTITY_LIST
                                },
                                onKeyGenerated = { name, type, comment ->
                                    identityViewModel.generateAndInsert(name, type, comment)
                                    currentScreen = Screen.IDENTITY_LIST
                                },
                                onNavigateUp = { currentScreen = Screen.IDENTITY_LIST },
                            )
                        }

                    }
                }
            }
        }
    }
}
