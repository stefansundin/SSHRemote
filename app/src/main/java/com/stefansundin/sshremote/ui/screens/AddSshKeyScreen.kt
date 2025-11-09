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

import android.content.ClipData
import android.net.Uri
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.unit.dp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun isKeyValid(privateKey: String): Boolean {
    if (privateKey.isBlank()) return false
    return try {
        val jsch = JSch()
        val keyPair = KeyPair.load(jsch, privateKey.toByteArray(), null)
        keyPair.dispose()
        true
    } catch (e: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSshKeyScreen(
    onKeySaved: (name: String, privateKey: String) -> Unit,
    onKeyGenerated: (name: String, type: Int, comment: String) -> Unit,
    onNavigateUp: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var privateKey by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Import File", "Generate", "Enter Manually")
    var isKeyContentValid by remember { mutableStateOf(false) }

    LaunchedEffect(privateKey, selectedTabIndex) {
        isKeyContentValid = if (selectedTabIndex == 0 || selectedTabIndex == 2) {
            isKeyValid(privateKey)
        } else {
            true
        }
    }

    val isFormValid = (selectedTabIndex == 0 && isKeyContentValid) ||
            (selectedTabIndex == 1) ||
            (selectedTabIndex == 2 && isKeyContentValid)

    var selectedKeyType by remember { mutableIntStateOf(KeyPair.ED25519) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add SSH Key") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
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
                        val finalName = name.ifBlank {
                            "Key ${
                                SimpleDateFormat(
                                    "yyyy-MM-dd HH:mm",
                                    Locale.getDefault()
                                ).format(Date())
                            }"
                        }
                        when (selectedTabIndex) {
                            0, 2 -> onKeySaved(finalName, privateKey)
                            1 -> onKeyGenerated(finalName, selectedKeyType, finalName)
                        }
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

            TabRow(selectedTabIndex = selectedTabIndex) {
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
                        onKeyContentRead = { content -> privateKey = content },
                        onNameSuggestion = { suggestedName -> name = suggestedName }
                    )
                    1 -> GenerateKeyTab(
                        selectedKeyType = selectedKeyType,
                        onKeyTypeSelected = { selectedKeyType = it }
                    )
                    2 -> ManualEntryTab(
                        privateKey = privateKey,
                        onPrivateKeyChange = { privateKey = it },
                        isError = privateKey.isNotBlank() && !isKeyContentValid
                    )
                }
            }
        }
    }
}

fun getClipEntryText(clipData: ClipData): String? {
        val itemCount = clipData.itemCount
        var textFull = ""
        for (i in 0 ..< itemCount) {
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
    isError: Boolean
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    OutlinedTextField(
        value = privateKey,
        onValueChange = onPrivateKeyChange,
        label = { Text("Private Key") },
        placeholder = { Text("Begins with -----BEGIN...-----") },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        isError = isError,
        supportingText = { if (isError) Text("Invalid key format") }
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.ContentPaste,
            contentDescription = "Paste from clipboard",
            modifier = Modifier.padding(end = 8.dp)
        )
        Text("Paste from clipboard")
    }
}

@Composable
fun ImportFileTab(
    onKeyContentRead: (String) -> Unit,
    onNameSuggestion: (String) -> Unit
) {
    val context = LocalContext.current
    var keyContentForParsing by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(keyContentForParsing) {
        keyContentForParsing?.let { content ->
            withContext(Dispatchers.IO) {
                try {
                    val keypair = KeyPair.load(JSch(), content.toByteArray(), null)
                    val comment = keypair.publicKeyComment
                    if (comment != null && comment.isNotBlank()) {
                        withContext(Dispatchers.Main) {
                            onNameSuggestion(comment)
                        }
                    }
                    keypair.dispose()
                } catch (e: Exception) {
                    e.printStackTrace()
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
        }
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
    }
}

@Composable
fun GenerateKeyTab(selectedKeyType: Int, onKeyTypeSelected: (Int) -> Unit) {
    val keyTypes = listOf("ED25519" to KeyPair.ED25519, "RSA" to KeyPair.RSA)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Select the type of key to generate:")
        keyTypes.forEach { (name, type) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onKeyTypeSelected(type) }
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = (selectedKeyType == type),
                    onClick = { onKeyTypeSelected(type) }
                )
                Text(text = name, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
