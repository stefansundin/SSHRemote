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

import android.content.ClipData
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val keyTypes = mapOf(KeyPair.ED25519 to "ED25519", KeyPair.RSA to "RSA")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIdentityScreen(
    onKeySaved: (name: String, privateKey: String) -> Unit,
    onKeyGenerated: (name: String, type: Int, comment: String) -> Unit,
    onNavigateUp: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var privateKey by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Import File", "Generate", "Enter Manually")
    var isKeyContentValid by remember { mutableStateOf(false) }
    var selectedKeyType by remember { mutableStateOf<Int?>(null) }

    var showSaveDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = privateKey.isNotBlank()
    val isFormValid = (selectedTabIndex == 0 && isKeyContentValid) ||
            (selectedTabIndex == 1) ||
            (selectedTabIndex == 2 && isKeyContentValid)

    fun handleSave() {
        val finalName = name.ifBlank {
            "Key ${
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm",
                    Locale.getDefault(),
                ).format(Date())
            }"
        }
        when (selectedTabIndex) {
            0, 2 -> onKeySaved(finalName, privateKey)
            1 -> onKeyGenerated(
                finalName,
                selectedKeyType ?: KeyPair.ED25519,
                finalName,
            )
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Unsaved changes") },
            text = { Text("Do you want to save the key before leaving?") },
            confirmButton = {
                TextButton(onClick = { handleSave() }) {
                    Text("Save and leave")
                }
            },
            dismissButton = {
                TextButton(onClick = onNavigateUp) {
                    Text("Discard and leave")
                }
            },
        )
    }

    BackHandler(enabled = hasUnsavedChanges && isFormValid) {
        showSaveDialog = true
    }

    LaunchedEffect(privateKey, selectedTabIndex) {
        isKeyContentValid = if (selectedTabIndex == 0 || selectedTabIndex == 2) {
            if (privateKey.isBlank()) false
            else {
                try {
                    val jsch = JSch()
                    val keyPair = KeyPair.load(jsch, privateKey.toByteArray(), null)
                    if (keyTypes.containsKey(keyPair.keyType)) {
                        selectedKeyType = keyPair.keyType
                    }
                    keyPair.dispose()
                    true
                } catch (_: Exception) {
                    false
                }
            }
        } else {
            true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add SSH Key") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (hasUnsavedChanges && isFormValid) {
                                showSaveDialog = true
                            } else {
                                onNavigateUp()
                            }
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
        floatingActionButton = {
            if (isFormValid) {
                FloatingActionButton(
                    onClick = {
                        handleSave()
                    },
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = "Save SSH Key",
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Key Name (optional)") },
                placeholder = { Text("Defaults to current date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                singleLine = true,
            )

            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) },
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                when (selectedTabIndex) {
                    0 -> ImportFileTab(
                        keyType = selectedKeyType,
                        onKeyContentRead = { content -> privateKey = content },
                        onNameSuggestion = { suggestedName -> name = suggestedName },
                    )

                    1 -> GenerateKeyTab(
                        keyType = selectedKeyType,
                        onKeyTypeSelected = { selectedKeyType = it },
                    )

                    2 -> ManualEntryTab(
                        privateKey = privateKey,
                        onPrivateKeyChange = { privateKey = it },
                        onNameSuggestion = { suggestedName -> name = suggestedName },
                        isError = privateKey.isNotBlank() && !isKeyContentValid,
                    )
                }
            }
        }
    }
}

