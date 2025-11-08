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
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import android.util.Base64
import android.util.Log
import androidx.compose.material3.IconToggleButton
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

class MainActivity : ComponentActivity() {
    private val cryptoManager = CryptoManager()

    private val sshServerViewModel: SshServerViewModel by viewModels {
        SshServerViewModelFactory((application as SshRemoteApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SSHRemoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var serverToEdit by remember { mutableStateOf<SshServer?>(null) }
                    var showAddEditScreen by remember { mutableStateOf(false) }
                    val navigateBackToList = {
                        serverToEdit = null
                        showAddEditScreen = false
                    }
                    val editServer = { server: SshServer? ->
                        serverToEdit = server
                        showAddEditScreen = true
                    }

                    if (showAddEditScreen) {
                        AddEditSshServerScreen(
                            server = serverToEdit,
                            onServerSaved = { server ->
                                sshServerViewModel.upsert(server)
                                navigateBackToList()
                            },
                            onNavigateUp = {
                                navigateBackToList()
                            },
                            cryptoManager,
                        )
                    } else {
                        val servers by sshServerViewModel.allServers.collectAsState()
                        SshServerScreen(
                            servers = servers,
                            onAddServerClicked = {
                                editServer(null)
                            },
                            onEditServerClicked = { server ->
                                editServer(server)
                            },
                            onDeleteServerClicked = { server ->
                                sshServerViewModel.delete(server)
                            }
                        )
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
    onAddServerClicked: () -> Unit,
    onEditServerClicked: (SshServer) -> Unit,
    onDeleteServerClicked: (SshServer) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hosts") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddServerClicked) {
                Icon(Icons.Filled.Add, contentDescription = "Add SSH Server")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(items = servers, key = { server -> server.id }) { server ->
                SshServerItem(
                    server = server,
                    onEdit = { onEditServerClicked(server) },
                    onDelete = { onDeleteServerClicked(server) }
                )
            }
        }
    }
}

@Composable
fun SshServerItem(
    server: SshServer,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isContextMenuVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .clickable {
                Toast.makeText(context, "TODO: Connect to ${server.name}", Toast.LENGTH_SHORT)
                    .show()
            }
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min).padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = server.name,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(2.dp))

                val portString = if (server.port != 22) ":${server.port}" else ""
                Text(
                    text = "${server.user}@${server.host}${portString}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(onClick = { isContextMenuVisible = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }

                DropdownMenu(
                    expanded = isContextMenuVisible,
                    onDismissRequest = { isContextMenuVisible = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            onEdit()
                            isContextMenuVisible = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            onDelete()
                            isContextMenuVisible = false
                        }
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
    onServerSaved: (SshServer) -> Unit,
    onNavigateUp: () -> Unit,
    cryptoManager: CryptoManager?, // null allowed for preview
) {
    var name by remember { mutableStateOf(server?.name ?: "") }
    var host by remember { mutableStateOf(server?.host ?: "") }
    var port by remember { mutableStateOf(server?.port?.toString() ?: "22") }
    var user by remember { mutableStateOf(server?.user ?: "") }
    var password by remember {
        val decryptedPassword = server?.encryptedPassword?.let {
            if (cryptoManager != null && it.contains(":")) {
                try {
                    val (iv, data) = it.split(":").map { str -> Base64.decode(str, Base64.DEFAULT) }
                    cryptoManager.decrypt(EncryptedPayload(iv, data))
                } catch (e: Exception) {
                    Log.e("AddEditSshServerScreen", "Error decrypting password", e)
                    ""
                }
            } else ""
        } ?: ""
        mutableStateOf(decryptedPassword)
    }

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
                    TextButton(onClick = onNavigateUp) { Text("Cancel") }
                },
                actions = {
                    Button(
                        onClick = {
                            onSubmit()
                            if (isFormValid) {
                                val encryptedPassword = if (cryptoManager != null && password.isNotEmpty()) {
                                    val encryptedPayload = cryptoManager.encrypt(password)
                                    Base64.encodeToString(encryptedPayload.iv, Base64.DEFAULT) +
                                            ":" + Base64.encodeToString(
                                        encryptedPayload.encryptedBytes,
                                        Base64.DEFAULT
                                    )
                                } else ""

                                val serverToSave = SshServer(
                                    id = server?.id ?: 0,
                                    name = name,
                                    host = host,
                                    port = port.toInt(),
                                    user = user,
                                    encryptedPassword = encryptedPassword,
                                )
                                onServerSaved(serverToSave)
                            }
                        },
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                // Allow multiple lines for the name field
                singleLine = false,
                minLines = 1,
                maxLines = 2
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
                singleLine = true
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = hasBeenSubmitted && !isPortValid,
                singleLine = true
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
                singleLine = true
            )

            // PASSWORD FIELD
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"
                    IconToggleButton(checked = passwordVisible, onCheckedChange = { passwordVisible = it }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SshServerScreenPreview() {
    SSHRemoteTheme {
        val sampleServers = listOf(
            SshServer(1, "Raspberry Pi", "192.168.1.10", 22, "pi", null),
            SshServer(2, "Example Server", "example.com", 2222, "admin", null)
        )
        SshServerScreen(
            servers = sampleServers,
            onAddServerClicked = {},
            onEditServerClicked = {},
            onDeleteServerClicked = {}
        )
    }
}

@Preview(showBackground = true, name = "Add Host Preview")
@Composable
fun AddSshServerScreenPreview() {
    SSHRemoteTheme {
        AddEditSshServerScreen(server = null, onServerSaved = {}, onNavigateUp = {}, cryptoManager = null)
    }
}

@Preview(showBackground = true, name = "Edit Host Preview")
@Composable
fun EditSshServerScreenPreview() {
    SSHRemoteTheme {
        val sampleServer = SshServer(1, "Raspberry Pi", "192.168.1.10", 22, "pi", null)
        AddEditSshServerScreen(server = sampleServer, onServerSaved = {}, onNavigateUp = {}, cryptoManager = null)
    }
}
