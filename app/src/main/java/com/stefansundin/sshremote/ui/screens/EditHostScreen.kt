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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.Validations
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.HostViewModel
import com.stefansundin.sshremote.data.identity.Identity
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme

private enum class PasswordState {
    LOADING,
    LOST,
    SET
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHostScreen(
    host: Host?,
    identities: List<Identity>,
    allUsers: List<String>,
    onSave: (Host, String?) -> Unit,
    onNavigateUp: () -> Unit,
    hostViewModel: HostViewModel?, // null allowed for preview
) {
    var name by rememberSaveable(host) { mutableStateOf(host?.name ?: "") }
    var hostname by rememberSaveable(host) { mutableStateOf(host?.hostname ?: "") }
    var port by rememberSaveable(host) { mutableStateOf(host?.port?.toString() ?: "22") }
    var user by rememberSaveable(host) { mutableStateOf(host?.user ?: "") }

    var passwordState by rememberSaveable { mutableStateOf(PasswordState.LOADING) }
    LaunchedEffect(host?.passwordId) {
        val currentPasswordId = host?.passwordId
        passwordState = if (currentPasswordId != null && hostViewModel != null) {
            if (hostViewModel.isPasswordLost(currentPasswordId)) {
                PasswordState.LOST
            } else {
                PasswordState.SET
            }
        } else {
            PasswordState.SET
        }
    }

    val isPasswordSet = host?.passwordId != null
    var userWantsToChangePassword by rememberSaveable { mutableStateOf(false) }
    val showPasswordField = !isPasswordSet || userWantsToChangePassword

    var password by rememberSaveable { mutableStateOf("") }
    var selectedIdentityIds by rememberSaveable(host, identities) {
        val originalIds = host?.identityIds
        val validIds = originalIds?.filter { id -> identities.any { it.id == id } }
        val resultIds = if (originalIds != null && originalIds.isNotEmpty() && validIds?.isEmpty() == true) {
            // Stale reference case: all selected keys are gone. Switch to "Use any key".
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
            (knownHosts != (host?.knownHosts ?: emptyList<String>()))

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
            )
                ?: Host(
                    name = name,
                    hostname = hostname,
                    port = port.toInt(),
                    user = user,
                    identityIds = selectedIdentityIds,
                    knownHosts = knownHosts,
                )
            onSave(hostToSave, if (showPasswordField) password else null)
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Unsaved changes") },
            text = { Text("Do you want to save the host before leaving?") },
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
            if (passwordState != PasswordState.LOADING) {
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
            }

            // SSH KEY SELECTION DROPDOWN
            ExposedDropdownMenuBox(
                expanded = identityDropdownExpanded,
                onExpandedChange = { identityDropdownExpanded = !identityDropdownExpanded },
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = if (selectedIdentityIds == null) {
                        "Use any key"
                    } else if (selectedIdentityIds!!.isEmpty()) {
                        "Do not use keys"
                    } else identities.filter { selectedIdentityIds!!.contains(it.id) }
                        .joinToString(", ") { it.name },
                    onValueChange = { },
                    label = { Text("SSH Key") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = identityDropdownExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                        .fillMaxWidth(),
                )
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

            // KNOWN HOSTS MANAGEMENT
            if (host != null) {
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
