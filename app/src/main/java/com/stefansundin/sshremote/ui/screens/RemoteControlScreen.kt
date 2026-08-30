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
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
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
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.stefansundin.sshremote.LocalNetworkPermissions
import com.stefansundin.sshremote.Message
import com.stefansundin.sshremote.PassphrasePrompt
import com.stefansundin.sshremote.PasswordPrompt
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.Result
import com.stefansundin.sshremote.rememberLocalNetworkPermissionRequest
import com.stefansundin.sshremote.data.host.ConnectionStatus
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.HostConnectionDetails
import com.stefansundin.sshremote.data.host.IRemoteControlHostViewModel
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.data.host.RemoteUiState
import com.stefansundin.sshremote.data.host.buildPhysicalKeyBindingMap
import com.stefansundin.sshremote.data.identity.IRemoteControlIdentityViewModel
import com.stefansundin.sshremote.data.identity.Identity
import com.stefansundin.sshremote.data.settings.ISettingsViewModel
import com.stefansundin.sshremote.notification.NotificationController
import com.stefansundin.sshremote.notification.toNotificationHost
import com.stefansundin.sshremote.performHapticFeedback
import com.stefansundin.sshremote.ui.HardwareMenuKeyHandler
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
import com.stefansundin.sshremote.ui.theme.AppDimens
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
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
    val activity = context as? Activity
    var showMenu by rememberSaveable { mutableStateOf(false) }
    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    var wasConnected by rememberSaveable { mutableStateOf(false) }
    val ensureLocalNetworkPermission = rememberLocalNetworkPermissionRequest()
    val permissionDeniedMsg = stringResource(R.string.permission_denied)

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

    val showWhenLocked by settingsViewModel.showWhenLocked.collectAsState()
    val disconnectFromRemote = {
        activity?.setShowWhenLockedEnabled(false)
        onDisconnect()
    }

    BackHandler {
        disconnectFromRemote()
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

    DisposableEffect(activity, view, keepScreenOn, showWhenLocked) {
        val window = activity?.window
        if (window != null) {
            if (keepScreenOn) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        activity?.setShowWhenLockedEnabled(showWhenLocked)
        onDispose {
            activity?.setShowWhenLockedEnabled(false)
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

    LaunchedEffect(notificationsEnabled, host, uiState.connectionStatus) {
        if (notificationsEnabled && uiState.connectionStatus != ConnectionStatus.DISCONNECTED) {
            NotificationController.show(context, host.toNotificationHost(uiState.connectionStatus))
        } else {
            NotificationController.stop(context)
        }
    }

    LaunchedEffect(isFullscreen, showMenu, uiState.commandOutput) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, view)
            if (isFullscreen) {
                // Dialogs (like CommandOutputDialog) and the menu can make system bars visible on Android 7 and earlier; reapply immersive mode when that happens.
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
            if (ensureLocalNetworkPermission(host.hostname)) {
                hostViewModel.connect(host)
            } else {
                Toast.makeText(context, permissionDeniedMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Reconnect in case of disconnection
    LaunchedEffect(host, uiState.connectionStatus, uiState.error, uiState.hostId) {
        if (uiState.hostId == host.id && uiState.connectionStatus == ConnectionStatus.DISCONNECTED && uiState.error == null && wasConnected) {
            if (!ensureLocalNetworkPermission(host.hostname)) {
                Toast.makeText(context, permissionDeniedMsg, Toast.LENGTH_SHORT).show()
                return@LaunchedEffect
            }
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
            renderMarkdown = uiState.commandOutputIsMarkdown,
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
                        val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description =
                            stringResource(if (passwordVisible) R.string.hide_password else R.string.show_password)

                        TooltipBox(
                            state = rememberTooltipState(),
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                TooltipAnchorPosition.Below,
                                AppDimens.tooltipAnchorOffset,
                            ),
                            tooltip = {
                                PlainTooltip {
                                    Text(description)
                                }
                            },
                        ) {
                            IconButton(
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    passwordVisible = !passwordVisible
                                },
                            ) {
                                Icon(icon, description)
                            }
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
                        val icon = if (passphraseVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description =
                            stringResource(if (passphraseVisible) R.string.hide_passphrase else R.string.show_passphrase)

                        TooltipBox(
                            state = rememberTooltipState(),
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                TooltipAnchorPosition.Below,
                                AppDimens.tooltipAnchorOffset,
                            ),
                            tooltip = {
                                PlainTooltip {
                                    Text(description)
                                }
                            },
                        ) {
                            IconButton(
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    passphraseVisible = !passphraseVisible
                                },
                            ) {
                                Icon(icon, description)
                            }
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
                            reuseShell = false,
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
    val keyboardController = LocalSoftwareKeyboardController.current
    val imeInsets = WindowInsets.ime
    var pressedSpecialKeys by rememberSaveable { mutableStateOf(emptySet<Int>()) }
    val physicalKeyBindings = remember(host.remoteCommands) { buildPhysicalKeyBindingMap(host.remoteCommands) }
    val longPressTimeoutMillis = remember { ViewConfiguration.getLongPressTimeout().toLong() }
    val activePhysicalKeyStates = remember { mutableMapOf<Int, PhysicalKeyPressState>() }
    val activePressReleaseCounts = remember { mutableMapOf<RemoteControlKey, Int>() }
    val pressedPhysicalRemoteKeys = remember { mutableStateMapOf<RemoteControlKey, Int>() }
    val physicallyPressedKeys = pressedPhysicalRemoteKeys.keys.toSet()

    fun markPhysicalRemoteKeyPressed(remoteKey: RemoteControlKey) {
        val currentCount = pressedPhysicalRemoteKeys[remoteKey] ?: 0
        pressedPhysicalRemoteKeys[remoteKey] = currentCount + 1
    }

    fun markPhysicalRemoteKeyReleased(remoteKey: RemoteControlKey) {
        val currentCount = pressedPhysicalRemoteKeys[remoteKey] ?: return
        if (currentCount <= 1) {
            pressedPhysicalRemoteKeys.remove(remoteKey)
        } else {
            pressedPhysicalRemoteKeys[remoteKey] = currentCount - 1
        }
    }

    val hideKeyboard = {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        activity?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).hide(WindowInsetsCompat.Type.ime())
        }
    }

    fun handleRemoteControlEvent(event: KeyEvent) {
        val key = event.key
        val command = host.remoteCommands?.get(key) ?: return

        fun runRemoteCommand(commandText: String?) {
            val text = commandText ?: return
            coroutineScope.launch {
                hostViewModel.runCommand(
                    text,
                    command.showOutput,
                    command.renderOutputAsMarkdown,
                )
            }
        }

        when (event) {
            is KeyEvent.Click -> {
                performHapticFeedback(context, uiState.hapticFeedback)
                hostViewModel.runRemoteControlCommand(key)
            }

            is KeyEvent.LongPress -> {
                performHapticFeedback(context, uiState.hapticFeedback)
                runRemoteCommand(command.longPressCommand)
            }

            is KeyEvent.Down -> {
                performHapticFeedback(context, uiState.hapticFeedback)
                hostViewModel.runRemoteControlPressCommand(key)
            }

            is KeyEvent.Up -> {
                performHapticFeedback(context, uiState.hapticFeedback)
                hostViewModel.runRemoteControlReleaseCommand(key)
            }
        }
    }

    fun clearActivePhysicalKeys(sendReleaseEvents: Boolean) {
        activePhysicalKeyStates.values.forEach { state ->
            state.longPressJob?.cancel()
            state.repeatJob?.cancel()
        }

        if (sendReleaseEvents) {
            activePressReleaseCounts.keys.forEach { remoteKey ->
                handleRemoteControlEvent(KeyEvent.Up(remoteKey))
            }
        }

        activePhysicalKeyStates.clear()
        activePressReleaseCounts.clear()
        pressedPhysicalRemoteKeys.clear()
    }

    fun handlePhysicalKeyEvent(nativeKeyEvent: AndroidKeyEvent): Boolean {
        if (pagerState.currentPage != 0) {
            return false
        }

        val keyCode = nativeKeyEvent.keyCode
        val remoteKey = physicalKeyBindings[keyCode] ?: return false
        val command = host.remoteCommands?.get(remoteKey) ?: return false

        if (!command.hasRemoteAction()) {
            return false
        }

        return when (nativeKeyEvent.action) {
            AndroidKeyEvent.ACTION_DOWN -> {
                if (nativeKeyEvent.repeatCount > 0 || activePhysicalKeyStates.containsKey(keyCode)) {
                    true
                } else {
                    val state = PhysicalKeyPressState(
                        remoteKey = remoteKey,
                        usesPressReleaseCommands = command.usesPressReleaseCommands(),
                    )
                    activePhysicalKeyStates[keyCode] = state
                    markPhysicalRemoteKeyPressed(remoteKey)

                    when {
                        state.usesPressReleaseCommands -> {
                            val currentCount = activePressReleaseCounts[remoteKey] ?: 0
                            activePressReleaseCounts[remoteKey] = currentCount + 1
                            if (currentCount == 0) {
                                handleRemoteControlEvent(KeyEvent.Down(remoteKey))
                            }
                        }

                        command.repeat -> {
                            handleRemoteControlEvent(KeyEvent.Click(remoteKey))
                            state.repeatJob = coroutineScope.launch {
                                delay(500.milliseconds)
                                while (isActive && activePhysicalKeyStates.containsKey(keyCode)) {
                                    handleRemoteControlEvent(KeyEvent.Click(remoteKey))
                                    delay(100.milliseconds)
                                }
                            }
                        }

                        command.hasLongPressCommand() -> {
                            state.longPressJob = coroutineScope.launch {
                                delay(longPressTimeoutMillis.milliseconds)
                                if (activePhysicalKeyStates[keyCode] === state) {
                                    state.longPressTriggered = true
                                    handleRemoteControlEvent(KeyEvent.LongPress(remoteKey))
                                }
                            }
                        }
                    }

                    true
                }
            }

            AndroidKeyEvent.ACTION_UP -> {
                val state = activePhysicalKeyStates.remove(keyCode)
                state?.longPressJob?.cancel()
                state?.repeatJob?.cancel()

                if (state != null) {
                    if (state.usesPressReleaseCommands) {
                        val currentCount = activePressReleaseCounts[state.remoteKey] ?: 0
                        if (currentCount <= 1) {
                            activePressReleaseCounts.remove(state.remoteKey)
                            handleRemoteControlEvent(KeyEvent.Up(state.remoteKey))
                        } else {
                            activePressReleaseCounts[state.remoteKey] = currentCount - 1
                        }
                    } else if (state.repeatJob == null && !state.longPressTriggered) {
                        handleRemoteControlEvent(KeyEvent.Click(state.remoteKey))
                    }

                    markPhysicalRemoteKeyReleased(state.remoteKey)
                }

                true
            }

            else -> true
        }
    }

    val runKeyboardCommand = { keyCode: Int, remoteControlKey: RemoteControlKey ->
        host.remoteCommands?.get(remoteControlKey)
            ?.let { commandTemplate ->
                try {
                    val commandRaw = commandTemplate.command ?: return@let
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
        if (pagerState.currentPage != 0) {
            clearActivePhysicalKeys(sendReleaseEvents = uiState.connectionStatus == ConnectionStatus.CONNECTED)
        }
        if (pagerState.currentPage != 2) {
            // Release any pressed special keys when leaving the keyboard tab
            pressedSpecialKeys.forEach { key ->
                runKeyboardCommand(key, RemoteControlKey.KEYBOARD_KEY_UP)
            }
            pressedSpecialKeys = emptySet()

            // Hide the virtual keyboard when the Keyboard tab is no longer focused:
            hideKeyboard()
            // Focus the remote control so volume hardware buttons can be intercepted:
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(pagerState, imeInsets, density) {
        snapshotFlow { pagerState.currentPage to (imeInsets.getBottom(density) > 0) }
            .collect { (currentPage, isImeVisible) ->
                if (currentPage != 2 && isImeVisible) {
                    hideKeyboard()
                }
            }
    }

    LaunchedEffect(uiState.connectionStatus) {
        if (uiState.connectionStatus != ConnectionStatus.CONNECTED) {
            clearActivePhysicalKeys(sendReleaseEvents = false)
        }
    }

    DisposableEffect(host.id) {
        onDispose {
            clearActivePhysicalKeys(sendReleaseEvents = false)
        }
    }

    HardwareMenuKeyHandler {
        showMenu = true
        true
    }

    BoxWithConstraints(
        modifier = modifier.onPreviewKeyEvent {
            val nativeKeyEvent = it.nativeKeyEvent
            if (host.smartVolume?.controlVolumeWithHardwareButtons == true) {
                when (nativeKeyEvent.keyCode) {
                    AndroidKeyEvent.KEYCODE_VOLUME_DOWN -> {
                        if (nativeKeyEvent.action == AndroidKeyEvent.ACTION_DOWN) {
                            hostViewModel.runRemoteControlCommand(RemoteControlKey.VOLUME_DOWN)
                        }
                        return@onPreviewKeyEvent true
                    }

                    AndroidKeyEvent.KEYCODE_VOLUME_UP -> {
                        if (nativeKeyEvent.action == AndroidKeyEvent.ACTION_DOWN) {
                            hostViewModel.runRemoteControlCommand(RemoteControlKey.VOLUME_UP)
                        }
                        return@onPreviewKeyEvent true
                    }
                }
            }
            handlePhysicalKeyEvent(nativeKeyEvent)
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
                            TooltipBox(
                                state = rememberTooltipState(),
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                    TooltipAnchorPosition.Below,
                                    AppDimens.tooltipAnchorOffset,
                                ),
                                tooltip = {
                                    PlainTooltip {
                                        Text(stringResource(R.string.disconnect))
                                    }
                                },
                            ) {
                                IconButton(
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        disconnectFromRemote()
                                    },
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.disconnect))
                                }
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
                            TooltipBox(
                                state = rememberTooltipState(),
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                    TooltipAnchorPosition.Below,
                                    AppDimens.tooltipAnchorOffset,
                                ),
                                tooltip = {
                                    PlainTooltip {
                                        Text(stringResource(uiState.connectionStatus.labelRes))
                                    }
                                },
                            ) {
                                ConnectionStatusIndicator(
                                    connectionStatus = uiState.connectionStatus,
                                    modifier = Modifier.padding(end = 8.dp),
                                )
                            }
                            if (canToggleFullscreen) {
                                val icon = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen
                                val description =
                                    stringResource(if (isFullscreen) R.string.exit_fullscreen else R.string.fullscreen)

                                TooltipBox(
                                    state = rememberTooltipState(),
                                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                        TooltipAnchorPosition.Below,
                                        AppDimens.tooltipAnchorOffset,
                                    ),
                                    tooltip = {
                                        PlainTooltip {
                                            Text(description)
                                        }
                                    },
                                ) {
                                    IconButton(
                                        onClick = {
                                            view.playSoundEffect(SoundEffectConstants.CLICK)
                                            isFullscreen = !isFullscreen
                                        },
                                    ) {
                                        Icon(icon, description)
                                    }
                                }
                            }
                            TooltipBox(
                                state = rememberTooltipState(),
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                    TooltipAnchorPosition.Below,
                                    AppDimens.tooltipAnchorOffset,
                                ),
                                tooltip = {
                                    PlainTooltip {
                                        Text(stringResource(R.string.more_options))
                                    }
                                },
                            ) {
                                IconButton(
                                    onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        showMenu = !showMenu
                                    },
                                ) {
                                    Icon(Icons.Default.MoreVert, stringResource(R.string.more_options))
                                }
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
                                onKeyEvent = { event: KeyEvent -> handleRemoteControlEvent(event) },
                                host = host,
                                connectionStatus = uiState.connectionStatus,
                                volume = uiState.volume,
                                muted = uiState.muted,
                                pressedKeys = physicallyPressedKeys,
                                onVolumeSet = { percent ->
                                    hostViewModel.setVolume(percent)
                                },
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
                                                    commandTemplate.command?.let { command ->
                                                        onMouseMove(event.dx, event.dy, command)
                                                    }
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
                                                val command = commandTemplate.formatCommand(text)
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

@Suppress("DEPRECATION")
private fun Activity.setShowWhenLockedEnabled(enabled: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(enabled)
    } else {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
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
        renderOutputAsMarkdown: Boolean,
        isRetry: Boolean,
        reuseShell: Boolean,
    ): Result {
        return Result.Success("")
    }

    override fun setVolume(percent: Int) {}
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
        muted = true,
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

private data class PhysicalKeyPressState(
    val remoteKey: RemoteControlKey,
    val usesPressReleaseCommands: Boolean,
    var longPressTriggered: Boolean = false,
    var longPressJob: Job? = null,
    var repeatJob: Job? = null,
)

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
