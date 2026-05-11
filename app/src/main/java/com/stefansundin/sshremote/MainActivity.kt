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

package com.stefansundin.sshremote

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SoundEffectConstants
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stefansundin.sshremote.data.CryptoManager
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommandViewModel
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommandViewModelFactory
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.ConnectionStatus
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.HostViewModel
import com.stefansundin.sshremote.data.host.HostViewModelFactory
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.data.host.presets
import com.stefansundin.sshremote.data.identity.IdentityViewModel
import com.stefansundin.sshremote.data.identity.IdentityViewModelFactory
import com.stefansundin.sshremote.data.knownhost.KnownHostViewModel
import com.stefansundin.sshremote.data.knownhost.KnownHostViewModelFactory
import com.stefansundin.sshremote.data.settings.SettingsViewModel
import com.stefansundin.sshremote.data.settings.SettingsViewModelFactory
import com.stefansundin.sshremote.notification.NotificationService
import com.stefansundin.sshremote.ui.screens.AdHocCommandScreen
import com.stefansundin.sshremote.ui.screens.AddIdentityScreen
import com.stefansundin.sshremote.ui.screens.EditHostScreen
import com.stefansundin.sshremote.ui.screens.EditRemoteControlScreen
import com.stefansundin.sshremote.ui.screens.HelpScreen
import com.stefansundin.sshremote.ui.screens.HostListScreen
import com.stefansundin.sshremote.ui.screens.IdentityListScreen
import com.stefansundin.sshremote.ui.screens.KnownHostListScreen
import com.stefansundin.sshremote.ui.screens.RemoteControlScreen
import com.stefansundin.sshremote.ui.screens.SettingsScreen
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    data object HostList : Screen("host_list")
    data object HostEdit : Screen("host_edit/{hostId}?scan={scan}") {
        fun createRoute(hostId: String?, scan: Boolean = false) = "host_edit/$hostId?scan=$scan"
    }

    data object RemoteControl : Screen("remote_control/{hostId}/{initialPage}") {
        fun createRoute(hostId: String, initialPage: Int = 0) = "remote_control/$hostId/$initialPage"
    }

    data object EditRemoteControl : Screen("edit_remote_control/{hostId}/{initialPage}") {
        fun createRoute(hostId: String, initialPage: Int = 0) = "edit_remote_control/$hostId/$initialPage"
    }

    data object AdHocCommand : Screen("ad_hoc_command")
    data object Settings : Screen("settings")
    data object IdentityList : Screen("identity_list")
    data object KnownHostList : Screen("known_host_list")
    data object AddIdentity : Screen("add_identity?scan={scan}") {
        fun createRoute(scan: Boolean = false) = "add_identity?scan=$scan"
    }

    data object Help : Screen("help")
}

enum class Theme(@StringRes val labelRes: Int) {
    SYSTEM(R.string.theme_system),
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark)
}

private data class PendingShortcut(
    val hostId: String,
    val commandId: String? = null,
    val remoteControlKey: String? = null,
    val connectionRequested: Boolean = false,
) {
    val hasPayload: Boolean
        get() = commandId != null || remoteControlKey != null
}

