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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.data.sshserver.Command

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCommandsScreen(
    commands: List<Command>,
    onSave: (List<Command>) -> Unit,
    onNavigateBack: () -> Unit,
) {
    var editingCommands by remember { mutableStateOf(commands) }
    var showDialog by remember { mutableStateOf(false) }
    var editingCommand by remember { mutableStateOf<Command?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Commands") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add command")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(editingCommands) { command ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "${command.name}: ${command.command}", modifier = Modifier.weight(1f))
                    IconButton(onClick = { editingCommand = command; showDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit command")
                    }
                    IconButton(onClick = { editingCommands = editingCommands.filter { it != command } }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete command")
                    }
                }
            }
            item {
                Button(onClick = { onSave(editingCommands) }) {
                    Text("Save")
                }
            }
        }
    }

    if (showDialog) {
        CommandDialog(
            command = editingCommand,
            onDismiss = { showDialog = false; editingCommand = null },
            onSave = { command ->
                editingCommands = if (editingCommand != null) {
                    editingCommands.map { if (it == editingCommand) command else it }
                } else {
                    editingCommands + command
                }
                showDialog = false
                editingCommand = null
            }
        )
    }
}

@Composable
fun CommandDialog(
    command: Command?,
    onDismiss: () -> Unit,
    onSave: (Command) -> Unit
) {
    var name by remember { mutableStateOf(command?.name ?: "") }
    var commandText by remember { mutableStateOf(command?.command ?: "") }
    var showOutput by remember { mutableStateOf(command?.showOutput ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (command == null) "Add Command" else "Edit Command") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                TextField(value = commandText, onValueChange = { commandText = it }, label = { Text("Command") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showOutput, onCheckedChange = { showOutput = it })
                    Text("Show output")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(Command(name, commandText, showOutput)) },
                enabled = name.isNotBlank() && commandText.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
