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
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stefansundin.sshremote.data.CryptoManager
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommandViewModel
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommandViewModelFactory
import com.stefansundin.sshremote.data.host.HostViewModel
import com.stefansundin.sshremote.data.host.HostViewModelFactory
import com.stefansundin.sshremote.data.host.StartScreen
import com.stefansundin.sshremote.data.identity.IdentityViewModel
import com.stefansundin.sshremote.data.identity.IdentityViewModelFactory
import com.stefansundin.sshremote.data.settings.SettingsViewModel
import com.stefansundin.sshremote.data.settings.SettingsViewModelFactory
import com.stefansundin.sshremote.ui.screens.AdHocCommandScreen
import com.stefansundin.sshremote.ui.screens.AddIdentityScreen
import com.stefansundin.sshremote.ui.screens.EditHostScreen
import com.stefansundin.sshremote.ui.screens.EditRemoteControlScreen
import com.stefansundin.sshremote.ui.screens.HostListScreen
import com.stefansundin.sshremote.ui.screens.IdentityListScreen
import com.stefansundin.sshremote.ui.screens.RemoteControlScreen
import com.stefansundin.sshremote.ui.screens.SettingsScreen
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    data object HostList : Screen("host_list")
    data object HostEdit : Screen("host_edit/{hostId}") {
        fun createRoute(hostId: Int?) = "host_edit/$hostId"
    }

    data object RemoteControl : Screen("remote_control/{hostId}/{initialPage}") {
        fun createRoute(hostId: Int, initialPage: Int = 0) = "remote_control/$hostId/$initialPage"
    }

    data object EditRemoteControl : Screen("edit_remote_control/{initialPage}") {
        fun createRoute(initialPage: Int = 0) = "edit_remote_control/$initialPage"
    }

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
        enableEdgeToEdge()

        setContent {
            val theme by settingsViewModel.theme.collectAsState()
            val useDarkTheme = when (theme) {
                Theme.SYSTEM -> isSystemInDarkTheme()
                Theme.LIGHT -> false
                Theme.DARK -> true
            }

            SSHRemoteTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val scope = rememberCoroutineScope()
                    val uiState by hostViewModel.uiState.collectAsState()
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.HostList.route,
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                    ) {
                        composable(Screen.HostList.route) {
                            val hosts by hostViewModel.allHosts.collectAsState()
                            HostListScreen(
                                hosts = hosts,
                                onConnectClicked = { host ->
                                    val initialPage = when (host.startScreen) {
                                        StartScreen.REMOTE -> 0
                                        StartScreen.MOUSE -> 1
                                        StartScreen.COMMANDS -> 2
                                    }
                                    navController.navigate(Screen.RemoteControl.createRoute(host.id, initialPage))
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
                                    scope.launch {
                                        hostViewModel.upsert(newHost)
                                        navController.popBackStack()
                                    }
                                },
                                onNavigateUp = {
                                    navController.popBackStack()
                                },
                                cryptoManager = cryptoManager,
                                hostViewModel = hostViewModel,
                            )
                        }

                        composable(
                            Screen.RemoteControl.route,
                            arguments = listOf(
                                navArgument("hostId") { type = NavType.IntType },
                                navArgument("initialPage") { type = NavType.IntType },
                            ),
                        ) { backStackEntry ->
                            val hostId = backStackEntry.arguments?.getInt("hostId")!!
                            val initialPage = backStackEntry.arguments?.getInt("initialPage")!!
                            val hosts by hostViewModel.allHosts.collectAsState()
                            val host = hosts.find { it.id == hostId }
                            LaunchedEffect(host) {
                                if (host != null) {
                                    hostViewModel.setActiveHost(host)
                                }
                            }
                            RemoteControlScreen(
                                uiState = uiState,
                                identityViewModel = identityViewModel,
                                hostViewModel = hostViewModel,
                                sshRepository = sshRepository,
                                onMouseMove = { dx, dy, template -> hostViewModel.onMouseMove(dx, dy, template) },
                                onMousePan = { dx, dy -> hostViewModel.onMousePan(dx, dy) },
                                onDisconnect = {
                                    hostViewModel.disconnect()
                                    navController.popBackStack()
                                },
                                onAdHocCommandClicked = { navController.navigate(Screen.AdHocCommand.route) },
                                onEditRemoteControlClicked = { page ->
                                    navController.navigate(
                                        Screen.EditRemoteControl.createRoute(
                                            page,
                                        ),
                                    )
                                },
                                onClearError = { hostViewModel.clearError() },
                                initialPage = initialPage,
                            )
                        }

                        composable(
                            Screen.EditRemoteControl.route,
                            arguments = listOf(
                                navArgument("initialPage") { type = NavType.IntType },
                            ),
                        ) { backStackEntry ->
                            val initialPage = backStackEntry.arguments?.getInt("initialPage")!!
                            EditRemoteControlScreen(
                                commands = uiState.host?.remoteCommands ?: emptyMap(),
                                initialCommands = uiState.host?.commands ?: emptyList(),
                                onSave = { remoteCommands, commands, navigateBack ->
                                    scope.launch {
                                        uiState.host?.let { host ->
                                            val updatedHost = host.copy(
                                                remoteCommands = remoteCommands,
                                                commands = commands,
                                            )
                                            hostViewModel.upsert(updatedHost)
                                            hostViewModel.updateActiveHostInUiState(updatedHost)
                                            if (navigateBack) {
                                                navController.popBackStack()
                                            }
                                        }
                                    }
                                },
                                onNavigateBack = { navController.popBackStack() },
                                onSetAsDefaultScreen = { startScreen ->
                                    scope.launch {
                                        uiState.host?.let { host ->
                                            val updatedHost = host.copy(startScreen = startScreen)
                                            hostViewModel.upsert(updatedHost)
                                            hostViewModel.updateActiveHostInUiState(updatedHost)
                                        }
                                    }
                                },
                                initialPage = initialPage,
                            )
                        }

                        composable(Screen.AdHocCommand.route) {
                            val adHocCommands by adHocCommandViewModel.adHocCommands.collectAsState()
                            AdHocCommandScreen(
                                commands = adHocCommands,
                                onExecuteCommand = { command, popUpToPrevious ->
                                    adHocCommandViewModel.addAdHocCommand(command)
                                    hostViewModel.runCommand(command, showOutput = true)
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
