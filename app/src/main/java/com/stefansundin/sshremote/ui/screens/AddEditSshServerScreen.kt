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

package com.stefansundin.sshremote.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.data.CryptoManager
import com.stefansundin.sshremote.data.sshserver.SshServer
import com.stefansundin.sshremote.Validations
import com.stefansundin.sshremote.data.sshkey.SshKey
import com.stefansundin.sshremote.data.decryptString
import com.stefansundin.sshremote.data.encryptString
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlin.collections.forEach

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

    val isNameValid by remember(name) { derivedStateOf { Validations.validateName(name) } }
    val isHostValid by remember(host) { derivedStateOf { Validations.validateHost(host) } }
    val isUserValid by remember(user) { derivedStateOf { Validations.validateUser(user) } }
    val isPortValid by remember(port) { derivedStateOf { Validations.validatePort(port) } }

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
                    value = if (selectedSshKeyIds == null) {
                        "Use any key"
                    } else if (selectedSshKeyIds!!.isEmpty()) {
                        "Do not use keys"
                    } else sshKeys.filter { selectedSshKeyIds!!.contains(it.id) }
                        .joinToString(", ") { it.name },
                    onValueChange = { },
                    label = { Text("SSH Key") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = sshKeyDropdownExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
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

@Preview(showBackground = true, name = "Add Host Preview")
@Composable
fun AddSshServerScreenPreview() {
    SSHRemoteTheme {
        AddEditSshServerScreen(
            server = null,
            onServerSaved = {},
            onNavigateUp = {},
            sshKeys = emptyList(),
            cryptoManager = null,
        )
    }
}

@Preview(showBackground = true, name = "Edit Host Preview")
@Composable
fun EditSshServerScreenPreview() {
    SSHRemoteTheme {
        val sampleServer = SshServer(1, "Raspberry Pi", "192.168.1.10", 22, "pi", null, emptyList())
        AddEditSshServerScreen(
            server = sampleServer,
            onServerSaved = {},
            onNavigateUp = {},
            sshKeys = emptyList(),
            cryptoManager = null,
        )
    }
}
