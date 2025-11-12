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

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.Theme
import com.stefansundin.sshremote.data.settings.SettingsEvent
import com.stefansundin.sshremote.data.settings.SettingsRepository
import com.stefansundin.sshremote.data.settings.SettingsViewModel
import com.stefansundin.sshremote.data.sshserver.SshServer
import com.stefansundin.sshremote.data.sshserver.SshServerDao
import com.stefansundin.sshremote.data.sshserver.SshServerRepository
import com.stefansundin.sshremote.ui.components.ThemeSettingDialog
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToSshKeys: () -> Unit,
) {
    val context = LocalContext.current
    val savedTheme by settingsViewModel.theme.collectAsState()
    var previewTheme by remember { mutableStateOf(savedTheme) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var importUri by remember { mutableStateOf<Uri?>(null) }
    val useDarkTheme = when (previewTheme) {
        Theme.SYSTEM -> isSystemInDarkTheme()
        Theme.LIGHT -> false
        Theme.DARK -> true
    }
    val strictHostKeyChecking by settingsViewModel.strictHostKeyChecking.collectAsState()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            uri?.let {
                settingsViewModel.exportSettings(context, uri)
            }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { importUri = it }
        }
    )

    LaunchedEffect(Unit) {
        settingsViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is SettingsEvent.ImportSuccess -> {
                    val message = "Successfully imported ${event.count} servers."
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                is SettingsEvent.ImportError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    importUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { importUri = null },
            title = { Text("Import settings") },
            text = { Text("Do you want to merge with existing servers or overwrite them? Overwriting will delete all current servers before importing.") },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            settingsViewModel.importSettings(context, uri, false)
                            importUri = null
                        }
                    ) {
                        Text("Overwrite")
                    }
                    TextButton(
                        onClick = {
                            settingsViewModel.importSettings(context, uri, true)
                            importUri = null
                        }
                    ) {
                        Text("Merge")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { importUri = null }
                ) {
                    Text("Cancel")
                }
            },
        )
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Strict host key checking",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Switch(
                        checked = strictHostKeyChecking,
                        onCheckedChange = {
                            settingsViewModel.setStrictHostKeyChecking(it)
                        },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Data", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .clickable {
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                            val filename = "ssh-remote-settings-${dateFormat.format(Date())}.json"
                            exportLauncher.launch(filename)
                        }
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                ) {
                    Text(
                        "Export settings",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        "Export settings to a file.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "SSH keys and passwords are not exported.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Column(
                    modifier = Modifier
                        .clickable { importLauncher.launch(arrayOf("application/json")) }
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                ) {
                    Text(
                        "Import settings",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        "Import settings from a file.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
