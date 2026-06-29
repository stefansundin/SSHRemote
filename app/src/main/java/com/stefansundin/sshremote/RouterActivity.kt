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

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.SoundEffectConstants
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.lifecycleScope
import com.stefansundin.sshremote.data.CryptoManager
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.ConnectionStatus
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.persistAcceptedHostKey
import com.stefansundin.sshremote.data.host.toConnectionDetails
import com.stefansundin.sshremote.ui.components.CommandOutputDialog
import com.stefansundin.sshremote.ui.components.SelectHostDialog
import com.stefansundin.sshremote.ui.dpadFocusable
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RouterActivity : ComponentActivity() {
    private val app by lazy { application as SshRemoteApplication }
    private val cryptoManager = CryptoManager()
    private var uiState by mutableStateOf<RouterUiState>(RouterUiState.Idle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyNoAnimationTransition(isOpen = true)
        setContent {
            val appearance by app.settingsRepository.appearance.collectAsState(initial = null)

            val message by app.sshRepository.message.collectAsState()
            val hostKeyVerification by app.sshRepository.hostKeyVerification.collectAsState()
            val passwordPrompt by app.sshRepository.passwordPrompt.collectAsState()
            val passphrasePrompt by app.sshRepository.passphrasePrompt.collectAsState()

            val resolvedAppearance = appearance ?: run {
                return@setContent
            }

            SSHRemoteTheme(appearance = resolvedAppearance) {
                RouterContent(
                    uiState = uiState,
                    message = message,
                    hostKeyVerification = hostKeyVerification,
                    passwordPrompt = passwordPrompt,
                    passphrasePrompt = passphrasePrompt,
                    onMessageDismissed = { app.sshRepository.onMessageDismissed() },
                    onHostKeyAccepted = { app.sshRepository.onHostKeyVerificationComplete(true) },
                    onHostKeyRejected = { app.sshRepository.onHostKeyVerificationComplete(false) },
                    onPasswordSubmitted = { app.sshRepository.onPasswordPromptComplete(it) },
                    onPasswordCanceled = { app.sshRepository.onPasswordPromptComplete(null) },
                    onPassphraseSubmitted = { app.sshRepository.onPassphrasePromptComplete(it) },
                    onPassphraseCanceled = { app.sshRepository.onPassphrasePromptComplete(null) },
                    onDismissOutput = { finishImmediately() },
                )
            }
        }
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        when {
            intent.action == Intent.ACTION_SEND && intent.type == "text/plain" -> {
                handleShareIntent(intent)
            }

            intent.hasExtra(EXTRA_HOST_ID) && intent.hasExtra(EXTRA_COMMAND_ID) -> {
                handleCommandShortcutIntent(intent)
            }

            else -> {
                forwardToMainActivity(intent)
            }
        }
    }

    private fun handleShareIntent(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText.isNullOrEmpty()) {
            finishImmediately()
            return
        }

        lifecycleScope.launch {
            val host =
                if (intent.hasExtra(EXTRA_HOST_ID)) {
                    intent.getStringExtra(EXTRA_HOST_ID)?.let { hostId ->
                        app.hostRepository.getOnce(hostId)
                    }
                } else {
                    resolveShareHost()
                } ?: run {
                    val hosts = app.hostRepository.getAll().first()
                    val selectedHostDeferred = CompletableDeferred<Host?>()
                    uiState = RouterUiState.SelectHostDialog(
                        hosts = hosts,
                        onHostSelected = { selectedHostDeferred.complete(it) },
                        onDismiss = { selectedHostDeferred.complete(null) },
                    )
                    selectedHostDeferred.await()
                }
            if (host == null) {
                finishImmediately()
                return@launch
            }

            if (!intent.hasExtra(EXTRA_HOST_ID)) {
                intent.putExtra(EXTRA_HOST_ID, host.id)
            }

            val commandTemplate = host.resolveShareCommandTemplate()
            if (commandTemplate?.command.isNullOrEmpty()) {
                uiState = RouterUiState.MissingShareCommand
                return@launch
            }
            if (host.shareInBackground) {
                executeShortcutCommand(
                    host = host,
                    commandTemplate = commandTemplate,
                    command = commandTemplate.formatCommand(sharedText),
                    loadingTitle = getString(R.string.sharing_to, host.name),
                )
            } else {
                forwardToMainActivity(intent)
            }
        }
    }

    private fun handleCommandShortcutIntent(intent: Intent) {
        val hostId = intent.getStringExtra(EXTRA_HOST_ID)
        val commandId = intent.getStringExtra(EXTRA_COMMAND_ID)
        if (hostId.isNullOrEmpty() || commandId.isNullOrEmpty()) {
            forwardToMainActivity(intent)
            return
        }
        val runInBackground = intent.getBooleanExtra(EXTRA_RUN_IN_BACKGROUND, false)

        if (app.activeConnectionTracker.state.value.isEditingRemoteControl) {
            Toast.makeText(
                this@RouterActivity,
                getString(R.string.shortcut_save_changes_before_using),
                Toast.LENGTH_LONG,
            ).show()
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                },
            )
            finishImmediately()
            return
        }

        lifecycleScope.launch {
            val host = app.hostRepository.getOnce(hostId)
            if (host == null) {
                Toast.makeText(this@RouterActivity, R.string.shortcut_host_not_found, Toast.LENGTH_SHORT).show()
                finishImmediately()
                return@launch
            }

            val commandTemplate = host.commands.find { it.id == commandId }
            if (commandTemplate == null) {
                Toast.makeText(this@RouterActivity, R.string.shortcut_command_not_found, Toast.LENGTH_SHORT).show()
                finishImmediately()
                return@launch
            }

            if (!runInBackground) {
                forwardToMainActivity(intent)
                return@launch
            }
            executeShortcutCommand(
                host = host,
                commandTemplate = commandTemplate,
                command = commandTemplate.command,
                loadingTitle = getString(R.string.executing_on, host.name),
            )
        }
    }

    private suspend fun resolveShareHost(): Host? {
        val connectionState = app.activeConnectionTracker.state.value
        return if (connectionState.connectionStatus == ConnectionStatus.CONNECTED && connectionState.hostId != null) {
            app.hostRepository.getOnce(connectionState.hostId)
        } else {
            app.hostRepository.getAll().first().singleOrNull()
        }
    }

    private suspend fun executeShortcutCommand(
        host: Host,
        commandTemplate: Command,
        command: String,
        loadingTitle: String,
    ) {
        val connectionState = app.activeConnectionTracker.state.value
        val isAlreadyConnectedToHost =
            connectionState.connectionStatus == ConnectionStatus.CONNECTED && connectionState.hostId == host.id
        val connectedInRouter = if (isAlreadyConnectedToHost) {
            false
        } else {
            connectInRouter(
                host = host,
                loadingTitle = loadingTitle,
                showUi = commandTemplate.showOutput,
            )
        }

        if (!isAlreadyConnectedToHost && !connectedInRouter) {
            if (!commandTemplate.showOutput) {
                finishImmediately()
            }
            return
        }

        if (commandTemplate.showOutput) {
            try {
                runCommandInRouter(
                    commandTemplate = commandTemplate,
                    command = command,
                    loadingTitle = loadingTitle,
                )
            } finally {
                if (connectedInRouter) {
                    disconnectRouterSession(host.id)
                }
            }
        } else {
            runCommandInBackground(command) {
                if (connectedInRouter) {
                    disconnectRouterSession(host.id)
                }
            }
            finishImmediately()
        }
    }

    private suspend fun connectInRouter(host: Host, loadingTitle: String, showUi: Boolean): Boolean {
        if (showUi) {
            uiState = RouterUiState.Loading(title = loadingTitle, statusRes = R.string.connecting)
        }
        app.activeConnectionTracker.update(host.id, ConnectionStatus.CONNECTING)

        return try {
            val hostKeyUsed = app.sshRepository.connect(
                host.toConnectionDetails(
                    identityRepository = app.identityRepository,
                    knownHostRepository = app.knownHostRepository,
                    passwordDao = app.passwordDao,
                    cryptoManager = cryptoManager,
                ),
            )
            persistAcceptedHostKey(app.hostRepository, host, hostKeyUsed)
            app.activeConnectionTracker.update(host.id, ConnectionStatus.CONNECTED)
            true
        } catch (e: Exception) {
            if (e is CancellationException) {
                app.activeConnectionTracker.update(host.id, ConnectionStatus.DISCONNECTED)
                throw e
            }
            app.activeConnectionTracker.update(host.id, ConnectionStatus.DISCONNECTED)
            if (showUi) {
                uiState = RouterUiState.Output(output = e.message ?: "Connection failed", renderMarkdown = false)
            } else {
                Toast.makeText(applicationContext, e.message ?: "Connection failed", Toast.LENGTH_LONG).show()
            }
            false
        }
    }

    private suspend fun disconnectRouterSession(hostId: String) {
        app.sshRepository.disconnect()
        app.activeConnectionTracker.update(hostId, ConnectionStatus.DISCONNECTED)
    }


    private suspend fun runCommandInRouter(
        commandTemplate: Command,
        command: String,
        loadingTitle: String,
    ) {
        uiState = RouterUiState.Loading(title = loadingTitle, statusRes = R.string.executing_command)
        uiState =
            when (val result = app.sshRepository.executeCommand(command)) {
                is Result.Success -> {
                    RouterUiState.Output(
                        output = result.output,
                        renderMarkdown = commandTemplate.renderOutputAsMarkdown,
                    )
                }

                is Result.Error -> {
                    RouterUiState.Output(
                        output = result.message,
                        renderMarkdown = false,
                    )
                }
            }
    }

    private fun runCommandInBackground(command: String, onComplete: (suspend () -> Unit)? = null) {
        val applicationContext = applicationContext
        app.applicationScope.launch {
            Toast.makeText(this@RouterActivity, R.string.executing_command, Toast.LENGTH_SHORT).show()

            when (val result = app.sshRepository.executeCommand(command)) {
                is Result.Error -> {
                    Toast.makeText(applicationContext, result.message, Toast.LENGTH_LONG).show()
                }

                is Result.Success -> Unit
            }
            onComplete?.invoke()
        }
    }

    private fun forwardToMainActivity(intent: Intent?) {
        if (intent == null) {
            finishImmediately()
            return
        }
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                action = intent.action
                type = intent.type
                putExtras(intent)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            },
        )
        finishImmediately()
    }

    private fun finishImmediately() {
        finish()
        applyNoAnimationTransition(isOpen = false)
    }

    private fun applyNoAnimationTransition(isOpen: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                if (isOpen) OVERRIDE_TRANSITION_OPEN else OVERRIDE_TRANSITION_CLOSE,
                0,
                0,
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }
}