private fun NavController.safePopBackStack() {
    if (previousBackStackEntry != null) {
        popBackStack()
    }
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
            app.knownHostRepository,
            sshRepository,
            cryptoManager,
            app.settingsRepository,
            app.passwordDao,
        )
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        val app = (application as SshRemoteApplication)
        SettingsViewModelFactory(
            app.settingsRepository,
            app.hostRepository,
            app.knownHostRepository,
            app.adHocCommandRepository,
        )
    }

    private val identityViewModel: IdentityViewModel by viewModels {
        val app = (application as SshRemoteApplication)
        IdentityViewModelFactory(
            app.identityRepository,
            cryptoManager,
        )
    }

    private val knownHostViewModel: KnownHostViewModel by viewModels {
        val app = (application as SshRemoteApplication)
        KnownHostViewModelFactory(app.knownHostRepository)
    }

    private val adHocCommandViewModel: AdHocCommandViewModel by viewModels {
        val app = (application as SshRemoteApplication)
        AdHocCommandViewModelFactory(app.adHocCommandRepository)
    }

    private var pendingShortcut = mutableStateOf<PendingShortcut?>(null)
    private var sharedText = mutableStateOf<String?>(null)

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            sharedText.value = intent.getStringExtra(Intent.EXTRA_TEXT)
            return
        }
        if (intent.hasExtra("HOST_ID")) {
            val hostId = intent.getStringExtra("HOST_ID") ?: return
            pendingShortcut.value = PendingShortcut(
                hostId = hostId,
                commandId = intent.getStringExtra("COMMAND_ID"),
                remoteControlKey = intent.getStringExtra("REMOTE_CONTROL_KEY"),
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG && savedInstanceState != null) {
            // Try to avoid activity recreation by adding to android:configChanges
            Log.w("MainActivity", "Activity recreated!")
        }
        enableEdgeToEdge()
        handleIncomingIntent(intent)

        setContent {
            val theme by settingsViewModel.theme.collectAsState()
            val useDynamicColors by settingsViewModel.useDynamicColors.collectAsState()
            val backgroundColor by settingsViewModel.backgroundColor.collectAsState()
            val primaryColor by settingsViewModel.primaryColor.collectAsState()
            val onPrimaryColor by settingsViewModel.onPrimaryColor.collectAsState()
            val useDarkTheme = when (theme) {
                Theme.SYSTEM -> isSystemInDarkTheme()
                Theme.LIGHT -> false
                Theme.DARK -> true
            }

            DisposableEffect(useDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { useDarkTheme },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { useDarkTheme },
                )
                onDispose {}
            }

            CommandBroadcastReceiver(hostViewModel)

            SSHRemoteTheme(
                theme,
                useDynamicColors,
                {
                    var scheme = this
                    if (backgroundColor != null) {
                        scheme = scheme.copy(
                            background = backgroundColor!!,
                            surface = backgroundColor!!,
                        )
                    }
                    if (primaryColor != null) {
                        scheme = scheme.copy(primary = primaryColor!!)
                    }
                    if (onPrimaryColor != null) {
                        scheme = scheme.copy(onPrimary = onPrimaryColor!!)
                    }
                    scheme
                },
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val view = LocalView.current
                    val scope = rememberCoroutineScope()
                    val uiState by hostViewModel.uiState.collectAsState()
                    val navController = rememberNavController()
                    val currentRoute = navController.currentBackStackEntry?.destination?.route
                    val app = LocalContext.current.applicationContext as SshRemoteApplication
                    var showBackupRestoredDialog by rememberSaveable { mutableStateOf(app.isRestoredFromBackup) }
                    val hosts by hostViewModel.allHosts.collectAsState()
                    val shareTargetEnabled by settingsViewModel.shareTargetEnabled.collectAsState()

                    var hostForPresetSelection by remember { mutableStateOf<Host?>(null) }
                    var showGettingStartedDialog by rememberSaveable { mutableStateOf(false) }
                    var showSelectPresetDialog by rememberSaveable { mutableStateOf(false) }
                    var showShareNotConnectedDialog by rememberSaveable { mutableStateOf(false) }
                    var showMissingShareCommandDialog by rememberSaveable { mutableStateOf(false) }

                    // Auto-run a command after connecting via a home-screen shortcut.
                    LaunchedEffect(
                        uiState.connectionStatus,
                        uiState.hostId,
                        hosts,
                        pendingShortcut.value,
                        currentRoute,
                    ) {
                        val shortcut = pendingShortcut.value ?: return@LaunchedEffect
                        val allHosts = hosts ?: return@LaunchedEffect
                        if (shortcut.hasPayload && currentRoute?.startsWith("edit_remote_control") == true) {
                            pendingShortcut.value = null
                            Toast.makeText(
                                this@MainActivity,
                                getString(R.string.shortcut_save_changes_before_using),
                                Toast.LENGTH_LONG,
                            ).show()
                            return@LaunchedEffect
                        }
                        if (
                            !shortcut.hasPayload ||
                            uiState.connectionStatus != ConnectionStatus.CONNECTED ||
                            uiState.hostId != shortcut.hostId
                        ) {
                            return@LaunchedEffect
                        }

                        val host = allHosts.find { it.id == shortcut.hostId }
                        if (host == null) {
                            pendingShortcut.value = null
                            Toast.makeText(
                                this@MainActivity,
                                getString(R.string.shortcut_host_not_found),
                                Toast.LENGTH_SHORT,
                            ).show()
                            return@LaunchedEffect
                        }

                        when {
                            shortcut.commandId != null -> {
                                pendingShortcut.value = null
                                val command = host.commands.find { it.id == shortcut.commandId }
                                if (command != null) {
                                    scope.launch {
                                        hostViewModel.runCommand(
                                            command = command.command,
                                            showOutput = command.showOutput,
                                            renderOutputAsMarkdown = command.renderOutputAsMarkdown,
                                        )
                                    }
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        getString(R.string.shortcut_command_not_found),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            }

                            shortcut.remoteControlKey != null -> {
                                pendingShortcut.value = null
                                try {
                                    val remoteKey = RemoteControlKey.valueOf(shortcut.remoteControlKey)
                                    if (host.remoteCommands?.get(remoteKey) != null) {
                                        scope.launch {
                                            hostViewModel.runRemoteControlCommand(remoteKey)
                                        }
                                    } else {
                                        Toast.makeText(
                                            this@MainActivity,
                                            getString(R.string.shortcut_command_not_found),
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    }
                                } catch (e: IllegalArgumentException) {
                                    Log.e(
                                        "MainActivity",
                                        "Invalid RemoteControlKey in shortcut: ${shortcut.remoteControlKey}",
                                        e,
                                    )
                                    Toast.makeText(
                                        this@MainActivity,
                                        getString(R.string.shortcut_remote_control_key_not_found),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            }
                        }
                    }

                    // Enable/disable the share target activity alias based on setting
                    LaunchedEffect(shareTargetEnabled) {
                        val componentName =
                            android.content.ComponentName(this@MainActivity, "${packageName}.ShareTargetActivity")
                        val newState = if (shareTargetEnabled) {
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        } else {
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                        }
                        packageManager.setComponentEnabledSetting(componentName, newState, PackageManager.DONT_KILL_APP)
                    }

                    // Handle incoming shared text
                    LaunchedEffect(sharedText.value, uiState.connectionStatus, uiState.hostId, hosts) {
                        val text = sharedText.value ?: return@LaunchedEffect
                        if (uiState.connectionStatus == ConnectionStatus.CONNECTED) {
                            val host = hosts?.find { it.id == uiState.hostId }
                            val commandTemplate = host?.remoteCommands?.get(RemoteControlKey.SHARE_TEXT)
                                ?: host?.remoteCommands?.get(RemoteControlKey.KEYBOARD_TYPE_INPUT)
                            if (commandTemplate?.command?.isNotEmpty() == true) {
                                sharedText.value = null
                                val escapedText = text.replace("'", "'\\''")
                                val command = commandTemplate.command.format(escapedText)
                                // Run in a stable scope so this work is not canceled when sharedText changes
                                scope.launch {
                                    hostViewModel.runCommand(command, commandTemplate.showOutput)
                                }
                            } else {
                                sharedText.value = null
                                showMissingShareCommandDialog = true
                            }
                        } else {
                            sharedText.value = null
                            showShareNotConnectedDialog = true
                        }
                    }

                    if (showShareNotConnectedDialog) {
                        ShareNotConnectedDialog(
                            onDismiss = { showShareNotConnectedDialog = false },
                        )
                    }

                    if (showMissingShareCommandDialog) {
                        ShareMissingShareCommandDialog(
                            onDismiss = { showMissingShareCommandDialog = false },
                        )
                    }

                    val onConnect = { host: Host ->
                        if (host.remoteCommands == null) {
                            hostForPresetSelection = host
                            showGettingStartedDialog = true
                        } else {
                            hostViewModel.disconnect()
                            val initialPage = host.startScreen.tabIndex
                            navController.navigate(Screen.RemoteControl.createRoute(host.id, initialPage)) {
                                popUpTo(Screen.HostList.route)
                            }
                        }
                    }

                    if (showGettingStartedDialog) {
                        GettingStartedDialog(
                            onDismiss = { showGettingStartedDialog = false },
                            onConfirm = {
                                showGettingStartedDialog = false
                                showSelectPresetDialog = true
                            },
                            onHelp = {
                                showGettingStartedDialog = false
                                navController.navigate(Screen.Help.route)
                            },
                        )
                    }

                    if (showSelectPresetDialog) {
                        SelectPresetDialog(
                            onDismiss = { showSelectPresetDialog = false },
                            onPresetSelected = { presetMap ->
                                hostForPresetSelection?.let { host ->
                                    if (presetMap != null) {
                                        hostViewModel.updateRemoteCommands(host.id, presetMap)
                                    }
                                    val initialPage = host.startScreen.tabIndex
                                    navController.navigate(Screen.RemoteControl.createRoute(host.id, initialPage))
                                }
                                showSelectPresetDialog = false
                            },
                        )
                    }

                    LaunchedEffect(hosts, pendingShortcut.value, uiState.connectionStatus, uiState.hostId) {
                        val shortcut = pendingShortcut.value ?: return@LaunchedEffect
                        val allHosts = hosts ?: return@LaunchedEffect
                        val hostToConnect = allHosts.find { it.id == shortcut.hostId }

                        if (hostToConnect == null) {
                            pendingShortcut.value = null
                            Toast.makeText(
                                this@MainActivity,
                                getString(R.string.shortcut_host_not_found),
                                Toast.LENGTH_SHORT,
                            ).show()
                            return@LaunchedEffect
                        }

                        val isSameConnectedHost =
                            uiState.connectionStatus == ConnectionStatus.CONNECTED && uiState.hostId == hostToConnect.id

                        if (isSameConnectedHost && !shortcut.hasPayload) {
                            pendingShortcut.value = null
                        } else if (!isSameConnectedHost && !shortcut.connectionRequested) {
                            pendingShortcut.value = shortcut.copy(connectionRequested = true)
                            onConnect(hostToConnect)
                        }
                    }

                    if (showBackupRestoredDialog) {
                        AlertDialog(
                            title = { Text(stringResource(R.string.restored_from_backup_title)) },
                            text = { Text(stringResource(R.string.restored_from_backup_text)) },
                            properties = DialogProperties(dismissOnClickOutside = false),
                            onDismissRequest = {
                                showBackupRestoredDialog = false
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        showBackupRestoredDialog = false
                                    },
                                ) {
                                    Text(stringResource(R.string.ok))
                                }
                            },
                        )
                    }

                    NavHost(
                        navController = navController,
                        startDestination = Screen.HostList.route,
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                    ) {
                        composable(Screen.HostList.route) {
                            HostListScreen(
                                hosts = hosts,
                                onConnectClicked = onConnect,
                                onAdd = { openQrScanner ->
                                    navController.navigate(Screen.HostEdit.createRoute(null, scan = openQrScanner))
                                },
                                onEdit = { host ->
                                    navController.navigate(Screen.HostEdit.createRoute(host.id))
                                },
                                onClone = { host ->
                                    hostViewModel.cloneHost(host) { newHostId ->
                                        navController.navigate(Screen.HostEdit.createRoute(newHostId))
                                    }
                                },
                                onCreateShortcut = { host ->
                                    createShortcut(this@MainActivity, host)
                                },
                                onDelete = { host -> hostViewModel.delete(host) },
                                onUndoDelete = { hostViewModel.undoDelete() },
                                onSettings = {
                                    navController.navigate(Screen.Settings.route)
                                },
                                onHelp = {
                                    navController.navigate(Screen.Help.route)
                                },
                            )
                        }

                        composable(
                            Screen.HostEdit.route,
                            arguments = listOf(
                                navArgument("hostId") { type = NavType.StringType; nullable = true },
                                navArgument("scan") { type = NavType.BoolType; defaultValue = false },
                            ),
                        ) { backStackEntry ->
                            val hostId = backStackEntry.arguments?.getString("hostId")
                            val scan = backStackEntry.arguments?.getBoolean("scan") ?: false
                            val host = hosts?.find { it.id == hostId }
                            val identities by identityViewModel.identities.collectAsState()
                            EditHostScreen(
                                host = host,
                                identities = identities,
                                allUsers = hosts?.map { it.user } ?: emptyList(),
                                onSave = { newHost, password ->
                                    scope.launch {
                                        hostViewModel.saveHost(newHost, password)
                                        navController.safePopBackStack()
                                    }
                                },
                                onNavigateUp = {
                                    navController.safePopBackStack()
                                },
                                hostViewModel = hostViewModel,
                                settingsViewModel = settingsViewModel,
                                scanQrCodeOnStart = scan,
                            )
                        }

                        composable(
                            Screen.RemoteControl.route,
                            arguments = listOf(
                                navArgument("hostId") { type = NavType.StringType },
                                navArgument("initialPage") { type = NavType.IntType },
                            ),
                        ) { backStackEntry ->
                            val hostId = backStackEntry.arguments?.getString("hostId")
                            val initialPage = backStackEntry.arguments?.getInt("initialPage")!!
                            val host = hosts?.find { it.id == hostId }
                            if (host != null) {
                                RemoteControlScreen(
                                    host = host,
                                    uiState = uiState,
                                    identityViewModel = identityViewModel,
                                    hostViewModel = hostViewModel,
                                    sshRepository = sshRepository,
                                    onMouseMove = { dx, dy, template -> hostViewModel.onMouseMove(dx, dy, template) },
                                    onMousePan = { dx, dy -> hostViewModel.onMousePan(dx, dy) },
                                    onDisconnect = {
                                        hostViewModel.disconnect()
                                        navController.safePopBackStack()
                                    },
                                    onAdHocCommandClicked = { navController.navigate(Screen.AdHocCommand.route) },
                                    onEditRemoteControlClicked = { page ->
                                        navController.navigate(
                                            Screen.EditRemoteControl.createRoute(
                                                host.id,
                                                page,
                                            ),
                                        )
                                    },
                                    onClearError = { hostViewModel.clearError() },
                                    initialPage = initialPage,
                                    settingsViewModel = settingsViewModel,
                                )
                            } else {
                                if (hosts == null) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                } else {
                                    LaunchedEffect(Unit) {
                                        navController.safePopBackStack()
                                    }
                                }
                            }
                        }

                        composable(
                            Screen.EditRemoteControl.route,
                            arguments = listOf(
                                navArgument("hostId") { type = NavType.StringType },
                                navArgument("initialPage") { type = NavType.IntType },
                            ),
                        ) { backStackEntry ->
                            val hostId = backStackEntry.arguments?.getString("hostId")
                            val initialPage = backStackEntry.arguments?.getInt("initialPage")!!
                            val host = hosts?.find { it.id == hostId }

                            if (host != null) {
                                EditRemoteControlScreen(
                                    host = host,
                                    onSave = { remoteCommands, commands, smartVolume, navigateBack ->
                                        scope.launch {
                                            val updatedHost = host.copy(
                                                remoteCommands = remoteCommands,
                                                commands = commands,
                                                smartVolume = smartVolume,
                                            )
                                            hostViewModel.upsert(updatedHost)
                                            if (navigateBack) {
                                                navController.safePopBackStack()
                                            }
                                        }
                                    },
                                    onNavigateBack = { navController.safePopBackStack() },
                                    onSetAsDefaultScreen = { startScreen ->
                                        scope.launch {
                                            hostViewModel.updateStartScreen(host.id, startScreen)
                                        }
                                    },
                                    onTestSmartVolumeSettings = {
                                        scope.launch {
                                            val volume = hostViewModel.readVolume()
                                            val message = if (volume == null) {
                                                getString(R.string.error_reading_volume)
                                            } else {
                                                getString(R.string.volume_format, volume)
                                            }
                                            Toast.makeText(
                                                this@MainActivity,
                                                message,
                                                Toast.LENGTH_SHORT,
                                            )
                                                .show()
                                        }
                                    },
                                    onAddCommandShortcut = { shortcutHost, commandId ->
                                        createCommandShortcut(this@MainActivity, shortcutHost, commandId)
                                    },
                                    onAddRemoteCommandShortcut = { shortcutHost, key ->
                                        createRemoteCommandShortcut(this@MainActivity, shortcutHost, key)
                                    },
                                    shareTargetEnabled = shareTargetEnabled,
                                    initialPage = initialPage,
                                )
                            } else {
                                LaunchedEffect(Unit) {
                                    navController.safePopBackStack()
                                }
                            }
                        }

                        composable(Screen.AdHocCommand.route) {
                            val adHocCommands by adHocCommandViewModel.adHocCommands.collectAsState()
                            AdHocCommandScreen(
                                uiState = uiState,
                                commands = adHocCommands,
                                onExecuteCommand = { command, popUpToPrevious ->
                                    scope.launch {
                                        adHocCommandViewModel.addAdHocCommand(command)
                                        hostViewModel.runCommand(command, showOutput = true)
                                        if (popUpToPrevious) {
                                            navController.safePopBackStack()
                                        }
                                    }
                                },
                                onDeleteCommand = { adHocCommandViewModel.deleteAdHocCommand(it) },
                                onClearHistory = { adHocCommandViewModel.clearAdHocCommands() },
                                onNavigateUp = { navController.safePopBackStack() },
                                onClearCommandOutput = { hostViewModel.clearCommandOutput() },
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                settingsViewModel = settingsViewModel,
                                onNavigateToIdentityList = { navController.navigate(Screen.IdentityList.route) },
                                onNavigateToKnownHostList = { navController.navigate(Screen.KnownHostList.route) },
                                onNavigateUp = {
                                    navController.safePopBackStack()
                                },
                            )
                        }

                        composable(Screen.IdentityList.route) {
                            IdentityListScreen(
                                cryptoManager = cryptoManager,
                                identityViewModel = identityViewModel,
                                onNavigateToAddIdentity = { openQrScanner ->
                                    navController.navigate(Screen.AddIdentity.createRoute(openQrScanner))
                                },
                                onNavigateUp = {
                                    navController.safePopBackStack()
                                },
                                onDelete = { identity -> identityViewModel.delete(identity) },
                                onRename = { identity, newName -> identityViewModel.rename(identity, newName) },
                                onAttachCertificate = { identity, cert ->
                                    identityViewModel.attachCertificate(
                                        identity,
                                        cert,
                                    )
                                },
                                onDeleteCertificate = { identity -> identityViewModel.deleteCertificate(identity) },
                                onUndoDelete = { identityViewModel.undoDelete() },
                            )
                        }

                        composable(Screen.KnownHostList.route) {
                            KnownHostListScreen(
                                knownHostViewModel = knownHostViewModel,
                                onNavigateUp = { navController.safePopBackStack() },
                            )
                        }

                        composable(
                            Screen.AddIdentity.route,
                            arguments = listOf(
                                navArgument("scan") { type = NavType.BoolType; defaultValue = false },
                            ),
                        ) { backStackEntry ->
                            val scan = backStackEntry.arguments?.getBoolean("scan") ?: false
                            AddIdentityScreen(
                                onKeysSaved = { keys ->
                                    keys.forEach { key ->
                                        identityViewModel.insert(
                                            name = key.name,
                                            privateKey = key.privateKey,
                                            certificate = key.certificate,
                                        )
                                    }
                                    navController.safePopBackStack()
                                },
                                onKeyGenerated = { name, type, size, comment ->
                                    identityViewModel.generateAndInsert(name, type, size, comment)
                                    navController.safePopBackStack()
                                },
                                onNavigateUp = { navController.safePopBackStack() },
                                scanQrCodeOnStart = scan,
                            )
                        }

                        composable(Screen.Help.route) {
                            HelpScreen(
                                onNavigateUp = { navController.safePopBackStack() },
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            handleIncomingIntent(intent)
            return
        }
        if (intent.hasExtra("HOST_ID")) {
            val hostId = intent.getStringExtra("HOST_ID")
            val uiState = hostViewModel.uiState.value
            val hasCommandPayload = intent.hasExtra("COMMAND_ID") || intent.hasExtra("REMOTE_CONTROL_KEY")
            if (uiState.connectionStatus == ConnectionStatus.CONNECTED && uiState.hostId == hostId && !hasCommandPayload) {
                return
            }
            handleIncomingIntent(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NotificationService.stop(this)
    }

    private fun createShortcut(context: Context, host: Host) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            putExtra("HOST_ID", host.id)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val shortcutInfo = ShortcutInfoCompat.Builder(context, "host_${host.id}")
            .setShortLabel(host.name)
            .setLongLabel(getString(R.string.connect_to_host, host.name))
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(intent)
            .build()

        ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }

    private fun createCommandShortcut(context: Context, host: Host, commandId: String) {
        val command = host.commands.find { it.id == commandId }
        val commandLabel = command?.name ?: command?.command ?: getString(R.string.command)
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            putExtra("HOST_ID", host.id)
            putExtra("COMMAND_ID", commandId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val shortcutInfo = ShortcutInfoCompat.Builder(context, "command_${host.id}_$commandId")
            .setShortLabel(getString(R.string.run_command_on_host_short, commandLabel, host.name))
            .setLongLabel(getString(R.string.run_command_on_host_long, commandLabel, host.name))
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(intent)
            .build()

        ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }

    private fun createRemoteCommandShortcut(context: Context, host: Host, key: RemoteControlKey) {
        val command = host.remoteCommands?.get(key)
        val commandLabel = command?.name ?: getString(key.titleRes)
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            putExtra("HOST_ID", host.id)
            putExtra("REMOTE_CONTROL_KEY", key.name)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val shortcutInfo = ShortcutInfoCompat.Builder(context, "remote_command_${host.id}_${key.name}")
            .setShortLabel(getString(R.string.run_command_on_host_short, commandLabel, host.name))
            .setLongLabel(getString(R.string.run_command_on_host_long, commandLabel, host.name))
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(intent)
            .build()

        ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }
}

@Composable
private fun GettingStartedDialog(onDismiss: () -> Unit, onConfirm: () -> Unit, onHelp: () -> Unit) {
    val view = LocalView.current

    AlertDialog(
        title = { Text(stringResource(R.string.getting_started_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.getting_started_text))
            }
        },
        properties = DialogProperties(dismissOnClickOutside = false),
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onConfirm()
                },
            ) {
                Text(stringResource(R.string.select_preset))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onHelp()
                },
            ) {
                Text(stringResource(R.string.help))
            }
        },
    )
}

