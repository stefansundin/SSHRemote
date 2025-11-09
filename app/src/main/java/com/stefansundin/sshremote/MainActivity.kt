/*
SSH Remote
Copyright (C) 2025  Stefan Sundin

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.stefansundin.sshremote

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import com.stefansundin.sshremote.data.SshKey
import com.stefansundin.sshremote.data.SshKeyViewModel
import com.stefansundin.sshremote.data.SshKeyViewModelFactory
import com.stefansundin.sshremote.ui.screens.AddSshKeyScreen
import com.stefansundin.sshremote.ui.screens.PublicKeyDialog
import com.stefansundin.sshremote.ui.screens.SshKeysScreen
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import java.io.ByteArrayOutputStream

enum class Screen {
    LIST,
    EDIT,
    TERMINAL,
    SETTINGS,
    SSH_KEYS,
    ADD_SSH_KEY,
}

enum class Theme {
    SYSTEM,
    LIGHT,
    DARK
}

class MainActivity : ComponentActivity() {
    private val cryptoManager = CryptoManager()
    private val sshRepository = SshRepository()

    private val sshServerViewModel: SshServerViewModel by viewModels {
        val app = (application as SshRemoteApplication)
        SshServerViewModelFactory(
            app.sshServerRepository,
            app.sshKeyRepository,
            sshRepository,
            cryptoManager,
        )
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        val app = (application as SshRemoteApplication)
        SettingsViewModelFactory(app.settingsRepository)
    }

    private val sshKeyViewModel: SshKeyViewModel by viewModels {
        val app = (application as SshRemoteApplication)
        SshKeyViewModelFactory(
            app.sshKeyRepository,
            cryptoManager,
        )
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
                var selectedServer by remember { mutableStateOf<SshServer?>(null) }
                var currentScreen by remember { mutableStateOf(Screen.LIST) }

                val navigateBackToList = {
                    selectedServer = null
                    currentScreen = Screen.LIST
                }
                val connectToServer = { server: SshServer ->
                    selectedServer = server
                    currentScreen = Screen.TERMINAL
                    sshServerViewModel.connectToServer(server)
                }
                val editServer = { server: SshServer? ->
                    selectedServer = server
                    currentScreen = Screen.EDIT
                }
                val showSettings = {
                    currentScreen = Screen.SETTINGS
                }
                val showSshKeys = {
                    currentScreen = Screen.SSH_KEYS
                }
                val addSshKey = {
                    currentScreen = Screen.ADD_SSH_KEY
                }

                var showPublicKeyDialog by remember { mutableStateOf(false) }
                var publicKeyToShow by remember { mutableStateOf("") }

                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    sshKeyViewModel.newPublicKeyFlow.collect { publicKey ->
                        publicKeyToShow = publicKey
                        showPublicKeyDialog = true
                    }
                }

                if (showPublicKeyDialog) {
                    PublicKeyDialog(
                        publicKey = publicKeyToShow,
                        onDismiss = { showPublicKeyDialog = false },
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    when (currentScreen) {
                        Screen.LIST -> {
                            val servers by sshServerViewModel.allServers.collectAsState()
                            SshServerScreen(
                                servers = servers,
                                onConnectClicked = { server: SshServer -> connectToServer(server) },
                                onAddServerClicked = { editServer(null) },
                                onEditServerClicked = { server -> editServer(server) },
                                onDeleteServerClicked = { server -> sshServerViewModel.delete(server) },
                                onSettingsClicked = { showSettings() },
                            )
                        }
                        Screen.EDIT -> {
                            val sshKeys by sshKeyViewModel.sshKeys.collectAsState()
                            AddEditSshServerScreen(
                                server = selectedServer,
                                sshKeys = sshKeys,
                                onServerSaved = { server ->
                                    sshServerViewModel.upsert(server)
                                    navigateBackToList()
                                },
                                onNavigateUp = { navigateBackToList() },
                                cryptoManager = cryptoManager,
                            )
                        }
                        Screen.TERMINAL -> {
                            val uiState by sshServerViewModel.uiState.collectAsState()
                            SshTerminalScreen(
                                uiState = uiState,
                                onRunUptime = { sshServerViewModel.runUptimeCommand() },
                                onDisconnect = {
                                    sshServerViewModel.disconnect()
                                    navigateBackToList()
                                },
                                onClearCommandOutput = { sshServerViewModel.clearCommandOutput() },
                            )
                        }
                        Screen.SETTINGS -> {
                            SettingsScreen(
                                settingsViewModel = settingsViewModel,
                                onNavigateToSshKeys = { showSshKeys() },
                                onNavigateUp = { navigateBackToList() },
                            )
                        }
                        Screen.SSH_KEYS -> {
                            val fileSaverLauncher =
                                rememberLauncherForActivityResult(
                                    contract = ActivityResultContracts.CreateDocument("attachment/plain"),
                                    onResult = { uri ->
                                        if (uri != null) {
                                            val keyToExport = sshKeyViewModel.keyToExport.value
                                            if (keyToExport != null) {
                                                val privateKey =
                                                    cryptoManager.decrypt(keyToExport.encryptedPrivateKey)
                                                val keypair = KeyPair.load(JSch(), privateKey, null)
                                                val outputStream = ByteArrayOutputStream()
                                                val comment =
                                                    keypair.publicKeyComment.ifEmpty { keyToExport.name }
                                                keypair.writePublicKey(outputStream, comment)
                                                val publicKey =
                                                    outputStream.toString(Charsets.UTF_8.name())
                                                keypair.dispose()

                                                context.contentResolver.openOutputStream(uri)
                                                    ?.use { output ->
                                                        output.write(publicKey.toByteArray())
                                                    }
                                            }
                                        }
                                    },
                                )

                            SshKeysScreen(
                                sshKeyViewModel = sshKeyViewModel,
                                cryptoManager = cryptoManager,
                                onNavigateToAddSshKey = { addSshKey() },
                                onNavigateUp = { showSettings() },
                                onShowPublicKey = { key ->
                                    val privateKey = cryptoManager.decrypt(key.encryptedPrivateKey)
                                    val keypair = KeyPair.load(JSch(), privateKey, null)
                                    val outputStream = ByteArrayOutputStream()
                                    keypair.writePublicKey(
                                        outputStream,
                                        keypair.publicKeyComment.ifEmpty { key.name },
                                    )
                                    val publicKey = outputStream.toString(Charsets.UTF_8.name())
                                    keypair.dispose()
                                    publicKeyToShow = publicKey
                                    showPublicKeyDialog = true
                                },
                                onExportPublicKey = { key ->
                                    sshKeyViewModel.keyToExport.value = key

                                    val privateKey = cryptoManager.decrypt(key.encryptedPrivateKey)
                                    val keypair = KeyPair.load(JSch(), privateKey, null)
                                    val keyTypeName = if (keypair.keyTypeString == "ssh-ed25519") { "ed25519" } else "rsa"
                                    keypair.dispose()

                                    val suggestedFilename = "id_$keyTypeName.pub"
                                    fileSaverLauncher.launch(suggestedFilename)
                                },
                                onDeleteKey = { key -> sshKeyViewModel.delete(key) },
                            )
                        }
                        Screen.ADD_SSH_KEY -> {
                            AddSshKeyScreen(
                                onKeySaved = { name, privateKey ->
                                    sshKeyViewModel.insert(name, privateKey)
                                    currentScreen = Screen.SSH_KEYS
                                },
                                onKeyGenerated = { name, type, comment ->
                                    Log.d(
                                        "AddSshKeyScreen",
                                        "Key generated: $name, $type, $comment",
                                    )
                                    sshKeyViewModel.generateAndInsert(name, type, comment)
                                    currentScreen = Screen.SSH_KEYS
                                },
                                onNavigateUp = { currentScreen = Screen.SSH_KEYS },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SshServerScreen(
    servers: List<SshServer>,
    onConnectClicked: (SshServer) -> Unit,
    onAddServerClicked: () -> Unit,
    onEditServerClicked: (SshServer) -> Unit,
    onDeleteServerClicked: (SshServer) -> Unit,
    onSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hosts") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = onSettingsClicked) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddServerClicked) {
                Icon(Icons.Filled.Add, contentDescription = "Add SSH Host")
            }
        },
    ) { innerPadding ->
        if (servers.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No SSH hosts added.\n\nPress the + button to add a new host.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            ) {
                items(items = servers, key = { server -> server.id }) { server ->
                    SshServerItem(
                        server = server,
                        onConnect = { onConnectClicked(server) },
                        onEdit = { onEditServerClicked(server) },
                        onDelete = { onDeleteServerClicked(server) },
                    )
                }
            }
        }
    }
}

@Composable
fun SshServerItem(
    server: SshServer,
    onConnect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isContextMenuVisible by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .clickable(onClick = onConnect),
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = server.name,
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(2.dp))

                val portString = if (server.port != 22) ":${server.port}" else ""
                Text(
                    text = "${server.user}@${server.host}${portString}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Box {
                IconButton(onClick = { isContextMenuVisible = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                    )
                }

                DropdownMenu(
                    expanded = isContextMenuVisible,
                    onDismissRequest = { isContextMenuVisible = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            onEdit()
                            isContextMenuVisible = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            onDelete()
                            isContextMenuVisible = false
                        },
                    )
                }
            }
        }
    }
}

fun validateName(name: String): Boolean {
    // Name must not be blank.
    return name.isNotBlank()
}

@Suppress("DEPRECATION")
fun validateHost(host: String): Boolean {
    if (host.isBlank()) return false

    // Check if it's a valid IP Address using Android's built-in pattern.
    val isIpAddress = Patterns.IP_ADDRESS.matcher(host).matches()
    if (isIpAddress) {
        return true
    }

    // For hostnames, we'll be more flexible than a strict regex.
    // First, disallow invalid characters like spaces.
    if (host.contains(" ")) {
        return false
    }

    // Allow single-label hostnames (like 'localhost' or 'raspberrypi').
    // We check if it contains no dots but is otherwise valid.
    if (!host.contains(".")) {
        // A simple check for valid characters in a single-label hostname.
        return host.all { it.isLetterOrDigit() || it == '-' }
    }

    // For multi-label hostnames (like 'example.com'), use Android's domain pattern.
    // We add a modification to allow a trailing dot, which is technically valid.
    return Patterns.DOMAIN_NAME.matcher(host).matches() ||
            (host.endsWith('.') && Patterns.DOMAIN_NAME.matcher(host.dropLast(1)).matches())
}

fun validateUser(user: String): Boolean {
    if (user.isBlank()) return false

    // Allow letters, numbers, underscore, and hyphen.
    val userRegex = """^[a-zA-Z0-9_-]+$""".toRegex()
    return userRegex.matches(user)
}

fun validatePort(port: String): Boolean {
    val portNumber = port.toIntOrNull()

    // Port must be a number between 1 and 65535.
    return portNumber != null && portNumber in 1..65535
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSshServerScreen(
    server: SshServer?,
    sshKeys: List<SshKey>,
    onServerSaved: (SshServer) -> Unit,
    onNavigateUp: () -> Unit,
    cryptoManager: CryptoManager?, // null allowed for preview
) {
    var name by remember { mutableStateOf(server?.name ?: "") }
    var host by remember { mutableStateOf(server?.host ?: "") }
    var port by remember { mutableStateOf(server?.port?.toString() ?: "22") }
    var user by remember { mutableStateOf(server?.user ?: "") }
    var password by remember {
        val decryptedPassword = if (cryptoManager != null && server?.encryptedPassword != null) {
            decryptString(server.encryptedPassword, cryptoManager)
        } else {
            ""
        }
        mutableStateOf(decryptedPassword)
    }
    var selectedSshKeyIds by remember { mutableStateOf(server?.sshKeyIds) }
    var sshKeyDropdownExpanded by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }
    var hasBeenSubmitted by remember { mutableStateOf(false) }
    val onSubmit = { hasBeenSubmitted = true }

    val isNameValid by remember(name) { derivedStateOf { validateName(name) } }
    val isHostValid by remember(host) { derivedStateOf { validateHost(host) } }
    val isUserValid by remember(user) { derivedStateOf { validateUser(user) } }
    val isPortValid by remember(port) { derivedStateOf { validatePort(port) } }

    val isFormValid by remember(isNameValid, isHostValid, isUserValid, isPortValid) {
        derivedStateOf { isNameValid && isHostValid && isUserValid && isPortValid }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (server == null) "Add Host" else "Edit Host") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancel",
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            onSubmit()
                            if (isFormValid) {
                                val encryptedPassword =
                                    if (cryptoManager != null && password.isNotEmpty()) {
                                        encryptString(password, cryptoManager)
                                    } else null

                                val serverToSave = SshServer(
                                    id = server?.id ?: 0,
                                    name = name,
                                    host = host,
                                    port = port.toInt(),
                                    user = user,
                                    encryptedPassword = encryptedPassword,
                                    sshKeyIds = selectedSshKeyIds,
                                )
                                onServerSaved(serverToSave)
                            }
                        },
                    ) {
                        Text("Save")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
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

            // HOST FIELD
            OutlinedTextField(
                value = host,
                onValueChange = { newHost ->
                    // Disallow spaces and newlines
                    host = newHost.replace(" ", "").replace("\n", "").take(255)
                },
                label = { Text("Host") },
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
            OutlinedTextField(
                value = user,
                onValueChange = { newUser ->
                    // Disallow spaces and newlines
                    user = newUser.replace(" ", "").replace("\n", "").take(32)
                },
                label = { Text("User") },
                modifier = Modifier.fillMaxWidth(),
                isError = hasBeenSubmitted && !isUserValid,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                ),
            )

            // PASSWORD FIELD
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
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

            // SSH KEY SELECTION DROPDOWN
            ExposedDropdownMenuBox(
                expanded = sshKeyDropdownExpanded,
                onExpandedChange = { sshKeyDropdownExpanded = !sshKeyDropdownExpanded },
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = if (selectedSshKeyIds == null) { "Use any key" }
                        else if (selectedSshKeyIds!!.isEmpty()) { "Do not use keys" }
                        else sshKeys.filter { selectedSshKeyIds!!.contains(it.id) }.joinToString(", ") { it.name },
                    onValueChange = { },
                    label = { Text("SSH Key") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = sshKeyDropdownExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = sshKeyDropdownExpanded,
                    onDismissRequest = { sshKeyDropdownExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Use any key") },
                        onClick = {
                            selectedSshKeyIds = null
                            sshKeyDropdownExpanded = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Do not use keys") },
                        onClick = {
                            selectedSshKeyIds = listOf()
                            sshKeyDropdownExpanded = false
                        },
                    )
                    sshKeys.forEach { key ->
                        DropdownMenuItem(
                            text = { Text(key.name) },
                            onClick = {
                                // Later this might support multiple key assignments
                                selectedSshKeyIds = listOf(key.id)
                                sshKeyDropdownExpanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SshServerScreenPreview() {
    SSHRemoteTheme {
        val sampleServers = listOf(
            SshServer(1, "Raspberry Pi", "192.168.1.10", 22, "pi", null),
            SshServer(2, "Example Server", "example.com", 2222, "admin", null),
        )
        SshServerScreen(
            servers = sampleServers,
            onConnectClicked = {},
            onAddServerClicked = {},
            onEditServerClicked = {},
            onDeleteServerClicked = {},
            onSettingsClicked = {},
        )
    }
}

@Preview(showBackground = true, name = "Add Host Preview")
@Composable
fun AddSshServerScreenPreview() {
    SSHRemoteTheme {
        AddEditSshServerScreen(server = null, onServerSaved = {}, onNavigateUp = {}, sshKeys = emptyList(), cryptoManager = null)
    }
}

@Preview(showBackground = true, name = "Edit Host Preview")
@Composable
fun EditSshServerScreenPreview() {
    SSHRemoteTheme {
        val sampleServer = SshServer(1, "Raspberry Pi", "192.168.1.10", 22, "pi", null)
        AddEditSshServerScreen(server = sampleServer, onServerSaved = {}, onNavigateUp = {}, sshKeys = emptyList(), cryptoManager = null)
    }
}

@Composable
fun SshTerminalScreen(
    uiState: SshTerminalUiState,
    onRunUptime: () -> Unit,
    onDisconnect: () -> Unit,
    onClearCommandOutput: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = uiState.connectionStatus,
            style = MaterialTheme.typography.titleMedium,
        )

        Button(
            onClick = onRunUptime,
            enabled = uiState.connectionStatus.startsWith("Connected") && !uiState.isLoading,
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text("Run 'uptime'")
            }
        }

        Button(onClick = onDisconnect) {
            Text("Disconnect")
        }
    }

    if (uiState.commandOutput != null) {
        AlertDialog(
            onDismissRequest = { onClearCommandOutput() },
            title = { Text("Command Output") },
            text = { Text(uiState.commandOutput) },
            confirmButton = {
                Button(onClick = { onClearCommandOutput() }) {
                    Text("OK")
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToSshKeys: () -> Unit,
) {
    val savedTheme by settingsViewModel.theme.collectAsState()
    var previewTheme by remember { mutableStateOf(savedTheme) }
    var showThemeDialog by remember { mutableStateOf(false) }
    val useDarkTheme = when (previewTheme) {
        Theme.SYSTEM -> isSystemInDarkTheme()
        Theme.LIGHT -> false
        Theme.DARK -> true
    }

    SSHRemoteTheme(darkTheme = useDarkTheme) {
        if (showThemeDialog) {
            ThemeSettingDialog(
                currentTheme = previewTheme,
                onThemeSelected = { newTheme ->
                    previewTheme = newTheme
                },
                onConfirm = {
                    settingsViewModel.setTheme(previewTheme)
                    showThemeDialog = false
                },
                onDismiss = {
                    previewTheme = savedTheme
                    showThemeDialog = false
                },
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (previewTheme != savedTheme) {
                                    settingsViewModel.setTheme(savedTheme)
                                }
                                onNavigateUp()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp),
            ) {
                Text("Appearance", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .clickable {
                            previewTheme = savedTheme
                            showThemeDialog = true
                        }
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                ) {
                    Text(
                        "Theme",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        savedTheme.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Security", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .clickable { onNavigateToSshKeys() }
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                ) {
                    Text(
                        "SSH Keys",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        "Manage SSH keys for authentication",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeSettingDialog(
    currentTheme: Theme,
    onThemeSelected: (Theme) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose theme") },
        text = {
            Column {
                Theme.entries.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (theme == currentTheme),
                                onClick = { onThemeSelected(theme) }, // Instantly report theme selection for preview
                                role = Role.RadioButton,
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = (theme == currentTheme),
                            onClick = null,
                        )
                        Text(
                            text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
