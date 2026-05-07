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

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.view.SoundEffectConstants
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.jcraft.jsch.HostKey
import com.stefansundin.sshremote.HapticFeedback
import com.stefansundin.sshremote.HostKeyVerification
import com.stefansundin.sshremote.ISshRepository
import com.stefansundin.sshremote.Message
import com.stefansundin.sshremote.PassphrasePrompt
import com.stefansundin.sshremote.PasswordPrompt
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.Result
import com.stefansundin.sshremote.data.host.ConnectionStatus
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.HostConnectionDetails
import com.stefansundin.sshremote.data.host.IRemoteControlHostViewModel
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.data.host.RemoteUiState
import com.stefansundin.sshremote.data.identity.IRemoteControlIdentityViewModel
import com.stefansundin.sshremote.data.identity.Identity
import com.stefansundin.sshremote.data.settings.ISettingsViewModel
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
import com.stefansundin.sshremote.ui.components.ResponsiveTabRow
import com.stefansundin.sshremote.ui.components.SelectIdentityDialog
import com.stefansundin.sshremote.ui.components.SpecialKeysRow
import com.stefansundin.sshremote.ui.dpadFocusable
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.view.KeyEvent as AndroidKeyEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteControlScreen(
    host: Host,
    uiState: RemoteUiState,
    identityViewModel: IRemoteControlIdentityViewModel,
    hostViewModel: IRemoteControlHostViewModel,
    settingsViewModel: ISettingsViewModel,
    sshRepository: ISshRepository,
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
    val configuration = LocalConfiguration.current
    var showMenu by rememberSaveable { mutableStateOf(false) }
    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    var wasConnected by rememberSaveable { mutableStateOf(false) }

    val isInMultiWindowMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        (context as? Activity)?.isInMultiWindowMode == true
    } else {
        false
    }
    val systemBarsInsets = WindowInsets.systemBars
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    val hasSystemBars = remember {
        // Android TV does not have system bars. This check prevents the fullscreen button from appearing there.
        // Calculate hasSystemBars only once based on the initial state of the insets.
        // This prevents the fullscreen button from flickering when exiting fullscreen, which happens because the system bar insets briefly report as 0 during the transition.
        with(density) {
            systemBarsInsets.getTop(this) > 0 ||
                    systemBarsInsets.getBottom(this) > 0 ||
                    systemBarsInsets.getLeft(this, layoutDirection) > 0 ||
                    systemBarsInsets.getRight(this, layoutDirection) > 0
        }
    }
    val canToggleFullscreen = !isInMultiWindowMode && (hasSystemBars || isFullscreen)

    BackHandler {
        onDisconnect()
    }

    LaunchedEffect(isInMultiWindowMode, configuration) {
        if (isInMultiWindowMode) {
            isFullscreen = false
        }
    }

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
        }
    }

    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsState()

    LaunchedEffect(uiState.connectionStatus) {
        if (notificationsEnabled) {
            host.let { NotificationService.start(context, it.toNotificationHost(uiState.connectionStatus)) }
        }
    }

    LaunchedEffect(uiState.connectionStatus) {
        if (uiState.connectionStatus == ConnectionStatus.DISCONNECTED) {
            NotificationService.stop(context)
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

    LaunchedEffect(uiState.connectionStatus) {
        if (uiState.connectionStatus == ConnectionStatus.CONNECTED) {
            wasConnected = true
        }
    }

    LaunchedEffect(host) {
        if (uiState.hostId != host.id) {
            wasConnected = false
            hostViewModel.connect(host)
        }
    }

    // Reconnect in case of disconnection
    LaunchedEffect(host, uiState.connectionStatus, uiState.error, uiState.hostId) {
        if (uiState.hostId == host.id && uiState.connectionStatus == ConnectionStatus.DISCONNECTED && uiState.error == null && wasConnected) {
            hostViewModel.connect(host)
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
            title = { Text(stringResource(R.string.connection_error)) },
            text = {
                SelectionContainer {
                    Text(uiState.error)
                }
            },
            properties = DialogProperties(dismissOnClickOutside = false),
            onDismissRequest = onClearError,
            confirmButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onClearError()
                    },
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
    }

    hostKeyVerification?.let { verification ->
        AlertDialog(
            title = { Text(stringResource(R.string.host_key_verification)) },
            text = {
                SelectionContainer {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(verification.message)
                    }
                }
            },
            properties = DialogProperties(dismissOnClickOutside = false),
            onDismissRequest = { sshRepository.onHostKeyVerificationComplete(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        sshRepository.onHostKeyVerificationComplete(true)
                    },
                ) {
                    Text(stringResource(R.string.accept))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        sshRepository.onHostKeyVerificationComplete(false)
                    },
                ) {
                    Text(stringResource(R.string.reject))
                }
            },
        )
    }

    message?.let { msg ->
        AlertDialog(
            title = { Text(stringResource(R.string.message)) },
            text = {
                SelectionContainer {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(msg.message)
                    }
                }
            },
            properties = DialogProperties(dismissOnClickOutside = false),
            onDismissRequest = { sshRepository.onMessageDismissed() },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        sshRepository.onMessageDismissed()
                    },
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
    }

    passwordPrompt?.let { prompt ->
        var password by rememberSaveable { mutableStateOf("") }
        var passwordVisible by rememberSaveable { mutableStateOf(false) }
        AlertDialog(
            title = { Text(prompt.message) },
            text = {
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .dpadFocusable(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { sshRepository.onPasswordPromptComplete(password) }),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description =
                            stringResource(if (passwordVisible) R.string.hide_password else R.string.show_password)
                        IconButton(
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                passwordVisible = !passwordVisible
                            },
                        ) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                )
            },
            properties = DialogProperties(dismissOnClickOutside = false),
            onDismissRequest = { sshRepository.onPasswordPromptComplete(null) },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        sshRepository.onPasswordPromptComplete(password)
                    },
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        sshRepository.onPasswordPromptComplete(null)
                    },
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    passphrasePrompt?.let { prompt ->
        var passphrase by rememberSaveable { mutableStateOf("") }
        var passphraseVisible by rememberSaveable { mutableStateOf(false) }
        AlertDialog(
            title = { Text(prompt.message) },
            text = {
                TextField(
                    value = passphrase,
                    onValueChange = { passphrase = it },
                    label = { Text(stringResource(R.string.passphrase)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .dpadFocusable(),
                    visualTransformation = if (passphraseVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { sshRepository.onPassphrasePromptComplete(passphrase) }),
                    trailingIcon = {
                        val image = if (passphraseVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description =
                            stringResource(if (passphraseVisible) R.string.hide_passphrase else R.string.show_passphrase)
                        IconButton(
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                passphraseVisible = !passphraseVisible
                            },
                        ) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                )
            },
            properties = DialogProperties(dismissOnClickOutside = false),
            onDismissRequest = { sshRepository.onPassphrasePromptComplete(null) },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        sshRepository.onPassphrasePromptComplete(passphrase)
                    },
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        sshRepository.onPassphrasePromptComplete(null)
                    },
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showSelectIdentityDialog) {
        val identities by identityViewModel.identities.collectAsState()
        if (identities == null) {
            AlertDialog(
                title = { Text(stringResource(R.string.select_public_key)) },
                text = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                },
                onDismissRequest = { showSelectIdentityDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            showSelectIdentityDialog = false
                        },
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        } else {
            val publicKeyCopiedMsg = stringResource(R.string.public_key_copied)
            SelectIdentityDialog(
                identities = identities!!,
                onIdentitySelected = {
                    coroutineScope.launch {
                        val publicKey = identityViewModel.getPublicKey(it)
                        val command =
                            "exec sh -c 'cd; umask 077; mkdir -p ~/.ssh; echo \"\n$publicKey\" >> ~/.ssh/authorized_keys'"
                        val result = hostViewModel.runCommand(
                            command = command,
                            showOutput = false,
                            isRetry = false,
//                            reuseShell = false,
                        )
                        if (result is Result.Success) {
                            snackbarHostState.showSnackbar(publicKeyCopiedMsg)
                        }
                    }
                    showSelectIdentityDialog = false
                },
                onDismiss = { showSelectIdentityDialog = false },
            )
        }
    }

    val pagerState = rememberPagerState(initialPage = initialPage) { 4 }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var pressedSpecialKeys by rememberSaveable { mutableStateOf(emptySet<Int>()) }

    val runKeyboardCommand = { keyCode: Int, remoteControlKey: RemoteControlKey ->
        host.remoteCommands?.get(remoteControlKey)
            ?.let { commandTemplate ->
                try {
                    val commandRaw = commandTemplate.command
                    val command = if (commandRaw.contains("%d")) {
                        val linuxKeyCode = getLinuxKeyCode(keyCode)
                        commandRaw.replace("%d", linuxKeyCode.toString())
                    } else {
                        val keyName = getKeyName(keyCode)
                        try {
                            commandRaw.format(keyName)
                        } catch (_: Exception) {
                            commandRaw
                        }
                    }

                    coroutineScope.launch {
                        hostViewModel.runCommand(command, commandTemplate.showOutput)
                    }
                } catch (_: IllegalArgumentException) {
                    // Ignore unsupported keys
                }
            }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != 2) {
            // Release any pressed special keys when leaving the keyboard tab
            pressedSpecialKeys.forEach { key ->
                runKeyboardCommand(key, RemoteControlKey.KEYBOARD_KEY_UP)
            }
            pressedSpecialKeys = emptySet()

            // Hide the virtual keyboard when the Keyboard tab is no longer focused:
            focusManager.clearFocus()
            // Focus the remote control so volume hardware buttons can be intercepted:
            focusRequester.requestFocus()
        }
    }

    BoxWithConstraints(
        modifier = modifier.onPreviewKeyEvent {
            if (host.smartVolume?.controlVolumeWithHardwareButtons == true) {
                when (it.nativeKeyEvent.keyCode) {
                    AndroidKeyEvent.KEYCODE_VOLUME_DOWN -> {
                        if (it.nativeKeyEvent.action == AndroidKeyEvent.ACTION_DOWN) {
                            hostViewModel.runRemoteControlCommand(RemoteControlKey.VOLUME_DOWN)
                        }
                        return@onPreviewKeyEvent true
                    }

                    AndroidKeyEvent.KEYCODE_VOLUME_UP -> {
                        if (it.nativeKeyEvent.action == AndroidKeyEvent.ACTION_DOWN) {
                            hostViewModel.runRemoteControlCommand(RemoteControlKey.VOLUME_UP)
                        }
                        return@onPreviewKeyEvent true
                    }
                }
            }
            false
        },
    ) {
        val showTopBar = maxHeight > 400.dp
        val showTabs = maxHeight > 300.dp

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                if (showTopBar) {
                    TopAppBar(
                        title = { Text(host.name, maxLines = 1) },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    onDisconnect()
                                },
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.disconnect),
                                )
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
                            if (canToggleFullscreen) {
                                IconButton(
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        isFullscreen = !isFullscreen
                                    },
                                ) {
                                    Icon(
                                        if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                        contentDescription = stringResource(if (isFullscreen) R.string.exit_fullscreen else R.string.fullscreen),
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    showMenu = !showMenu
                                },
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options))
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.ad_hoc_command)) },
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        showMenu = false
                                        onAdHocCommandClicked()
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.edit_remote_control)) },
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        showMenu = false
                                        onEditRemoteControlClicked(pagerState.currentPage)
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.push_public_key)) },
                                    enabled = uiState.connectionStatus == ConnectionStatus.CONNECTED,
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        showMenu = false
                                        showSelectIdentityDialog = true
                                    },
                                )
                            }
                        },
                    )
                }
            },
            modifier = Modifier.fillMaxSize(),
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .focusRequester(focusRequester)
                    .focusable(),
            ) {
                val tabTitles = listOf(
                    stringResource(R.string.tab_remote),
                    stringResource(R.string.tab_mouse),
                    stringResource(R.string.tab_keyboard),
                    stringResource(R.string.tab_commands),
                )

                if (showTabs) {
                    ResponsiveTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        edgePadding = 0.dp,
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            key(index) {
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        coroutineScope.launch { pagerState.scrollToPage(index) }
                                    },
                                    text = { Text(text = title, maxLines = 1) },
                                )
                            }
                        }
                    }
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
                                onKeyEvent = { event: KeyEvent ->
                                    val key = event.key
                                    val command = host.remoteCommands?.get(key) ?: return@RemoteControl
                                    when (event) {
                                        is KeyEvent.Click -> {
                                            performHapticFeedback(context, uiState.hapticFeedback)
                                            hostViewModel.runRemoteControlCommand(key)
                                        }

                                        is KeyEvent.LongPress -> {
                                            performHapticFeedback(context, uiState.hapticFeedback)
                                            command.longPressCommand?.let {
                                                coroutineScope.launch {
                                                    hostViewModel.runCommand(it, command.showOutput)
                                                }
                                            }
                                        }
                                    }
                                },
                                host = host,
                                connectionStatus = uiState.connectionStatus,
                                volume = uiState.volume,
                                muted = uiState.muted,
                            )
                        }

                        1 -> {
                            MousePad(
                                host = host,
                                connectionStatus = uiState.connectionStatus,
                                onMouseEvent = { event ->
                                    if (event is MouseEvent.LeftClick || event is MouseEvent.RightClick ||
                                        event is MouseEvent.LeftDown || event is MouseEvent.RightDown
                                    ) {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        performHapticFeedback(context, uiState.hapticFeedback)
                                    }
                                    when (event) {
                                        is MouseEvent.Move -> {
                                            host.remoteCommands?.get(RemoteControlKey.MOUSE_MOVE)
                                                ?.let { commandTemplate ->
                                                    onMouseMove(event.dx, event.dy, commandTemplate.command)
                                                }
                                        }

                                        MouseEvent.LeftClick -> {
                                            hostViewModel.runRemoteControlCommand(RemoteControlKey.MOUSE_LEFT_CLICK)
                                        }

                                        MouseEvent.RightClick -> {
                                            hostViewModel.runRemoteControlCommand(RemoteControlKey.MOUSE_RIGHT_CLICK)
                                        }

                                        MouseEvent.LeftDown -> {
                                            hostViewModel.runRemoteControlCommand(RemoteControlKey.MOUSE_LEFT_DOWN)
                                        }

                                        MouseEvent.LeftUp -> {
                                            hostViewModel.runRemoteControlCommand(RemoteControlKey.MOUSE_LEFT_UP)
                                        }

                                        MouseEvent.RightDown -> {
                                            hostViewModel.runRemoteControlCommand(RemoteControlKey.MOUSE_RIGHT_DOWN)
                                        }

                                        MouseEvent.RightUp -> {
                                            hostViewModel.runRemoteControlCommand(RemoteControlKey.MOUSE_RIGHT_UP)
                                        }

                                        is MouseEvent.Pan -> {
                                            onMousePan(event.dx, event.dy)
                                        }
                                    }
                                },
                            )
                        }

                        2 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .imePadding(),
                            ) {
                                KeyboardInput(
                                    isCurrentlySelected = pagerState.currentPage == 2,
                                    onKey = { key ->
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        performHapticFeedback(context, uiState.hapticFeedback)
                                        runKeyboardCommand(key, RemoteControlKey.KEYBOARD_KEY_INPUT)
                                    },
                                    onType = { text ->
                                        host.remoteCommands?.get(RemoteControlKey.KEYBOARD_TYPE_INPUT)
                                            ?.let { commandTemplate ->
                                                val escapedText = text.replace("'", "'\\''")
                                                val command = commandTemplate.command.format(escapedText)
                                                coroutineScope.launch {
                                                    hostViewModel.runCommand(
                                                        command,
                                                        commandTemplate.showOutput,
                                                    )
                                                }
                                            }
                                    },
                                    host = host,
                                    connectionStatus = uiState.connectionStatus,
                                    modifier = Modifier.weight(1f),
                                )
                                SpecialKeysRow(
                                    onKey = { key ->
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        performHapticFeedback(context, uiState.hapticFeedback)
                                        runKeyboardCommand(key, RemoteControlKey.KEYBOARD_KEY_INPUT)
                                    },
                                    onKeyDown = { key ->
                                        performHapticFeedback(context, uiState.hapticFeedback)
                                        pressedSpecialKeys = pressedSpecialKeys + key
                                        runKeyboardCommand(key, RemoteControlKey.KEYBOARD_KEY_DOWN)
                                    },
                                    onKeyUp = { key ->
                                        performHapticFeedback(context, uiState.hapticFeedback)
                                        pressedSpecialKeys = pressedSpecialKeys - key
                                        runKeyboardCommand(key, RemoteControlKey.KEYBOARD_KEY_UP)
                                    },
                                    pressedKeys = pressedSpecialKeys,
                                    host = host,
                                    connectionStatus = uiState.connectionStatus,
                                )
                            }
                        }

                        3 -> {
                            CommandList(
                                commands = host.commands,
                                hostViewModel = hostViewModel,
                                connectionStatus = uiState.connectionStatus,
                            )
                        }
                    }
                }
            }
        }
    }
}

