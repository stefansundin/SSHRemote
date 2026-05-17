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

import android.content.res.Configuration
import android.util.Base64
import android.util.Log
import android.view.SoundEffectConstants
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.google.gson.Gson
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.Validations
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.IEditHostViewModel
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.data.host.RemoteControlScreen
import com.stefansundin.sshremote.data.host.wtypePreset
import com.stefansundin.sshremote.data.identity.Identity
import com.stefansundin.sshremote.data.settings.ExportedCommand
import com.stefansundin.sshremote.data.settings.ExportedHost
import com.stefansundin.sshremote.data.settings.ISettingsViewModel
import com.stefansundin.sshremote.ui.components.QrCodeDialog
import com.stefansundin.sshremote.ui.dpadFocusable
import com.stefansundin.sshremote.ui.portraitImePadding
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.URLEncoder
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

private enum class PasswordState {
    SET,
    LOST,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHostScreen(
    host: Host?,
    identities: List<Identity>?,
    allUsers: List<String>,
    onSave: (Host, String?) -> Unit,
    onNavigateUp: () -> Unit,
    hostViewModel: IEditHostViewModel,
    settingsViewModel: ISettingsViewModel,
    scanQrCodeOnStart: Boolean = false,
) {
    var name by rememberSaveable(host) { mutableStateOf(host?.name ?: "") }
    var hostname by rememberSaveable(host) { mutableStateOf(host?.hostname ?: "") }
    var port by rememberSaveable(host) { mutableStateOf(host?.port?.toString() ?: "22") }
    var user by rememberSaveable(host) { mutableStateOf(host?.user ?: "") }
    var sshConfig by rememberSaveable(host) { mutableStateOf(host?.sshConfig) }

    val coroutineScope = rememberCoroutineScope()

    // Only used for QR code import:
    var scanQrCode by rememberSaveable { mutableStateOf(scanQrCodeOnStart) }
    var commands by rememberSaveable(host) { mutableStateOf(host?.commands) }
    var remoteCommands by rememberSaveable(host) { mutableStateOf(host?.remoteCommands) }
    var shareInBackground by rememberSaveable(host) { mutableStateOf(host?.shareInBackground ?: false) }
    var startScreen by rememberSaveable(host) { mutableStateOf(host?.startScreen ?: RemoteControlScreen.Default) }

    var passwordState by rememberSaveable { mutableStateOf(PasswordState.SET) }
    LaunchedEffect(host?.passwordId) {
        val currentPasswordId = host?.passwordId
        if (currentPasswordId != null && hostViewModel.isPasswordLost(currentPasswordId)) {
            passwordState = PasswordState.LOST
        }
    }

    val isPasswordSet = host?.passwordId != null
    val allowPasswordPrompting = settingsViewModel.allowPasswordPrompting.collectAsState().value
    var userWantsToChangePassword by rememberSaveable { mutableStateOf(false) }
    val showPasswordField = !isPasswordSet || userWantsToChangePassword

    var password by rememberSaveable { mutableStateOf("") }
    var selectedIdentityIds by rememberSaveable(host, identities) {
        val originalIds = host?.identityIds
        // If identities is null, we can't filter valid IDs yet, so keep original IDs
        val validIds = if (identities != null) {
            originalIds?.filter { id -> identities.any { it.id == id } }
        } else {
            originalIds
        }
        val resultIds =
            if (!originalIds.isNullOrEmpty() && validIds?.isEmpty() == true && identities != null) {
                // Stale reference case: all selected keys are gone. Switch to "Use any key".
                // Only do this if we are sure identities are loaded (not null)
                null
            } else {
                // Otherwise, use the valid ones (or the original null/empty list)
                validIds ?: originalIds
            }
        mutableStateOf(resultIds)
    }
    var identityDropdownExpanded by rememberSaveable { mutableStateOf(false) }
    var knownHosts by rememberSaveable(host) { mutableStateOf(host?.knownHosts ?: emptyList()) }

    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var hasBeenSubmitted by rememberSaveable { mutableStateOf(false) }
    val onSubmit = { hasBeenSubmitted = true }

    val isNameValid by remember(name) { derivedStateOf { Validations.validateName(name) } }
    val isHostValid by remember(hostname) { derivedStateOf { Validations.validateHost(hostname) } }
    val isUserValid by remember(user) { derivedStateOf { Validations.validateUser(user) } }
    val isPortValid by remember(port) { derivedStateOf { Validations.validatePort(port) } }

    val isFormValid by remember(isNameValid, isHostValid, isUserValid, isPortValid) {
        derivedStateOf { isNameValid && isHostValid && isUserValid && isPortValid }
    }

    val passwordChanged = if (isPasswordSet) {
        userWantsToChangePassword
    } else {
        password.isNotEmpty()
    }
    val hasUnsavedChanges = (name != (host?.name ?: "")) ||
            (hostname != (host?.hostname ?: "")) ||
            (port != (host?.port?.toString() ?: "22")) ||
            (user != (host?.user ?: "")) ||
            passwordChanged ||
            (selectedIdentityIds != host?.identityIds) ||
            (knownHosts != (host?.knownHosts ?: emptyList<String>())) ||
            (shareInBackground != (host?.shareInBackground ?: false)) ||
            (sshConfig != host?.sshConfig)

    var showSaveDialog by rememberSaveable { mutableStateOf(false) }
    val view = LocalView.current

    val nameRequester = remember { BringIntoViewRequester() }
    val hostnameRequester = remember { BringIntoViewRequester() }
    val portRequester = remember { BringIntoViewRequester() }
    val userRequester = remember { BringIntoViewRequester() }

    fun handleSave() {
        onSubmit()
        if (isFormValid) {
            val hostToSave = host?.copy(
                name = name,
                hostname = hostname,
                port = port.toInt(),
                user = user,
                identityIds = selectedIdentityIds,
                knownHosts = knownHosts,
                shareInBackground = shareInBackground,
                sshConfig = sshConfig,
            )
                ?: Host(
                    name = name,
                    hostname = hostname,
                    port = port.toInt(),
                    user = user,
                    identityIds = selectedIdentityIds,
                    knownHosts = knownHosts,
                    shareInBackground = shareInBackground,
                    sshConfig = sshConfig,
                    commands = commands ?: Host.DEFAULT_COMMANDS,
                    remoteCommands = remoteCommands,
                    startScreen = startScreen,
                )
            onSave(hostToSave, if (showPasswordField) password else null)
        } else {
            coroutineScope.launch {
                if (!isNameValid) nameRequester.bringIntoView()
                else if (!isHostValid) hostnameRequester.bringIntoView()
                else if (!isPortValid) portRequester.bringIntoView()
                else if (!isUserValid) userRequester.bringIntoView()
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.unsaved_changes_title)) },
            text = { Text(stringResource(R.string.unsaved_changes_text)) },
            properties = DialogProperties(dismissOnClickOutside = false),
            onDismissRequest = { showSaveDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        if (isFormValid) {
                            handleSave()
                        } else {
                            showSaveDialog = false
                        }
                    },
                ) {
                    if (isFormValid) {
                        Text(stringResource(R.string.save_and_leave))
                    } else {
                        Text(stringResource(R.string.stay))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onNavigateUp()
                    },
                ) {
                    Text(stringResource(R.string.discard_and_leave))
                }
            },
        )
    }

    var showSshConfigWarning by rememberSaveable { mutableStateOf(false) }
    var showSshConfigDialog by rememberSaveable { mutableStateOf(false) }

    if (showSshConfigWarning) {
        AlertDialog(
            title = { Text(stringResource(R.string.advanced_users_only)) },
            text = { Text(stringResource(R.string.advanced_users_warning)) },
            onDismissRequest = { showSshConfigWarning = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        showSshConfigWarning = false
                        showSshConfigDialog = true
                    },
                ) {
                    Text(stringResource(R.string.i_know_what_i_am_doing))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        showSshConfigWarning = false
                    },
                ) {
                    Text(stringResource(R.string.go_back))
                }
            },
        )
    }

    if (showSshConfigDialog) {
        val uriHandler = LocalUriHandler.current
        var currentConfig by rememberSaveable { mutableStateOf(sshConfig ?: Host.DEFAULT_SSH_CONFIG) }

        AlertDialog(
            title = { Text(stringResource(R.string.ssh_configuration_title)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(stringResource(R.string.jsch_config_description))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = currentConfig,
                        onValueChange = { currentConfig = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace),
                    )
                }
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = false,
                decorFitsSystemWindows = false,
            ),
            onDismissRequest = { showSshConfigDialog = false },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            sshConfig = currentConfig
                            showSshConfigDialog = false
                        },
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            },
            dismissButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            uriHandler.openUri("https://github.com/stefansundin/SSHRemote/discussions/2")
                        },
                    ) {
                        Text(stringResource(R.string.help))
                    }
                    TextButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            sshConfig = null
                            showSshConfigDialog = false
                        },
                    ) {
                        Text(stringResource(R.string.reset_to_default))
                    }
                }
            },
            modifier = Modifier
                .portraitImePadding()
                .padding(16.dp),
        )
    }

    var qrCodeString by remember { mutableStateOf<String?>(null) }

    qrCodeString?.let {
        QrCodeDialog(
            qrCodeString = it,
            title = stringResource(R.string.scan_qr_code_to_import),
            onDismissRequest = { qrCodeString = null },
            onError = { qrCodeString = null },
        )
    }

    val qrScanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val contents = result.contents
            try {
                if (contents.startsWith("ssh://")) {
                    val uri = contents.toUri()
                    uri.userInfo?.let { user = it }
                    uri.host?.let { hostname = it }
                    if (uri.port != -1) {
                        port = uri.port.toString()
                    }
                    uri.getQueryParameter("name")?.let {
                        name = it
                    }
                    val hostKeys = uri.getQueryParameters("hostKey[]")
                    if (hostKeys.isNotEmpty()) {
                        knownHosts = hostKeys
                    }
                } else {
                    val reader = if (contents.startsWith("{")) {
                        contents.byteInputStream().reader()
                    } else {
                        val compressed = Base64.decode(contents, Base64.DEFAULT)
                        val bis = ByteArrayInputStream(compressed)
                        val gis = GZIPInputStream(bis)
                        InputStreamReader(gis, "UTF-8")
                    }
                    val hostData = Gson().fromJson(reader, ExportedHost::class.java)
                    hostData.name?.let { name = it }
                    hostData.hostname?.let { hostname = it }
                    hostData.port?.let { port = it.toString() }
                    hostData.user?.let { user = it }
                    hostData.knownHosts?.let { knownHosts = it }
                    hostData.sshConfig?.let { sshConfig = it }
                    hostData.allowIdentities?.let { selectedIdentityIds = if (it) null else emptyList() }
                    hostData.shareInBackground?.let { shareInBackground = it }
                    hostData.commands?.let { it -> commands = it.map { it.toCommand() } }
                    @Suppress("UNCHECKED_CAST")
                    hostData.remoteCommands?.let { it ->
                        remoteCommands =
                            (it.filterKeys { it != null } as Map<RemoteControlKey, ExportedCommand>).mapValues { it.value.toCommand() }
                    }
                    hostData.startScreen?.let { startScreen = it }
                }
            } catch (e: Exception) {
                Log.e("SSHRemote", "Error parsing QR code", e)
            }
        }
    }

    val scanQrCodePrompt = stringResource(R.string.scan_qr_code_prompt)
    LaunchedEffect(scanQrCode) {
        if (scanQrCode) {
            scanQrCode = false
            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            options.setPrompt(scanQrCodePrompt)
            options.setBeepEnabled(false)
            options.setOrientationLocked(false)
            qrScanLauncher.launch(options)
        }
    }

    var showExportDialog by rememberSaveable { mutableStateOf(false) }

    if (showExportDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.export_host_title)) },
            text = { Text(stringResource(R.string.choose_what_to_include)) },
            onDismissRequest = { showExportDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        showExportDialog = false
                        coroutineScope.launch {
                            val data = host!!.toExportedHost().exportToString()
                            val bos = ByteArrayOutputStream(data.length)
                            val gzip = GZIPOutputStream(bos)
                            gzip.write(data.toByteArray())
                            gzip.close()
                            val compressed = bos.toByteArray()
                            qrCodeString = Base64.encodeToString(compressed, Base64.DEFAULT)
                        }
                    },
                ) {
                    Text(stringResource(R.string.full_configuration))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        showExportDialog = false
                        val encodedName = URLEncoder.encode(name, "UTF-8")
                        var url = "ssh://$user@$hostname:$port?name=$encodedName"
                        if (knownHosts.isNotEmpty()) {
                            val key = URLEncoder.encode("hostKey[]", "UTF-8")
                            val hostKeysQuery =
                                knownHosts.joinToString("&") {
                                    val value = URLEncoder.encode(it, "UTF-8")
                                    "$key=$value"
                                }
                            url += "&$hostKeysQuery"
                        }
                        qrCodeString = url
                    },
                ) {
                    Text(stringResource(R.string.connection_details_only))
                }
            },
        )
    }


    BackHandler(enabled = hasUnsavedChanges) {
        showSaveDialog = true
    }

    val title = stringResource(if (host == null) R.string.add_host_title else R.string.edit_host_title)

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            if (hasUnsavedChanges) {
                                showSaveDialog = true
                            } else {
                                onNavigateUp()
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cancel),
                        )
                    }
                },
                actions = {
                    var menuExpanded by rememberSaveable { mutableStateOf(false) }
                    IconButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            menuExpanded = true
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more_options),
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.advanced_ssh_config)) },
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                menuExpanded = false
                                if (sshConfig == null) {
                                    showSshConfigWarning = true
                                } else {
                                    showSshConfigDialog = true
                                }
                            },
                        )
                        if (host != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.export_to_qr_code)) },
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    menuExpanded = false
                                    showExportDialog = true
                                },
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.scan_qr_code)) },
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    menuExpanded = false
                                    scanQrCode = true
                                },
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    handleSave()
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = stringResource(R.string.save),
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // NAME FIELD
            OutlinedTextField(
                value = name,
                onValueChange = { newName ->
                    // Allow one newline, limit to 100 chars
                    val newlineCount = newName.count { it == '\n' }
                    if (newlineCount <= 1) {
                        name = newName.take(100)
                    }
                },
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier
                    .bringIntoViewRequester(nameRequester)
                    .fillMaxWidth()
                    .dpadFocusable(),
                isError = hasBeenSubmitted && !isNameValid,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                ),
            )

            // HOSTNAME FIELD
            OutlinedTextField(
                value = hostname,
                onValueChange = { newHostname ->
                    // Disallow spaces and newlines
                    hostname = newHostname.replace(" ", "").replace("\n", "").take(255)
                },
                label = { Text(stringResource(R.string.hostname_or_ip)) },
                modifier = Modifier
                    .bringIntoViewRequester(hostnameRequester)
                    .fillMaxWidth()
                    .dpadFocusable(),
                isError = hasBeenSubmitted && !isHostValid,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                ),
            )

            // PORT FIELD
            OutlinedTextField(
                value = port,
                onValueChange = { newPort ->
                    // Allow only digits
                    if (newPort.all { it.isDigit() }) {
                        port = newPort.take(5)
                    }
                },
                label = { Text(stringResource(R.string.port)) },
                modifier = Modifier
                    .bringIntoViewRequester(portRequester)
                    .fillMaxWidth()
                    .dpadFocusable(),
                isError = hasBeenSubmitted && !isPortValid,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
            )

            // USER FIELD
            var userDropdownExpanded by rememberSaveable { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = userDropdownExpanded,
                onExpandedChange = { userDropdownExpanded = !userDropdownExpanded },
            ) {
                OutlinedTextField(
                    value = user,
                    onValueChange = { newUser ->
                        // Disallow spaces and newlines
                        user = newUser.replace(" ", "").replace("\n", "").take(32)
                    },
                    label = { Text(stringResource(R.string.user)) },
                    modifier = Modifier
                        .bringIntoViewRequester(userRequester)
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                        .fillMaxWidth()
                        .dpadFocusable(),
                    isError = hasBeenSubmitted && !isUserValid,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                    ),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = userDropdownExpanded)
                    },
                )

                val suggestedUsers = allUsers.distinct().sorted()
                if (suggestedUsers.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = userDropdownExpanded,
                        onDismissRequest = { userDropdownExpanded = false },
                    ) {
                        suggestedUsers.forEach { suggestedUser ->
                            DropdownMenuItem(
                                text = { Text(suggestedUser) },
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    user = suggestedUser
                                    userDropdownExpanded = false
                                },
                            )
                        }
                    }
                }
            }

            // PASSWORD FIELD
            if (showPasswordField) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password)) },
                    supportingText = {
                        Text(
                            stringResource(
                                if (passwordState == PasswordState.LOST) {
                                    R.string.passwords_not_backed_up
                                } else if (isPasswordSet) {
                                    R.string.enter_new_password_or_empty
                                } else if (allowPasswordPrompting) {
                                    R.string.optional_password_prompt
                                } else {
                                    R.string.optional
                                },
                            ),
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .dpadFocusable(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    trailingIcon = {
                        val image =
                            if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description =
                            stringResource(if (passwordVisible) R.string.hide_password else R.string.show_password)
                        IconToggleButton(
                            checked = passwordVisible,
                            onCheckedChange = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                passwordVisible = it
                            },
                        ) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(if (passwordState == PasswordState.LOST) R.string.password_lost else R.string.password_saved),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                    TextButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            userWantsToChangePassword = true
                        },
                    ) {
                        Text(
                            stringResource(if (passwordState == PasswordState.LOST) R.string.re_enter else R.string.change_or_clear),
                        )
                    }
                }
            }

            // SSH KEY SELECTION DROPDOWN
            ExposedDropdownMenuBox(
                expanded = identityDropdownExpanded,
                onExpandedChange = { if (identities != null) identityDropdownExpanded = !identityDropdownExpanded },
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = if (identities == null) {
                        stringResource(R.string.loading)
                    } else if (selectedIdentityIds == null) {
                        stringResource(R.string.use_any_key)
                    } else if (selectedIdentityIds!!.isEmpty()) {
                        stringResource(R.string.do_not_use_keys)
                    } else {
                        identities.filter { selectedIdentityIds!!.contains(it.id) }
                            .joinToString(", ") { it.name }
                    },
                    onValueChange = { },
                    label = { Text(stringResource(R.string.ssh_key)) },
                    trailingIcon = {
                        if (identities != null) {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = identityDropdownExpanded)
                        }
                    },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                        .fillMaxWidth()
                        .dpadFocusable(),
                )
                if (identities != null) {
                    ExposedDropdownMenu(
                        expanded = identityDropdownExpanded,
                        onDismissRequest = { identityDropdownExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.use_any_key)) },
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                selectedIdentityIds = null
                                identityDropdownExpanded = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.do_not_use_keys)) },
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                selectedIdentityIds = listOf()
                                identityDropdownExpanded = false
                            },
                        )
                        identities.forEach { identity ->
                            DropdownMenuItem(
                                text = { Text(identity.name) },
                                onClick = {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    // Later this might support multiple key assignments
                                    selectedIdentityIds = listOf(identity.id)
                                    identityDropdownExpanded = false
                                },
                            )
                        }
                    }
                }
            }

            // KNOWN HOSTS MANAGEMENT
            if (host != null || knownHosts.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    val keyCount = knownHosts.size
                    Text(
                        text = stringResource(R.string.saved_host_keys, keyCount),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                    Button(
                        enabled = keyCount > 0,
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            knownHosts = emptyList()
                        },
                    ) {
                        Text(stringResource(R.string.clear))
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}


val sampleHost = Host(
    "1",
    "Raspberry Pi",
    "192.168.1.10",
    22,
    "pi",
    "passwordId",
    emptyList(),
    listOf(""),
    remoteCommands = wtypePreset,
)

private val fakeEditHostViewModel = object : IEditHostViewModel {
    override suspend fun isPasswordLost(passwordId: String): Boolean = passwordId == "lost"
}

@Preview(showBackground = true, name = "Add Host Preview")
@Preview(
    showBackground = true,
    name = "Add Host Preview (dark and large font)",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun EditHostScreenPreview_Add() {
    SSHRemoteTheme {
        EditHostScreen(
            host = null,
            onSave = { _, _ -> },
            onNavigateUp = {},
            identities = emptyList(),
            allUsers = emptyList(),
            hostViewModel = fakeEditHostViewModel,
            settingsViewModel = fakeSettingsViewModel,
        )
    }
}

@Preview(showBackground = true, name = "Edit Host Preview")
@Preview(
    showBackground = true,
    name = "Edit Host Preview (dark and large font)",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun EditHostScreenPreview_Edit() {
    SSHRemoteTheme {
        EditHostScreen(
            host = sampleHost,
            onSave = { _, _ -> },
            onNavigateUp = {},
            identities = emptyList(),
            allUsers = emptyList(),
            hostViewModel = fakeEditHostViewModel,
            settingsViewModel = fakeSettingsViewModel,
        )
    }
}

// Note: This preview has to be tested in "Interactive Mode".
@Preview(showBackground = true, name = "Password Lost")
@Preview(
    showBackground = true,
    name = "Password Lost (dark and large font)",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun EditHostScreenPreview_PasswordLost() {
    SSHRemoteTheme {
        EditHostScreen(
            host = sampleHost.copy(passwordId = "lost"),
            onSave = { _, _ -> },
            onNavigateUp = {},
            identities = emptyList(),
            allUsers = emptyList(),
            hostViewModel = fakeEditHostViewModel,
            settingsViewModel = fakeSettingsViewModel,
        )
    }
}
