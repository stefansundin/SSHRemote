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
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stefansundin.sshremote.data.CryptoManager
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommandViewModel
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommandViewModelFactory
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.HostViewModel
import com.stefansundin.sshremote.data.host.HostViewModelFactory
import com.stefansundin.sshremote.data.host.StartScreen
import com.stefansundin.sshremote.data.identity.IdentityEvent
import com.stefansundin.sshremote.data.identity.IdentityViewModel
import com.stefansundin.sshremote.data.identity.IdentityViewModelFactory
import com.stefansundin.sshremote.data.settings.SettingsViewModel
import com.stefansundin.sshremote.data.settings.SettingsViewModelFactory
import com.stefansundin.sshremote.ui.components.CommandOutputDialog
import com.stefansundin.sshremote.ui.components.PublicKeyDialog
import com.stefansundin.sshremote.ui.components.SelectIdentityDialog
import com.stefansundin.sshremote.ui.screens.AdHocCommandScreen
import com.stefansundin.sshremote.ui.screens.AddIdentityScreen
import com.stefansundin.sshremote.ui.screens.CommandListScreen
import com.stefansundin.sshremote.ui.screens.EditCommandsScreen
import com.stefansundin.sshremote.ui.screens.EditHostScreen
import com.stefansundin.sshremote.ui.screens.EditRemoteControlScreen
import com.stefansundin.sshremote.ui.screens.HostScreen
import com.stefansundin.sshremote.ui.screens.IdentityListScreen
import com.stefansundin.sshremote.ui.screens.RemoteControlScreen
import com.stefansundin.sshremote.ui.screens.SettingsScreen
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    data object HostList : Screen("host_list")
    data object HostEdit : Screen("host_edit/{hostId}") {
        fun createRoute(hostId: Int?) = "host_edit/$hostId"
    }

    data object CommandList : Screen("command_list/{hostId}") {
        fun createRoute(hostId: Int) = "command_list/$hostId"
    }

    data object RemoteControl : Screen("remote_control/{hostId}") {
        fun createRoute(hostId: Int) = "remote_control/$hostId"
    }

    data object EditRemoteControl : Screen("edit_remote_control")

    data object EditCommands : Screen("edit_commands")
    data object AdHocCommand : Screen("ad_hoc_command")
    data object Settings : Screen("settings")
    data object IdentityList : Screen("identity_list")
    data object AddIdentity : Screen("add_identity")
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
            app.settingsRepository,
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
                var showPublicKeyDialog by remember { mutableStateOf(false) }
                var showSelectIdentityDialog by remember { mutableStateOf(false) }
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

                if (showSelectIdentityDialog) {
                    val identities by identityViewModel.identities.collectAsState()
                    SelectIdentityDialog(
                        identities = identities,
                        onIdentitySelected = {
                            scope.launch {
                                val publicKey = identityViewModel.getPublicKey(it)
                                val command =
                                    """exec sh -c 'cd; umask 077; echo "\n$publicKey" >> ~/.ssh/authorized_keys'"""
                                hostViewModel.runCommand(Command("Copy public key", command, false))
                                snackbarHostState.showSnackbar("Public key copied to host.")
                            }
                            showSelectIdentityDialog = false
                        },
                        onDismiss = { showSelectIdentityDialog = false },
                    )
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                ) { innerPadding ->
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Screen.HostList.route,
                        modifier = Modifier.padding(innerPadding),
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                    ) {
                        composable(Screen.HostList.route) {
                            val hosts by hostViewModel.allHosts.collectAsState()
                            HostScreen(
                                hosts = hosts,
                                onConnectClicked = { host ->
                                    val route = when (host.startScreen) {
                                        StartScreen.COMMAND_LIST -> Screen.CommandList.createRoute(host.id)
                                        StartScreen.REMOTE_CONTROL -> Screen.RemoteControl.createRoute(host.id)
                                    }
                                    navController.navigate(route)
                                    hostViewModel.connect(host)
                                },
                                onAddClicked = {
                                    navController.navigate(Screen.HostEdit.createRoute(null))
                                },
                                onEditClicked = { host ->
                                    navController.navigate(Screen.HostEdit.createRoute(host.id))
                                },
                                onCloneClicked = { host ->
                                    hostViewModel.setCloneHost(host)
                                    navController.navigate(Screen.HostEdit.createRoute(null))
                                },
                                onDeleteClicked = { host -> hostViewModel.delete(host) },
                                onUndoDeleteClicked = { hostViewModel.undoDelete() },
                                onSettingsClicked = {
                                    navController.navigate(Screen.Settings.route)
                                },
                            )
                        }

                        composable(
                            Screen.HostEdit.route,
                            arguments = listOf(navArgument("hostId") { type = NavType.StringType; nullable = true }),
                        ) { backStackEntry ->
                            val hostId = backStackEntry.arguments?.getString("hostId")?.toIntOrNull()
                            val hosts by hostViewModel.allHosts.collectAsState()
                            val host = hosts.find { it.id == hostId }
                            val identities by identityViewModel.identities.collectAsState()
                            EditHostScreen(
                                host = host,
                                identities = identities,
                                onSave = { newHost ->
                                    hostViewModel.upsert(newHost)
                                    navController.popBackStack()
                                },
                                onNavigateUp = {
                                    navController.popBackStack()
                                },
                                cryptoManager = cryptoManager,
                                hostViewModel = hostViewModel,
                            )
                        }

                        composable(
                            Screen.CommandList.route,
                            arguments = listOf(navArgument("hostId") { type = NavType.IntType }),
                        ) { backStackEntry ->
                            val hostId = backStackEntry.arguments?.getInt("hostId")!!
                            val hosts by hostViewModel.allHosts.collectAsState()
                            val host = hosts.find { it.id == hostId }
                            LaunchedEffect(host) {
                                if (host != null) {
                                    hostViewModel.setActiveHost(host)
                                }
                            }
                            CommandListScreen(
                                uiState = uiState,
                                onRunCommand = { hostViewModel.runCommand(it) },
                                onDisconnect = {
                                    hostViewModel.disconnect()
                                    navController.popBackStack()
                                },
                                onEditCommands = { navController.navigate(Screen.EditCommands.route) },
                                onAdHocCommandClicked = { navController.navigate(Screen.AdHocCommand.route) },
                                onSwitchToRemoteControl = {
                                    navController.navigate(Screen.RemoteControl.createRoute(hostId)) {
                                        popUpTo(Screen.CommandList.createRoute(hostId)) {
                                            inclusive = true
                                        }
                                    }
                                },
                                onCopyPublicKeyClicked = { showSelectIdentityDialog = true },
                                onClearError = { hostViewModel.clearError() },
                            )
                        }

                        composable(
                            Screen.RemoteControl.route,
                            arguments = listOf(navArgument("hostId") { type = NavType.IntType }),
                        ) { backStackEntry ->
                            val hostId = backStackEntry.arguments?.getInt("hostId")!!
                            val hosts by hostViewModel.allHosts.collectAsState()
                            val host = hosts.find { it.id == hostId }
                            LaunchedEffect(host) {
                                if (host != null) {
                                    hostViewModel.setActiveHost(host)
                                }
                            }
                            RemoteControlScreen(
                                uiState = uiState,
                                onRunCommand = { hostViewModel.runCommand(it) },
                                onDisconnect = {
                                    hostViewModel.disconnect()
                                    navController.popBackStack()
                                },
                                onSwitchToCommandList = {
                                    navController.navigate(Screen.CommandList.createRoute(hostId)) {
                                        popUpTo(Screen.RemoteControl.createRoute(hostId)) {
                                            inclusive = true
                                        }
                                    }
                                },
                                onAdHocCommandClicked = { navController.navigate(Screen.AdHocCommand.route) },
                                onEditRemoteControlClicked = { navController.navigate(Screen.EditRemoteControl.route) },
                                onCopyPublicKeyClicked = { showSelectIdentityDialog = true },
                                onClearError = { hostViewModel.clearError() },
                            )
                        }

                        composable(Screen.EditRemoteControl.route) {
                            EditRemoteControlScreen(
                                commands = uiState.host?.remoteCommands ?: emptyMap(),
                                onSave = { commands ->
                                    uiState.host?.let { host ->
                                        hostViewModel.upsert(host.copy(remoteCommands = commands))
                                    }
                                },
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToEditCommands = {
                                    navController.navigate(Screen.EditCommands.route) {
                                        popUpTo(Screen.EditRemoteControl.route) {
                                            inclusive = true
                                        }
                                    }
                                },
                                onSetAsDefaultScreen = { startScreen ->
                                    uiState.host?.let { host ->
                                        hostViewModel.upsert(host.copy(startScreen = startScreen))
                                    }
                                },
                            )
                        }

                        composable(Screen.EditCommands.route) {
                            EditCommandsScreen(
                                commands = uiState.commands,
                                onSave = { commands ->
                                    uiState.host?.let { host ->
                                        hostViewModel.upsert(host.copy(commands = commands))
                                    }
                                },
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToEditRemoteControl = {
                                    navController.navigate(Screen.EditRemoteControl.route) {
                                        popUpTo(Screen.EditCommands.route) {
                                            inclusive = true
                                        }
                                    }
                                },
                                onSetAsDefaultScreen = { startScreen ->
                                    uiState.host?.let { host ->
                                        hostViewModel.upsert(host.copy(startScreen = startScreen))
                                    }
                                },
                            )
                        }

                        composable(Screen.AdHocCommand.route) {
                            val adHocCommands by adHocCommandViewModel.adHocCommands.collectAsState()
                            AdHocCommandScreen(
                                commands = adHocCommands,
                                onExecuteCommand = { command, popUpToPrevious ->
                                    adHocCommandViewModel.addAdHocCommand(command)
                                    hostViewModel.runCommand(Command(command, command, true))
                                    if (popUpToPrevious) {
                                        navController.popBackStack()
                                    }
                                },
                                onDeleteCommand = { adHocCommandViewModel.deleteAdHocCommand(it) },
                                onClearHistory = { adHocCommandViewModel.clearAdHocCommands() },
                                onNavigateUp = { navController.popBackStack() },
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                settingsViewModel = settingsViewModel,
                                onNavigateToIdentityList = { navController.navigate(Screen.IdentityList.route) },
                                onNavigateUp = {
                                    navController.popBackStack()
                                },
                            )
                        }

                        composable(Screen.IdentityList.route) {
                            IdentityListScreen(
                                cryptoManager = cryptoManager,
                                identityViewModel = identityViewModel,
                                onNavigateToAddIdentity = { navController.navigate(Screen.AddIdentity.route) },
                                onNavigateUp = {
                                    navController.popBackStack()
                                },
                                onShowPublicKey = { key -> identityViewModel.showPublicKeyFor(key) },
                                onExportPublicKey = { key -> identityViewModel.exportPublicKeyFor(key) },
                                onDelete = { key -> identityViewModel.delete(key) },
                                onRename = { key, newName -> identityViewModel.rename(key, newName) },
                                onUndoDelete = { identityViewModel.undoDelete() },
                            )
                        }

                        composable(Screen.AddIdentity.route) {
                            AddIdentityScreen(
                                onKeySaved = { name, privateKey ->
                                    identityViewModel.insert(name, privateKey)
                                    navController.popBackStack()
                                },
                                onKeyGenerated = { name, type, comment ->
                                    identityViewModel.generateAndInsert(name, type, comment)
                                    navController.popBackStack()
                                },
                                onNavigateUp = { navController.popBackStack() },
                            )
                        }
                    }
                }
            }
        }
    }
}