private sealed interface RouterUiState {
    data object Idle : RouterUiState
    data object MissingShareCommand : RouterUiState
    data class Loading(
        val title: String,
        @StringRes val statusRes: Int,
    ) : RouterUiState

    data class Output(
        val output: String,
        val renderMarkdown: Boolean,
    ) : RouterUiState

    data class SelectHostDialog(
        val hosts: List<Host>,
        val onHostSelected: (Host?) -> Unit,
        val onDismiss: () -> Unit,
    ) : RouterUiState
}

@Composable
private fun RouterContent(
    uiState: RouterUiState,
    message: Message?,
    hostKeyVerification: HostKeyVerification?,
    passwordPrompt: PasswordPrompt?,
    passphrasePrompt: PassphrasePrompt?,
    onMessageDismissed: () -> Unit,
    onHostKeyAccepted: () -> Unit,
    onHostKeyRejected: () -> Unit,
    onPasswordSubmitted: (String) -> Unit,
    onPasswordCanceled: () -> Unit,
    onPassphraseSubmitted: (String) -> Unit,
    onPassphraseCanceled: () -> Unit,
    onDismissOutput: () -> Unit,
) {
    message?.let { currentMessage ->
        RouterMessageDialog(
            message = currentMessage,
            onDismiss = onMessageDismissed,
        )
    }
    hostKeyVerification?.let { verification ->
        RouterHostKeyVerificationDialog(
            verification = verification,
            onAccept = onHostKeyAccepted,
            onReject = onHostKeyRejected,
        )
    }
    passwordPrompt?.let { prompt ->
        RouterPasswordPrompt(
            prompt = prompt,
            onConfirm = onPasswordSubmitted,
            onCancel = onPasswordCanceled,
        )
    }
    passphrasePrompt?.let { prompt ->
        RouterPassphrasePrompt(
            prompt = prompt,
            onConfirm = onPassphraseSubmitted,
            onCancel = onPassphraseCanceled,
        )
    }
    when (uiState) {
        RouterUiState.Idle -> Unit
        RouterUiState.MissingShareCommand -> ShareMissingShareCommandDialog(onDismiss = onDismissOutput)
        is RouterUiState.Loading -> RouterLoadingDialog(title = uiState.title, statusRes = uiState.statusRes)
        is RouterUiState.Output -> {
            CommandOutputDialog(
                output = uiState.output,
                renderMarkdown = uiState.renderMarkdown,
                onDismiss = onDismissOutput,
            )
        }

        is RouterUiState.SelectHostDialog -> {
            SelectHostDialog(
                hosts = uiState.hosts,
                onHostSelected = { host ->
                    uiState.onHostSelected(host)
                },
                onDismiss = {
                    uiState.onDismiss()
                },
            )
        }
    }
}