@Composable
private fun SelectPresetDialog(onDismiss: () -> Unit, onPresetSelected: (Map<RemoteControlKey, Command>?) -> Unit) {
    val view = LocalView.current
    val noPreset = stringResource(R.string.no_preset)

    AlertDialog(
        title = { Text(stringResource(R.string.select_preset)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                (presets.keys + noPreset).forEach { presetKey ->
                    ListItem(
                        headlineContent = { Text(presetKey) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                onPresetSelected(presets[presetKey])
                            },
                    )
                    HorizontalDivider()
                }
            }
        },
        properties = DialogProperties(dismissOnClickOutside = false),
        onDismissRequest = onDismiss,
        confirmButton = {},
    )
}

@Composable
fun CommandBroadcastReceiver(hostViewModel: HostViewModel) {
    val context = LocalContext.current

    DisposableEffect(context, hostViewModel) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == NotificationService.ACTION_EXECUTE_COMMAND) {
                    val hostId = intent.getStringExtra(NotificationService.EXTRA_HOST_ID)
                    val remoteControlKeyString = intent.getStringExtra(NotificationService.EXTRA_REMOTE_CONTROL_KEY)

                    if (remoteControlKeyString != null && hostId != null) {
                        val remoteControlKey: RemoteControlKey
                        try {
                            remoteControlKey = RemoteControlKey.valueOf(remoteControlKeyString)
                        } catch (e: IllegalArgumentException) {
                            Log.e(
                                "CommandBroadcastReceiver",
                                "Invalid RemoteControlKey received: $remoteControlKeyString",
                                e,
                            )
                            return
                        }

                        val allHosts = hostViewModel.allHosts.value
                        val targetHost = allHosts?.find { it.id == hostId }

                        if (targetHost != null) {
                            hostViewModel.runRemoteControlCommand(remoteControlKey)
                            Log.d("CommandBroadcastReceiver", "Executing command on host: ${targetHost.name}")
                        } else {
                            Log.w("CommandBroadcastReceiver", "Host with ID $hostId not found.")
                        }
                    }
                }
            }
        }

        val intentFilter = IntentFilter(NotificationService.ACTION_EXECUTE_COMMAND)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.registerReceiver(context, receiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, intentFilter)
        }

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
}

@Composable
private fun ShareNotConnectedDialog(onDismiss: () -> Unit) {
    val view = LocalView.current

    AlertDialog(
        title = { Text(stringResource(R.string.share_not_connected_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.share_not_connected_text))
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
private fun ShareMissingShareCommandDialog(onDismiss: () -> Unit) {
    val view = LocalView.current

    AlertDialog(
        title = { Text(stringResource(R.string.share_missing_share_command_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.share_missing_share_command_text))
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

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 600,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun GettingStartedDialogPreview() {
    SSHRemoteTheme {
        Surface {
            GettingStartedDialog(onDismiss = {}, onConfirm = {}, onHelp = {})
        }
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
private fun SelectPresetDialogPreview() {
    SSHRemoteTheme {
        Surface {
            SelectPresetDialog(onDismiss = {}, onPresetSelected = {})
        }
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
private fun ShareNotConnectedDialogPreview() {
    SSHRemoteTheme {
        Surface {
            ShareNotConnectedDialog(onDismiss = {})
        }
    }
}
