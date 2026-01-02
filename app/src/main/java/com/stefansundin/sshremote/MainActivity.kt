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
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
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
import com.stefansundin.sshremote.ui.screens.RemoteControlScreen
import com.stefansundin.sshremote.ui.screens.SettingsScreen
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    data object HostList : Screen("host_list")
    data object HostEdit : Screen("host_edit/{hostId}?scan={scan}") {
        fun createRoute(hostId: Int?, scan: Boolean = false) = "host_edit/$hostId?scan=$scan"
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
    data object Help : Screen("help")
}

enum class Theme {
    SYSTEM,
    LIGHT,
    DARK
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
            sshRepository,
            cryptoManager,
            app.settingsRepository,
            app.passwordDao,
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

    private var shortcutHostId = mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG && savedInstanceState != null) {
            // Try to avoid activity recreation by adding to android:configChanges
            Log.w("MainActivity", "Activity recreated!")
        }
        enableEdgeToEdge()

        if (intent?.hasExtra("HOST_ID") == true) {
            shortcutHostId.value = intent.getIntExtra("HOST_ID", -1)
        }

        setContent {
            val theme by settingsViewModel.theme.collectAsState()
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

            SSHRemoteTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val scope = rememberCoroutineScope()
                    val uiState by hostViewModel.uiState.collectAsState()
                    val navController = rememberNavController()
                    val app = LocalContext.current.applicationContext as SshRemoteApplication
                    var showBackupRestoredDialog by rememberSaveable { mutableStateOf(app.isRestoredFromBackup) }
                    val hosts by hostViewModel.allHosts.collectAsState()

                    var hostForPresetSelection by remember { mutableStateOf<Host?>(null) }
                    var showGettingStartedDialog by rememberSaveable { mutableStateOf(false) }
                    var showSelectPresetDialog by rememberSaveable { mutableStateOf(false) }

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
                                    hostViewModel.updateRemoteCommands(host, presetMap)
                                    val initialPage = host.startScreen.tabIndex
                                    navController.navigate(Screen.RemoteControl.createRoute(host.id, initialPage))
                                }
                                showSelectPresetDialog = false
                            },
                        )
                    }

                    LaunchedEffect(hosts, shortcutHostId.value) {
                        if (!hosts.isNullOrEmpty()) {
                            val shortcutId = shortcutHostId.value
                            if (shortcutId != null) {
                                val hostToConnect = hosts?.find { it.id == shortcutId }
                                if (hostToConnect != null) {
                                    onConnect(hostToConnect)
                                    shortcutHostId.value = null
                                }
                            }
                        }
                    }

                    if (showBackupRestoredDialog) {
                        AlertDialog(
                            title = { Text("Restored from backup") },
                            text = { Text("The application data was restored from a backup. For security reasons, encrypted data such as SSH keys and passwords are not included in backups.\n\nPlease configure these again.") },
                            properties = DialogProperties(dismissOnClickOutside = false),
                            onDismissRequest = {
                                showBackupRestoredDialog = false
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showBackupRestoredDialog = false
                                    },
                                ) {
                                    Text("OK")
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
                                onAdd = {
                                    navController.navigate(Screen.HostEdit.createRoute(null))
                                },
                                onAddFromQrCode = {
                                    navController.navigate(Screen.HostEdit.createRoute(null, scan = true))
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
                            val hostId = backStackEntry.arguments?.getString("hostId")?.toIntOrNull()
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
                                scanQrCodeOnStart = scan,
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
                                navArgument("initialPage") { type = NavType.IntType },
                            ),
                        ) { backStackEntry ->
                            val initialPage = backStackEntry.arguments?.getInt("initialPage")!!
                            EditRemoteControlScreen(
                                commands = uiState.host?.remoteCommands ?: emptyMap(),
                                initialCommands = uiState.host?.commands ?: emptyList(),
                                initialSmartVolumeSettings = uiState.host?.smartVolume,
                                onSave = { remoteCommands, commands, smartVolume, navigateBack ->
                                    scope.launch {
                                        uiState.host?.let { host ->
                                            val updatedHost = host.copy(
                                                remoteCommands = remoteCommands,
                                                commands = commands,
                                                smartVolume = smartVolume,
                                            )
                                            hostViewModel.upsert(updatedHost)
                                            hostViewModel.updateActiveHostInUiState(updatedHost)
                                            if (navigateBack) {
                                                navController.safePopBackStack()
                                            }
                                        }
                                    }
                                },
                                onNavigateBack = { navController.safePopBackStack() },
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
                                onTestSmartVolumeSettings = {
                                    scope.launch {
                                        val volume = hostViewModel.readVolume()
                                        Toast.makeText(
                                            this@MainActivity,
                                            if (volume == null) "Error reading volume. Please install pactl." else "Volume: $volume",
                                            Toast.LENGTH_SHORT,
                                        )
                                            .show()
                                    }
                                },
                            )
                        }

                        composable(Screen.AdHocCommand.route) {
                            val adHocCommands by adHocCommandViewModel.adHocCommands.collectAsState()
                            AdHocCommandScreen(
                                uiState = uiState,
                                commands = adHocCommands,
                                onExecuteCommand = { command, popUpToPrevious ->
                                    adHocCommandViewModel.addAdHocCommand(command)
                                    hostViewModel.runCommand(command, showOutput = true)
                                    if (popUpToPrevious) {
                                        navController.safePopBackStack()
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
                                onNavigateUp = {
                                    navController.safePopBackStack()
                                },
                            )
                        }

                        composable(Screen.IdentityList.route) {
                            IdentityListScreen(
                                cryptoManager = cryptoManager,
                                identityViewModel = identityViewModel,
                                onNavigateToAddIdentity = { navController.navigate(Screen.AddIdentity.route) },
                                onNavigateUp = {
                                    navController.safePopBackStack()
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
                                    navController.safePopBackStack()
                                },
                                onKeyGenerated = { name, type, size, comment ->
                                    identityViewModel.generateAndInsert(name, type, size, comment)
                                    navController.safePopBackStack()
                                },
                                onNavigateUp = { navController.safePopBackStack() },
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
        if (intent.hasExtra("HOST_ID")) {
            val hostId = intent.getIntExtra("HOST_ID", -1)
            val uiState = hostViewModel.uiState.value
            if (uiState.connectionStatus == ConnectionStatus.CONNECTED && uiState.host?.id == hostId) {
                return
            }
            shortcutHostId.value = hostId
        }
    }

    private fun createShortcut(context: Context, host: Host) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            putExtra("HOST_ID", host.id)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val shortcutInfo = ShortcutInfoCompat.Builder(context, "host_${host.id}")
            .setShortLabel(host.name)
            .setLongLabel("Connect to ${host.name}")
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(intent)
            .build()

        ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }
}

@Composable
private fun GettingStartedDialog(onDismiss: () -> Unit, onConfirm: () -> Unit, onHelp: () -> Unit) {
    AlertDialog(
        title = { Text("Getting started") },
        text = { Text("The buttons on the remote control are mapped to specific commands that are executed on the host.\n\nYou have to install the appropriate utility on the host, which one depends on which window manager the host uses (X11 or Wayland).\n\nTo get started, please select a preset. You can always reset to a preset later by editing the remote control.\n\nPlease read the Help page if this is the first time you are using this app.") },
        properties = DialogProperties(dismissOnClickOutside = false),
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Select preset")
            }
        },
        dismissButton = {
            TextButton(onClick = onHelp) {
                Text("Help")
            }
        },
    )
}

@Composable
private fun SelectPresetDialog(onDismiss: () -> Unit, onPresetSelected: (Map<RemoteControlKey, Command>) -> Unit) {
    AlertDialog(
        title = { Text("Select preset") },
        text = {
            Column {
                (presets.keys + "No preset").forEach { presetKey ->
                    Text(
                        text = presetKey,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val preset = presets[presetKey] ?: emptyMap()
                                onPresetSelected(preset)
                            }
                            .padding(vertical = 12.dp),
                    )
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
                    val hostIdString = intent.getStringExtra(NotificationService.EXTRA_HOST_ID)
                    val remoteControlKeyString = intent.getStringExtra(NotificationService.EXTRA_REMOTE_CONTROL_KEY)

                    if (remoteControlKeyString != null && hostIdString != null) {
                        val hostId = hostIdString.toIntOrNull()
                        if (hostId == null) {
                            Log.e("CommandBroadcastReceiver", "Invalid host ID received: $hostIdString")
                            return
                        }

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
