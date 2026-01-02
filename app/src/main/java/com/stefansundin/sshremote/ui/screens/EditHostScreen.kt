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
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Base64
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.stefansundin.sshremote.Validations
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.HostViewModel
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.data.host.RemoteControlScreen
import com.stefansundin.sshremote.data.identity.Identity
import com.stefansundin.sshremote.data.settings.ExportedCommand
import com.stefansundin.sshremote.data.settings.ExportedHost
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    hostViewModel: HostViewModel?, // null allowed for preview
    scanQrCodeOnStart: Boolean = false,
) {
    var name by rememberSaveable(host) { mutableStateOf(host?.name ?: "") }
    var hostname by rememberSaveable(host) { mutableStateOf(host?.hostname ?: "") }
    var port by rememberSaveable(host) { mutableStateOf(host?.port?.toString() ?: "22") }
    var user by rememberSaveable(host) { mutableStateOf(host?.user ?: "") }
    var sshConfig by rememberSaveable(host) { mutableStateOf(host?.sshConfig) }

    // Only used for QR code import:
    var commands by rememberSaveable(host) { mutableStateOf(host?.commands) }
    var remoteCommands by rememberSaveable(host) { mutableStateOf(host?.remoteCommands) }
    var startScreen by rememberSaveable(host) { mutableStateOf(host?.startScreen ?: RemoteControlScreen.Default) }

    var passwordState by rememberSaveable { mutableStateOf(PasswordState.SET) }
    LaunchedEffect(host?.passwordId) {
        val currentPasswordId = host?.passwordId
        if (currentPasswordId != null && hostViewModel != null) {
            if (hostViewModel.isPasswordLost(currentPasswordId)) {
                passwordState = PasswordState.LOST
            }
        }
    }

    val isPasswordSet = host?.passwordId != null
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
            if (originalIds != null && originalIds.isNotEmpty() && validIds?.isEmpty() == true && identities != null) {
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
            (sshConfig != host?.sshConfig)

    var showSaveDialog by rememberSaveable { mutableStateOf(false) }

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
                sshConfig = sshConfig,
            )
                ?: Host(
                    name = name,
                    hostname = hostname,
                    port = port.toInt(),
                    user = user,
                    identityIds = selectedIdentityIds,
                    knownHosts = knownHosts,
                    sshConfig = sshConfig,
                    commands = commands ?: Host.DEFAULT_COMMANDS,
                    remoteCommands = remoteCommands,
                    startScreen = startScreen,
                )
            onSave(hostToSave, if (showPasswordField) password else null)
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            title = { Text("Unsaved changes") },
            text = { Text("Do you want to save the host before leaving?") },
            properties = DialogProperties(dismissOnClickOutside = false),
            onDismissRequest = { showSaveDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isFormValid) {
                            handleSave()
                        } else {
                            showSaveDialog = false
                        }
                    },
                ) {
                    if (isFormValid) {
                        Text("Save and leave")
                    } else {
                        Text("Stay")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onNavigateUp) {
                    Text("Discard and leave")
                }
            },
        )
    }

    var showSshConfigWarning by rememberSaveable { mutableStateOf(false) }
    var showSshConfigDialog by rememberSaveable { mutableStateOf(false) }

    if (showSshConfigWarning) {
        AlertDialog(
            title = { Text("Advanced Users Only") },
            text = { Text("Changing SSH configuration values is intended for advanced users. Incorrect settings may prevent connection.") },
            onDismissRequest = { showSshConfigWarning = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSshConfigWarning = false
                        showSshConfigDialog = true
                    },
                ) {
                    Text("I know what I am doing")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSshConfigWarning = false },
                ) {
                    Text("Go back")
                }
            },
        )
    }

    if (showSshConfigDialog) {
        val uriHandler = LocalUriHandler.current
        var currentConfig by rememberSaveable { mutableStateOf(sshConfig ?: Host.DEFAULT_SSH_CONFIG) }

        AlertDialog(
            title = { Text("SSH Configuration") },
            text = {
                Column {
                    Text("JSch configuration is mostly like OpenSSH, but not exactly. Please do your research (click the Help button below).")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = currentConfig,
                        onValueChange = { currentConfig = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
                    )
                }
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = false,
            ),
            onDismissRequest = { showSshConfigDialog = false },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = {
                            sshConfig = currentConfig
                            showSshConfigDialog = false
                        },
                    ) {
                        Text("Save")
                    }
                }
            },
            dismissButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = {
                            uriHandler.openUri("https://deepwiki.com/mwiede/jsch/7-configuration")
                        },
                    ) {
                        Text("Help")
                    }
                    TextButton(
                        onClick = {
                            sshConfig = null
                            showSshConfigDialog = false
                        },
                    ) {
                        Text("Reset to defaults")
                    }
                }
            },
            modifier = Modifier.padding(16.dp),
        )
    }

    var qrCodeString by remember { mutableStateOf<String?>(null) }

    qrCodeString?.let {
        ExportHostQrCodeDialog(
            qrCodeString = it,
            onDismissRequest = { qrCodeString = null },
            onError = { qrCodeString = null },
        )
    }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
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

    LaunchedEffect(Unit) {
        if (scanQrCodeOnStart) {
            scanLauncher.launch(ScanOptions())
        }
    }

    var showExportDialog by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (showExportDialog) {
        AlertDialog(
            title = { Text("Export Host") },
            text = { Text("Choose what to include in the QR code.") },
            onDismissRequest = { showExportDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
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
                    Text("Full configuration")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
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
                    Text("Connection details only")
                }
            },
        )
    }


    BackHandler(enabled = hasUnsavedChanges) {
        showSaveDialog = true
    }

    val title = if (host == null) "Add Host" else "Edit Host"

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (hasUnsavedChanges) {
                                showSaveDialog = true
                            } else {
                                onNavigateUp()
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancel",
                        )
                    }
                },
                actions = {
                    var menuExpanded by rememberSaveable { mutableStateOf(false) }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Advanced SSH Config") },
                            onClick = {
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
                                text = { Text("Export to QR code") },
                                onClick = {
                                    menuExpanded = false
                                    showExportDialog = true
                                },
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Scan QR code") },
                                onClick = {
                                    menuExpanded = false
                                    scanLauncher.launch(ScanOptions())
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
                    contentDescription = "Save",
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
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                isError = hasBeenSubmitted && !isNameValid,
                singleLine = false,
                minLines = 1,
                maxLines = 2,
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
                label = { Text("Hostname or IP") },
                modifier = Modifier.fillMaxWidth(),
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
                label = { Text("Port") },
                modifier = Modifier.fillMaxWidth(),
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
                    label = { Text("User") },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                        .fillMaxWidth(),
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
                    label = { Text("Password") },
                    supportingText = {
                        if (passwordState == PasswordState.LOST) {
                            Text("Passwords are not backed up, please re-enter")
                        } else if (isPasswordSet) {
                            Text("Enter a new password, or leave empty to clear it")
                        } else {
                            Text("Optional, will be prompted for if not provided")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    trailingIcon = {
                        val image =
                            if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconToggleButton(
                            checked = passwordVisible,
                            onCheckedChange = { passwordVisible = it },
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
                        text = if (passwordState == PasswordState.LOST) "Password lost" else "Password saved",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                    TextButton(
                        onClick = { userWantsToChangePassword = true },
                    ) {
                        Text(if (passwordState == PasswordState.LOST) "Re-enter" else "Change or Clear")
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
                        "Loading..."
                    } else if (selectedIdentityIds == null) {
                        "Use any key"
                    } else if (selectedIdentityIds!!.isEmpty()) {
                        "Do not use keys"
                    } else {
                        identities.filter { selectedIdentityIds!!.contains(it.id) }
                            .joinToString(", ") { it.name }
                    },
                    onValueChange = { },
                    label = { Text("SSH Key") },
                    trailingIcon = {
                        if (identities != null) {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = identityDropdownExpanded)
                        }
                    },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                        .fillMaxWidth(),
                )
                if (identities != null) {
                    ExposedDropdownMenu(
                        expanded = identityDropdownExpanded,
                        onDismissRequest = { identityDropdownExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Use any key") },
                            onClick = {
                                selectedIdentityIds = null
                                identityDropdownExpanded = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Do not use keys") },
                            onClick = {
                                selectedIdentityIds = listOf()
                                identityDropdownExpanded = false
                            },
                        )
                        identities.forEach { key ->
                            DropdownMenuItem(
                                text = { Text(key.name) },
                                onClick = {
                                    // Later this might support multiple key assignments
                                    selectedIdentityIds = listOf(key.id)
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
                        text = "Saved host keys: $keyCount",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                    Button(
                        enabled = keyCount > 0,
                        onClick = { knownHosts = emptyList() },
                    ) {
                        Text("Clear")
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ExportHostQrCodeDialog(
    qrCodeString: String,
    onDismissRequest: () -> Unit,
    onError: (Exception) -> Unit,
) {
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val configuration = LocalConfiguration.current
    val view = LocalView.current

    if (!view.isInEditMode) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            DisposableEffect(configuration.orientation) {
                val windowInsetsController = WindowCompat.getInsetsController(window, view)
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                if (isLandscape) {
                    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                    windowInsetsController.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
                onDispose {
                    if (isLandscape) {
                        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
                    }
                }
            }
        }
    }

    LaunchedEffect(qrCodeString, imageSize) {
        if (imageSize.width > 0) {
            withContext(Dispatchers.Default) {
                val generatedBitmap = try {
                    val size = imageSize.width
                    val hints = mapOf(
                        EncodeHintType.CHARACTER_SET to "UTF-8",
                        EncodeHintType.MARGIN to 1,
                    )
                    val writer = QRCodeWriter()
                    val bitMatrix = writer.encode(qrCodeString, BarcodeFormat.QR_CODE, size, size, hints)
                    val width = bitMatrix.width
                    val height = bitMatrix.height
                    val bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)
                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            bitmap[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                        }
                    }
                    bitmap
                } catch (e: WriterException) {
                    withContext(Dispatchers.Main) {
                        onError(e)
                    }
                    null
                }
                qrCodeBitmap = generatedBitmap
            }
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        Text(
                            "Scan the QR code to import host",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .aspectRatio(1f)
                            .onSizeChanged {
                                if (it.width > 0 && it != imageSize) {
                                    imageSize = it
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (qrCodeBitmap != null) {
                            Image(
                                bitmap = qrCodeBitmap!!.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = androidx.compose.ui.graphics.Color.White,
                            ) {
                                CircularProgressIndicator(strokeWidth = 16.dp, modifier = Modifier.padding(128.dp))
                            }
                        }
                    }

                    if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        TextButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.padding(top = 24.dp),
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Add Host Preview")
@Composable
fun AddHostScreenPreview() {
    SSHRemoteTheme {
        EditHostScreen(
            host = null,
            onSave = { _, _ -> },
            onNavigateUp = {},
            identities = emptyList(),
            allUsers = emptyList(),
            hostViewModel = null,
        )
    }
}

@Preview(showBackground = true, name = "Edit Host Preview")
@Composable
fun EditHostScreenPreview() {
    SSHRemoteTheme {
        val sampleHost = Host(1, "Raspberry Pi", "192.168.1.10", 22, "pi", "passwordId", emptyList())
        EditHostScreen(
            host = sampleHost,
            onSave = { _, _ -> },
            onNavigateUp = {},
            identities = emptyList(),
            allUsers = emptyList(),
            hostViewModel = null,
        )
    }
}

@Preview(showBackground = true, name = "Clone Host Preview")
@Composable
fun CloneHostScreenPreview() {
    SSHRemoteTheme {
        val sampleHost = Host(0, "Copy of Raspberry Pi", "192.168.1.10", 22, "pi", null, emptyList())
        EditHostScreen(
            host = sampleHost,
            onSave = { _, _ -> },
            onNavigateUp = {},
            identities = emptyList(),
            allUsers = emptyList(),
            hostViewModel = null,
        )
    }
}