@Composable
private fun RouterMessageDialog(
    message: Message,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = { Text(stringResource(R.string.message)) },
        text = {
            SelectionContainer {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(message.message)
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        },
        properties = DialogProperties(dismissOnClickOutside = false, decorFitsSystemWindows = false),
    )
}

@Composable
private fun RouterHostKeyVerificationDialog(
    verification: HostKeyVerification,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    AlertDialog(
        title = { Text(stringResource(R.string.host_key_verification)) },
        text = {
            SelectionContainer {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(verification.message)
                }
            }
        },
        onDismissRequest = onReject,
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text(stringResource(R.string.accept))
            }
        },
        dismissButton = {
            TextButton(onClick = onReject) {
                Text(stringResource(R.string.reject))
            }
        },
        properties = DialogProperties(dismissOnClickOutside = false, decorFitsSystemWindows = false),
    )
}

@Composable
private fun RouterPasswordPrompt(
    prompt: PasswordPrompt,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
) {
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
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description =
                        stringResource(if (passwordVisible) R.string.hide_password else R.string.show_password)
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
            )
        },
        onDismissRequest = onCancel,
        confirmButton = {
            TextButton(onClick = { onConfirm(password) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        },
        properties = DialogProperties(dismissOnClickOutside = false, decorFitsSystemWindows = false),
    )
}