private val fakeRemoteControlIdentityViewModel = object : IRemoteControlIdentityViewModel {
    override val identities: StateFlow<List<Identity>?> = MutableStateFlow(emptyList())
    override suspend fun getPublicKey(identity: Identity): String = ""
}

private val fakeRemoteControlHostViewModel = object : IRemoteControlHostViewModel {
    override fun connect(host: Host) {}
    override fun runRemoteControlCommand(key: RemoteControlKey) {}
    override fun clearCommandOutput() {}
    override suspend fun runCommand(
        command: String,
        showOutput: Boolean,
        isRetry: Boolean,
//        reuseShell: Boolean,
    ): Result {
        return Result.Success("")
    }
}

private val fakeSshRepository = object : ISshRepository {
    override val hostKeyVerification: StateFlow<HostKeyVerification?> = MutableStateFlow(null)
    override val message: StateFlow<Message?> = MutableStateFlow(null)
    override val passwordPrompt: StateFlow<PasswordPrompt?> = MutableStateFlow(null)
    override val passphrasePrompt: StateFlow<PassphrasePrompt?> = MutableStateFlow(null)
    override suspend fun connect(details: HostConnectionDetails): HostKey? = null
    override fun onHostKeyVerificationComplete(result: Boolean) {}
    override fun onMessageDismissed() {}
    override fun onPasswordPromptComplete(password: String?) {}
    override fun onPassphrasePromptComplete(passphrase: String?) {}
    override suspend fun executeCommand(command: String): Result = Result.Success("")
    override suspend fun executeCommandReuseShell(command: String): Result = Result.Success("")
    override suspend fun disconnect() {}
}

