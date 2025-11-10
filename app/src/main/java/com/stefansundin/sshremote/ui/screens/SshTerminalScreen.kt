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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.data.sshserver.SshTerminalUiState

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