@Composable
fun ImportFileTab(
    keyType: Int?,
    onKeyContentRead: (String) -> Unit,
    onNameSuggestion: (String) -> Unit,
) {
    val context = LocalContext.current
    var keyContentForParsing by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(keyContentForParsing) {
        keyContentForParsing?.let { content ->
            withContext(Dispatchers.IO) {
                try {
                    val keypair = KeyPair.load(JSch(), content.toByteArray(), null)
                    val comment = keypair.publicKeyComment
                    if (comment.isNotBlank()) {
                        withContext(Dispatchers.Main) {
                            onNameSuggestion(comment)
                        }
                    }
                    keypair.dispose()
                    error = null
                } catch (e: Exception) {
                    error = e.message
                    Log.e("AddIdentityScreen", "Error parsing key", e)
                }
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val keyContent = inputStream.reader().readText()
                    onKeyContentRead(keyContent)
                    keyContentForParsing = keyContent
                }
            }
        },
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Select a private key file from your device.")
        Button(onClick = { filePickerLauncher.launch("*/*") }) {
            Text("Select File")
        }
        if (error != null) {
            Text("Error: $error")
        } else if (keyType != null) {
            Text("Key Type: ${keyTypes[keyType]}")
        }
    }
}

@Composable
fun GenerateKeyTab(keyType: Int?, onKeyTypeSelected: (Int) -> Unit) {
    val keyTypeOptions = listOf("ED25519" to KeyPair.ED25519, "RSA" to KeyPair.RSA)
    val selectedKeyType = keyType ?: KeyPair.ED25519

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Select the type of key to generate:")
        keyTypeOptions.forEach { (name, type) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onKeyTypeSelected(type) }
                    .padding(vertical = 4.dp),
            ) {
                RadioButton(
                    selected = (selectedKeyType == type),
                    onClick = { onKeyTypeSelected(type) },
                )
                Text(text = name, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

fun getClipEntryText(clipData: ClipData): String? {
    val itemCount = clipData.itemCount
    var textFull = ""
    for (i in 0..<itemCount) {
        val item = clipData.getItemAt(i)
        val text = item?.text
        if (text != null)
            textFull += text
    }
    return textFull.ifEmpty { null }
}

@Composable
fun ManualEntryTab(
    privateKey: String,
    onPrivateKeyChange: (String) -> Unit,
    onNameSuggestion: (String) -> Unit,
    isError: Boolean,
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(privateKey) {
        withContext(Dispatchers.IO) {
            try {
                val keypair = KeyPair.load(JSch(), privateKey.toByteArray(), null)
                val comment = keypair.publicKeyComment
                if (comment.isNotBlank()) {
                    onNameSuggestion(comment)
                }
                keypair.dispose()
            } catch (e: Exception) {
                Log.e("AddIdentityScreen", "Error parsing key", e)
            }
        }
    }

    OutlinedTextField(
        value = privateKey,
        onValueChange = onPrivateKeyChange,
        label = { Text("Private Key") },
        placeholder = { Text("-----BEGIN OPENSSH PRIVATE KEY-----") },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        isError = isError,
        supportingText = { if (isError) Text("Invalid key format") },
    )
    Spacer(modifier = Modifier.height(8.dp))
    TextButton(
        onClick = {
            coroutineScope.launch {
                val clipEntry = clipboard.getClipEntry() ?: return@launch
                val clipboardText = getClipEntryText(clipEntry.clipData) ?: return@launch
                onPrivateKeyChange(clipboardText)
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Default.ContentPaste,
            contentDescription = "Paste from clipboard",
            modifier = Modifier.padding(end = 8.dp),
        )
        Text("Paste from clipboard")
    }
}

@Preview(showBackground = true)
@Composable
fun AddIdentityScreenPreview_ImportTab() {
    AddIdentityScreen(onKeySaved = { _, _ -> }, onKeyGenerated = { _, _, _ -> }, onNavigateUp = {})
}

@Preview(showBackground = true)
@Composable
fun GenerateKeyTabPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        GenerateKeyTab(keyType = KeyPair.ED25519, onKeyTypeSelected = {})
    }
}

@Preview(showBackground = true)
@Composable
fun ManualEntryTabPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        ManualEntryTab(
            privateKey = "",
            onPrivateKeyChange = {},
            onNameSuggestion = {},
            isError = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ManualEntryTabWithErrorPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        ManualEntryTab(
            privateKey = "invalid key data",
            onPrivateKeyChange = {},
            onNameSuggestion = {},
            isError = true,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ImportFileTabPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        ImportFileTab(keyType = null, onKeyContentRead = {}, onNameSuggestion = {})
    }
}
