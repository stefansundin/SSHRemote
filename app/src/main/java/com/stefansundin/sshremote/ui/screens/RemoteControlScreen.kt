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

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.stefansundin.sshremote.SshRepository
import com.stefansundin.sshremote.data.host.ConnectionStatus
import com.stefansundin.sshremote.data.host.HostViewModel
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.data.host.RemoteUiState
import com.stefansundin.sshremote.data.identity.IdentityViewModel
import com.stefansundin.sshremote.data.settings.SettingsViewModel
import com.stefansundin.sshremote.notification.NotificationService
import com.stefansundin.sshremote.notification.toNotificationHost
import com.stefansundin.sshremote.performHapticFeedback
import com.stefansundin.sshremote.ui.KeyEvent
import com.stefansundin.sshremote.ui.MouseEvent
import com.stefansundin.sshremote.ui.components.CommandList
import com.stefansundin.sshremote.ui.components.CommandOutputDialog
import com.stefansundin.sshremote.ui.components.ConnectionStatusIndicator
import com.stefansundin.sshremote.ui.components.KeyboardInput
import com.stefansundin.sshremote.ui.components.MousePad
import com.stefansundin.sshremote.ui.components.RemoteControl
import com.stefansundin.sshremote.ui.components.SelectIdentityDialog
import com.stefansundin.sshremote.ui.components.SpecialKeysRow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RemoteControlScreen(
    uiState: RemoteUiState,
    identityViewModel: IdentityViewModel,
    hostViewModel: HostViewModel,
    settingsViewModel: SettingsViewModel,
    sshRepository: SshRepository,
    onMouseMove: (Float, Float, String) -> Unit,
    onMousePan: (Float, Float) -> Unit,
    onDisconnect: () -> Unit,
    onAdHocCommandClicked: () -> Unit,
    onEditRemoteControlClicked: (Int) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
) {
    val context = LocalContext.current
    var showMenu by rememberSaveable { mutableStateOf(false) }
    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    var repeatJob by remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSelectIdentityDialog by rememberSaveable { mutableStateOf(false) }

    val view = LocalView.current
    val keepScreenOn by settingsViewModel.keepScreenOn.collectAsState()

    DisposableEffect(keepScreenOn) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            if (keepScreenOn) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        onDispose {
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                if (keepScreenOn) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
            NotificationService.stop(context) // TODO: Remove this when NotificationService can maintain its own SSH connection
        }
    }

    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsState()

    LaunchedEffect(uiState.connectionStatus) {
        if (notificationsEnabled) {
            uiState.host?.let { NotificationService.start(context, it.toNotificationHost(uiState.connectionStatus)) }
        }
    }

    LaunchedEffect(isFullscreen) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, view)
            if (isFullscreen) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    LaunchedEffect(uiState.host) {
        if (uiState.host != null && uiState.connectionStatus == ConnectionStatus.DISCONNECTED) {
            hostViewModel.connect(uiState.host)
        }
    }

    val hostKeyVerification by sshRepository.hostKeyVerification.collectAsState()
    val message by sshRepository.message.collectAsState()
    val passwordPrompt by sshRepository.passwordPrompt.collectAsState()
    val passphrasePrompt by sshRepository.passphrasePrompt.collectAsState()

    uiState.commandOutput?.let { output ->
        CommandOutputDialog(
            output = output,
            onDismiss = { hostViewModel.clearCommandOutput() },
        )
    }

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = onClearError,
            title = { Text("Connection Error") },
            text = { Text(uiState.error) },
            confirmButton = {
                TextButton(onClick = onClearError) {
                    Text("OK")
                }
            },
        )
    }

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

    passwordPrompt?.let { prompt ->
        var password by rememberSaveable { mutableStateOf("") }
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

    passphrasePrompt?.let { prompt ->
        var passphrase by rememberSaveable { mutableStateOf("") }
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

    if (showSelectIdentityDialog) {
        val identities by identityViewModel.identities.collectAsState()
        SelectIdentityDialog(
            identities = identities,
            onIdentitySelected = {
                coroutineScope.launch {
                    val publicKey = identityViewModel.getPublicKey(it)
                    val command =
                        """exec sh -c 'cd; umask 077; echo "\n$publicKey" >> ~/.ssh/authorized_keys'"""
                    hostViewModel.runCommand(
                        command = command,
                        showOutput = false,
                        isRetry = false,
                        reuseShell = false,
                    )
                    snackbarHostState.showSnackbar("Public key copied to host.")
                }
                showSelectIdentityDialog = false
            },
            onDismiss = { showSelectIdentityDialog = false },
        )
    }

    val pagerState = rememberPagerState(initialPage = initialPage) { 4 }

    // Hide the virtual keyboard when the Keyboard tab is no longer focused:
    val focusManager = LocalFocusManager.current
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != 2) {
            focusManager.clearFocus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.host?.name ?: "Remote") },
                navigationIcon = {
                    IconButton(onClick = onDisconnect) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Disconnect")
                    }
                },
                actions = {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(24.dp),
                        )
                    }
                    ConnectionStatusIndicator(
                        connectionStatus = uiState.connectionStatus,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    IconButton(onClick = { isFullscreen = !isFullscreen }) {
                        Icon(
                            if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (isFullscreen) "Exit fullscreen" else "Fullscreen",
                        )
                    }
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ad-hoc command") },
                            onClick = {
                                showMenu = false
                                onAdHocCommandClicked()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Edit remote control") },
                            onClick = {
                                showMenu = false
                                onEditRemoteControlClicked(pagerState.currentPage)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Copy public key") },
                            onClick = {
                                showMenu = false
                                showSelectIdentityDialog = true
                            },
                        )
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        val commands = uiState.host?.remoteCommands ?: emptyMap()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { coroutineScope.launch { pagerState.scrollToPage(0) } },
                    text = { Text("Remote") },
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { coroutineScope.launch { pagerState.scrollToPage(1) } },
                    text = { Text("Mouse") },
                )
                Tab(
                    selected = pagerState.currentPage == 2,
                    onClick = { coroutineScope.launch { pagerState.scrollToPage(2) } },
                    text = { Text("Keyboard") },
                )
                Tab(
                    selected = pagerState.currentPage == 3,
                    onClick = { coroutineScope.launch { pagerState.scrollToPage(3) } },
                    text = { Text("Commands") },
                )
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,
                userScrollEnabled = false,
            ) { page ->
                when (page) {
                    0 -> {
                        RemoteControl(
                            commands = commands,
                            onKeyEvent = { event ->
                                val command = when (event) {
                                    is KeyEvent.Down -> commands[event.key]
                                    is KeyEvent.Up -> commands[event.key]
                                    is KeyEvent.Click -> commands[event.key]
                                } ?: return@RemoteControl
                                when (event) {
                                    is KeyEvent.Down -> {
                                        if (command.repeat) {
                                            repeatJob?.cancel()
                                            repeatJob = coroutineScope.launch {
                                                performHapticFeedback(context, uiState.hapticFeedback)
                                                hostViewModel.runCommand(
                                                    command.command,
                                                    command.showOutput,
                                                ) // Fire once immediately
                                                delay(500) // Initial delay
                                                while (isActive) {
                                                    performHapticFeedback(context, uiState.hapticFeedback)
                                                    hostViewModel.runCommand(command.command, command.showOutput)
                                                    delay(100) // Repeat rate
                                                }
                                            }
                                        }
                                    }

                                    is KeyEvent.Up -> {
                                        if (command.repeat) {
                                            repeatJob?.cancel()
                                        }
                                    }

                                    is KeyEvent.Click -> {
                                        if (!command.repeat) {
                                            performHapticFeedback(context, uiState.hapticFeedback)
                                            hostViewModel.runCommand(command.command, command.showOutput)
                                        }

                                    }
                                }
                            },
                        )
                    }

                    1 -> {
                        MousePad(
                            commands = commands,
                            onMouseEvent = { event ->
                                if (event is MouseEvent.LeftClick || event is MouseEvent.RightClick) {
                                    performHapticFeedback(context, uiState.hapticFeedback)
                                }
                                when (event) {
                                    is MouseEvent.Move -> {
                                        commands[RemoteControlKey.MOUSE_MOVE]?.let { commandTemplate ->
                                            onMouseMove(event.dx, event.dy, commandTemplate.command)
                                        }
                                    }

                                    MouseEvent.LeftClick -> {
                                        commands[RemoteControlKey.MOUSE_LEFT_CLICK]?.let {
                                            hostViewModel.runCommand(it.command, it.showOutput)
                                        }
                                    }

                                    MouseEvent.RightClick -> {
                                        commands[RemoteControlKey.MOUSE_RIGHT_CLICK]?.let {
                                            hostViewModel.runCommand(it.command, it.showOutput)
                                        }
                                    }

                                    is MouseEvent.Pan -> {
                                        onMousePan(event.dx, event.dy)
                                    }
                                }
                            },
                        )
                    }

                    2 -> {
                        val onKey = { key: String ->
                            commands[RemoteControlKey.KEYBOARD_KEY_INPUT]?.let { commandTemplate ->
                                val command = commandTemplate.command.format(key)
                                hostViewModel.runCommand(command, commandTemplate.showOutput)
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .imePadding(),
                        ) {
                            KeyboardInput(
                                isCurrentlySelected = pagerState.currentPage == 2,
                                onKey = { key -> onKey(key) },
                                onType = { text ->
                                    commands[RemoteControlKey.KEYBOARD_TYPE_INPUT]?.let { commandTemplate ->
                                        val escapedText = text.replace("'", "'\\''")
                                        val command = commandTemplate.command.format(escapedText)
                                        hostViewModel.runCommand(command, commandTemplate.showOutput)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                commands = commands,
                            )
                            SpecialKeysRow(
                                onKey = { key -> onKey(key) },
                                commands = commands,
                            )
                        }
                    }

                    3 -> {
                        CommandList(
                            uiState = uiState,
                            hostViewModel = hostViewModel,
                        )
                    }
                }
            }
        }
    }
}