@Composable
fun RemoteControlScreenPreview(
    modifier: Modifier = Modifier,
    host: Host = sampleHost,
    uiState: RemoteUiState = RemoteUiState(
        hostId = sampleHost.id,
        connectionStatus = ConnectionStatus.CONNECTED,
        isLoading = false,
        error = null,
        commandOutput = null,
        volume = "75%",
        muted = false,
        hapticFeedback = HapticFeedback.Medium,
    ),
    identityViewModel: IRemoteControlIdentityViewModel = fakeRemoteControlIdentityViewModel,
    hostViewModel: IRemoteControlHostViewModel = fakeRemoteControlHostViewModel,
    settingsViewModel: ISettingsViewModel = fakeSettingsViewModel,
    sshRepository: ISshRepository = fakeSshRepository,
    onMouseMove: (Float, Float, String) -> Unit = { _, _, _ -> },
    onMousePan: (Float, Float) -> Unit = { _, _ -> },
    onDisconnect: () -> Unit = {},
    onAdHocCommandClicked: () -> Unit = {},
    onEditRemoteControlClicked: (Int) -> Unit = {},
    onClearError: () -> Unit = {},
    initialPage: Int = 0,
) {
    RemoteControlScreen(
        host = host,
        uiState = uiState,
        identityViewModel = identityViewModel,
        hostViewModel = hostViewModel,
        settingsViewModel = settingsViewModel,
        sshRepository = sshRepository,
        onMouseMove = onMouseMove,
        onMousePan = onMousePan,
        onDisconnect = onDisconnect,
        onAdHocCommandClicked = onAdHocCommandClicked,
        onEditRemoteControlClicked = onEditRemoteControlClicked,
        onClearError = onClearError,
        modifier = modifier,
        initialPage = initialPage,
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun RemoteControlScreenPreview_RemoteTab() {
    SSHRemoteTheme {
        RemoteControlScreenPreview(initialPage = 0)
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun RemoteControlScreenPreview_MouseTab() {
    SSHRemoteTheme {
        RemoteControlScreenPreview(initialPage = 1)
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun RemoteControlScreenPreview_KeyboardTab() {
    SSHRemoteTheme {
        RemoteControlScreenPreview(initialPage = 2)
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun RemoteControlScreenPreview_CommandsTab() {
    SSHRemoteTheme {
        RemoteControlScreenPreview(initialPage = 3)
    }
}

private fun getLinuxKeyCode(keyCode: Int): Int {
    return when (keyCode) {
        AndroidKeyEvent.KEYCODE_DEL -> 14
        AndroidKeyEvent.KEYCODE_FORWARD_DEL -> 111
        AndroidKeyEvent.KEYCODE_ESCAPE -> 1
        AndroidKeyEvent.KEYCODE_TAB -> 15
        AndroidKeyEvent.KEYCODE_CAPS_LOCK -> 58
        AndroidKeyEvent.KEYCODE_SHIFT_LEFT -> 42
        AndroidKeyEvent.KEYCODE_CTRL_LEFT -> 29
        AndroidKeyEvent.KEYCODE_META_LEFT -> 125
        AndroidKeyEvent.KEYCODE_ALT_LEFT -> 56
        AndroidKeyEvent.KEYCODE_INSERT -> 110
        AndroidKeyEvent.KEYCODE_MOVE_HOME -> 102
        AndroidKeyEvent.KEYCODE_MOVE_END -> 107
        AndroidKeyEvent.KEYCODE_PAGE_UP -> 104
        AndroidKeyEvent.KEYCODE_PAGE_DOWN -> 109
        AndroidKeyEvent.KEYCODE_DPAD_UP -> 103
        AndroidKeyEvent.KEYCODE_DPAD_DOWN -> 108
        AndroidKeyEvent.KEYCODE_DPAD_LEFT -> 105
        AndroidKeyEvent.KEYCODE_DPAD_RIGHT -> 106
        else -> throw IllegalArgumentException("Unsupported key code: $keyCode")
    }
}

private fun getKeyName(keyCode: Int): String {
    return when (keyCode) {
        AndroidKeyEvent.KEYCODE_DEL -> "BackSpace"
        AndroidKeyEvent.KEYCODE_FORWARD_DEL -> "Delete"
        AndroidKeyEvent.KEYCODE_ESCAPE -> "Escape"
        AndroidKeyEvent.KEYCODE_TAB -> "Tab"
        AndroidKeyEvent.KEYCODE_CAPS_LOCK -> "Caps_Lock"
        AndroidKeyEvent.KEYCODE_SHIFT_LEFT -> "Shift_L"
        AndroidKeyEvent.KEYCODE_CTRL_LEFT -> "Control_L"
        AndroidKeyEvent.KEYCODE_META_LEFT -> "Super_L"
        AndroidKeyEvent.KEYCODE_ALT_LEFT -> "Alt_L"
        AndroidKeyEvent.KEYCODE_INSERT -> "Insert"
        AndroidKeyEvent.KEYCODE_MOVE_HOME -> "Home"
        AndroidKeyEvent.KEYCODE_MOVE_END -> "End"
        AndroidKeyEvent.KEYCODE_PAGE_UP -> "Page_Up"
        AndroidKeyEvent.KEYCODE_PAGE_DOWN -> "Page_Down"
        AndroidKeyEvent.KEYCODE_DPAD_UP -> "Up"
        AndroidKeyEvent.KEYCODE_DPAD_DOWN -> "Down"
        AndroidKeyEvent.KEYCODE_DPAD_LEFT -> "Left"
        AndroidKeyEvent.KEYCODE_DPAD_RIGHT -> "Right"
        else -> throw IllegalArgumentException("Unsupported key code: $keyCode")
    }
}
