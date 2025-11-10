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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.Theme
import com.stefansundin.sshremote.data.settings.SettingsRepository
import com.stefansundin.sshremote.data.settings.SettingsViewModel
import com.stefansundin.sshremote.ui.components.ThemeSettingDialog
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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

class MockSettingsViewModel(theme: Theme, settingsRepository: SettingsRepository) :
    SettingsViewModel(settingsRepository) {
    private val _theme = MutableStateFlow(theme)
    override val theme = _theme.asStateFlow()
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    val mockViewModel =
        MockSettingsViewModel(Theme.SYSTEM, SettingsRepository(LocalContext.current))
    SSHRemoteTheme {
        SettingsScreen(
            settingsViewModel = mockViewModel,
            onNavigateUp = {},
            onNavigateToSshKeys = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenDarkPreview() {
    val mockViewModel = MockSettingsViewModel(Theme.DARK, SettingsRepository(LocalContext.current))
    SSHRemoteTheme(darkTheme = true) {
        SettingsScreen(
            settingsViewModel = mockViewModel,
            onNavigateUp = {},
            onNavigateToSshKeys = {},
        )
    }
}