@Composable
private fun RouterPassphrasePrompt(
    prompt: PassphrasePrompt,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
) {
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
                trailingIcon = {
                    val image = if (passphraseVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description =
                        stringResource(if (passphraseVisible) R.string.hide_passphrase else R.string.show_passphrase)
                    IconButton(onClick = { passphraseVisible = !passphraseVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
            )
        },
        onDismissRequest = onCancel,
        confirmButton = {
            TextButton(onClick = { onConfirm(passphrase) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        },
        properties = DialogProperties(dismissOnClickOutside = false, decorFitsSystemWindows = false),
    )
}

@Composable
private fun RouterLoadingDialog(
    title: String,
    @StringRes statusRes: Int,
) {
    AlertDialog(
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
                Text(
                    text = stringResource(statusRes),
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
        },
        onDismissRequest = {},
        confirmButton = {},
        dismissButton = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            decorFitsSystemWindows = false,
        ),
    )
}

@Composable
fun ShareMissingShareCommandDialog(onDismiss: () -> Unit) {
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

@Preview(showBackground = true, widthDp = 400, heightDp = 600, name = "Router loading")
@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 600,
    name = "Router loading (dark and large font)",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun RouterContentPreview_Loading() {
    SSHRemoteTheme {
        Surface {
            RouterContent(
                uiState = RouterUiState.Loading(
                    title = "Uptime",
                    statusRes = R.string.connecting,
                ),
                message = null,
                hostKeyVerification = null,
                passwordPrompt = null,
                passphrasePrompt = null,
                onMessageDismissed = {},
                onHostKeyAccepted = {},
                onHostKeyRejected = {},
                onPasswordSubmitted = {},
                onPasswordCanceled = {},
                onPassphraseSubmitted = {},
                onPassphraseCanceled = {},
                onDismissOutput = {},
            )
        }
    }
}

@Suppress("SpellCheckingInspection", "GrazieInspectionRunner")
@Preview(showBackground = true, widthDp = 400, heightDp = 600, name = "Router output")
@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 600,
    name = "Router output (dark and large font)",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun RouterContentPreview_Output() {
    SSHRemoteTheme {
        Surface {
            RouterContent(
                uiState = RouterUiState.Output(
                    output = "Linux pi 6.12.34+rpt-rpi-2712 #1 SMP PREEMPT Debian 1:6.12.34-1+rpt1~bookworm (2025-06-26) aarch64 GNU/Linux\n",
                    renderMarkdown = false,
                ),
                message = null,
                hostKeyVerification = null,
                passwordPrompt = null,
                passphrasePrompt = null,
                onMessageDismissed = {},
                onHostKeyAccepted = {},
                onHostKeyRejected = {},
                onPasswordSubmitted = {},
                onPasswordCanceled = {},
                onPassphraseSubmitted = {},
                onPassphraseCanceled = {},
                onDismissOutput = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600, name = "Connection message")
@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 600,
    name = "Connection message (dark and large font)",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun RouterContentPreview_Message() {
    SSHRemoteTheme {
        Surface {
            RouterContent(
                uiState = RouterUiState.Idle,
                message = Message(
                    message = "Connected to host.example.com.\n\nAuthorized access only.",
                    response = CompletableDeferred(),
                ),
                hostKeyVerification = null,
                passwordPrompt = null,
                passphrasePrompt = null,
                onMessageDismissed = {},
                onHostKeyAccepted = {},
                onHostKeyRejected = {},
                onPasswordSubmitted = {},
                onPasswordCanceled = {},
                onPassphraseSubmitted = {},
                onPassphraseCanceled = {},
                onDismissOutput = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600, name = "Host key verification")
@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 600,
    name = "Host key verification (dark and large font)",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun RouterContentPreview_HostKeyVerification() {
    SSHRemoteTheme {
        Surface {
            RouterContent(
                uiState = RouterUiState.Idle,
                message = null,
                hostKeyVerification = HostKeyVerification(
                    message = "The authenticity of host '192.168.1.2' can't be established.\nED25519 key fingerprint is SHA256:+DiY3wvvV6TuJJhbpZisF/zLDA0zPMSvHdkr4UvCOqU.\nAre you sure you want to continue connecting?",
                    response = CompletableDeferred(),
                ),
                passwordPrompt = null,
                passphrasePrompt = null,
                onMessageDismissed = {},
                onHostKeyAccepted = {},
                onHostKeyRejected = {},
                onPasswordSubmitted = {},
                onPasswordCanceled = {},
                onPassphraseSubmitted = {},
                onPassphraseCanceled = {},
                onDismissOutput = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600, name = "Password prompt")
@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 600,
    name = "Password prompt (dark and large font)",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun RouterContentPreview_PasswordPrompt() {
    SSHRemoteTheme {
        Surface {
            RouterContent(
                uiState = RouterUiState.Idle,
                message = null,
                hostKeyVerification = null,
                passwordPrompt = PasswordPrompt(
                    message = "Enter password",
                    response = CompletableDeferred(),
                ),
                passphrasePrompt = null,
                onMessageDismissed = {},
                onHostKeyAccepted = {},
                onHostKeyRejected = {},
                onPasswordSubmitted = {},
                onPasswordCanceled = {},
                onPassphraseSubmitted = {},
                onPassphraseCanceled = {},
                onDismissOutput = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600, name = "Passphrase prompt")
@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 600,
    name = "Passphrase prompt (dark and large font)",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun RouterContentPreview_PassphrasePrompt() {
    SSHRemoteTheme {
        Surface {
            RouterContent(
                uiState = RouterUiState.Idle,
                message = null,
                hostKeyVerification = null,
                passwordPrompt = null,
                passphrasePrompt = PassphrasePrompt(
                    message = "Enter passphrase for private key",
                    response = CompletableDeferred(),
                ),
                onMessageDismissed = {},
                onHostKeyAccepted = {},
                onHostKeyRejected = {},
                onPasswordSubmitted = {},
                onPasswordCanceled = {},
                onPassphraseSubmitted = {},
                onPassphraseCanceled = {},
                onDismissOutput = {},
            )
        }
    }
}
