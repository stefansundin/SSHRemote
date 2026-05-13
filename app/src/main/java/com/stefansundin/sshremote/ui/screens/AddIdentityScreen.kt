/*
 * SSH Remote
 * Copyright (C) 2026  Stefan Sundin
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
import android.content.res.Configuration
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import android.view.SoundEffectConstants
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.ui.components.NoWrapOnSpecialCharactersVisualTransformation
import com.stefansundin.sshremote.ui.dpadFocusable
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream

val keyTypes = mapOf(KeyPair.ED25519 to "Ed25519", KeyPair.RSA to "RSA")

@Parcelize
data class IdentityKeyData(
    val name: String,
    val privateKey: String,
    val certificate: String? = null,
) : Parcelable

private const val OPENSSH_PRIVATE_KEY_BEGIN_MARKER = "-----BEGIN OPENSSH PRIVATE KEY-----"
private const val OPENSSH_PRIVATE_KEY_END_MARKER = "-----END OPENSSH PRIVATE KEY-----"

fun isOpenSshCertificateLine(line: String): Boolean {
    val parts = line.trim().split(Regex("\\s+"), limit = 3)
    return parts.size >= 2 && parts[0].contains("-cert-v01@openssh.com")
}

fun decodeBase64OrNull(content: String): ByteArray? {
    val trimmed = content.trim()
    if (trimmed.isEmpty()) {
        return null
    }

    return try {
        Base64.getMimeDecoder().decode(trimmed)
    } catch (_: IllegalArgumentException) {
        try {
            Base64.getDecoder().decode(trimmed)
        } catch (_: IllegalArgumentException) {
            try {
                Base64.getUrlDecoder().decode(trimmed)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }
}

fun splitPrivateKeyAndCertificate(content: String): Pair<String, String?> {
    val trimmedContent = content.trim()
    val beginMarkerIndex = trimmedContent.indexOf(OPENSSH_PRIVATE_KEY_BEGIN_MARKER)
    if (beginMarkerIndex == -1) {
        return trimmedContent to null
    }

    val endMarkerIndex = trimmedContent.indexOf(OPENSSH_PRIVATE_KEY_END_MARKER, beginMarkerIndex)
    if (endMarkerIndex == -1) {
        return trimmedContent to null
    }

    val privateKeyEndIndex = endMarkerIndex + OPENSSH_PRIVATE_KEY_END_MARKER.length
    val privateKeyContent = trimmedContent.substring(beginMarkerIndex, privateKeyEndIndex).trim()

    val trailingLine = trimmedContent
        .substring(privateKeyEndIndex)
        .lineSequence()
        .map { it.trim() }
        .firstOrNull { it.isNotBlank() }

    val leadingLine = trimmedContent
        .substring(0, beginMarkerIndex)
        .lineSequence()
        .map { it.trim() }
        .lastOrNull { it.isNotBlank() }

    val certificate = trailingLine?.takeIf(::isOpenSshCertificateLine)
        ?: leadingLine?.takeIf(::isOpenSshCertificateLine)

    return privateKeyContent to certificate
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIdentityScreen(
    onKeysSaved: (keys: List<IdentityKeyData>) -> Unit,
    onKeyGenerated: suspend (name: String, type: Int, size: Int?, comment: String) -> Unit,
    onNavigateUp: () -> Unit,
    scanQrCodeOnStart: Boolean = false,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var privateKey by rememberSaveable { mutableStateOf("") }
    var certificate by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabTitles = listOf(
        stringResource(R.string.tab_import),
        stringResource(R.string.tab_generate),
        stringResource(R.string.tab_manual),
    )
    var isKeyContentValid by rememberSaveable { mutableStateOf(false) }
    var importKeyTypeDescription by rememberSaveable { mutableStateOf<String?>(null) }
    var zipKeys by rememberSaveable { mutableStateOf(emptyList<IdentityKeyData>()) }
    var generateKeyType by rememberSaveable { mutableStateOf<Pair<Int, Int?>>(Pair(KeyPair.ED25519, null)) }
    var isGenerating by rememberSaveable { mutableStateOf(false) }
    var scanQrCode by rememberSaveable { mutableStateOf(scanQrCodeOnStart) }
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current
    val context = LocalContext.current
    val resources = LocalResources.current

    var showSaveDialog by rememberSaveable { mutableStateOf(false) }
    var importError by rememberSaveable { mutableStateOf<String?>(null) }

    val hasZipKeys = zipKeys.isNotEmpty()
    val zipSummary = if (hasZipKeys) resources.getQuantityString(
        R.plurals.zip_keys_detected_in_zip,
        zipKeys.size,
        zipKeys.size,
    ) else null
    val hasUnsavedChanges = privateKey.isNotBlank() || hasZipKeys
    val isFormValid = (selectedTabIndex == 0 && (isKeyContentValid || hasZipKeys)) ||
            (selectedTabIndex == 1) ||
            (selectedTabIndex == 2 && isKeyContentValid)

    val keyNamePrefix = stringResource(R.string.generated_key_name)
    val invalidKeyFormatMsg = stringResource(R.string.invalid_key_format)
    val zipNoValidKeysMsg = stringResource(R.string.zip_no_valid_keys)

    fun saveZipKeys() {
        val keys = zipKeys.mapIndexed { index, key ->
            if (key.name.isBlank()) key.copy(name = "$keyNamePrefix ${index + 1}") else key
        }
        onKeysSaved(keys)
    }

    fun handleSave() {
        val finalName = name.ifBlank {
            val dateAndTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            "$keyNamePrefix $dateAndTime"
        }
        val (privateKeyContent, parsedCertificate) = splitPrivateKeyAndCertificate(privateKey)
        val certificateToSave = parsedCertificate ?: certificate
        when (selectedTabIndex) {
            0 -> {
                if (hasZipKeys) {
                    saveZipKeys()
                } else {
                    onKeysSaved(
                        listOf(
                            IdentityKeyData(
                                name = finalName,
                                privateKey = privateKeyContent,
                                certificate = certificateToSave,
                            ),
                        ),
                    )
                }
            }

            1 -> {
                coroutineScope.launch {
                    isGenerating = true
                    onKeyGenerated(finalName, generateKeyType.first, generateKeyType.second, finalName)
                }
            }

            2 -> {
                onKeysSaved(
                    listOf(
                        IdentityKeyData(
                            name = finalName,
                            privateKey = privateKeyContent,
                            certificate = certificateToSave,
                        ),
                    ),
                )
            }
        }
    }

    fun parsePrivateKeyContent(content: String): IdentityKeyData {
        var finalKey = content.trim()
        if (!finalKey.startsWith("-----")) {
            val decoded = decodeBase64OrNull(finalKey)
                ?: throw IllegalArgumentException("Invalid base64-encoded key payload")
            val inputStream = GZIPInputStream(ByteArrayInputStream(decoded))
            val decompressed = inputStream.bufferedReader(StandardCharsets.UTF_8).readText()
            finalKey = decompressed.trim()
        }
        val (privateKeyContent, parsedCertificate) = splitPrivateKeyAndCertificate(finalKey)
        val keyPair = KeyPair.load(JSch(), privateKeyContent.toByteArray(), null)
        val comment = keyPair.publicKeyComment.trim()
        keyPair.dispose()
        return IdentityKeyData(
            name = comment,
            privateKey = privateKeyContent,
            certificate = parsedCertificate,
        )
    }

    fun isZipContent(content: ByteArray): Boolean {
        if (content.size < 4) return false
        return content[0] == 'P'.code.toByte() &&
                content[1] == 'K'.code.toByte() &&
                (content[2] == 3.toByte() || content[2] == 5.toByte() || content[2] == 7.toByte()) &&
                (content[3] == 4.toByte() || content[3] == 6.toByte() || content[3] == 8.toByte())
    }

    fun readZipTextEntries(content: ByteArray): Map<String, String> {
        val entries = linkedMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(content)).use { zipStream ->
            var entry = zipStream.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val entryPath = entry.name.trim().removePrefix("./").trimStart('/')
                    val entryText = zipStream.readBytes().toString(StandardCharsets.UTF_8).trim()
                    if (entryText.isNotBlank()) {
                        entries.putIfAbsent(entryPath, entryText)
                    }
                }
                zipStream.closeEntry()
                entry = zipStream.nextEntry
            }
        }
        return entries
    }


    fun parseAndSetZipKeys(content: ByteArray) {
        coroutineScope.launch(Dispatchers.IO) {
            val validKeys = mutableListOf<IdentityKeyData>()
            try {
                val zipContentByPath = readZipTextEntries(content)

                zipContentByPath.forEach { (entryPath, entryText) ->
                    if (entryPath.endsWith(".pub", ignoreCase = true)) {
                        return@forEach
                    }
                    try {
                        val parsedKey = parsePrivateKeyContent(entryText)
                        val certificatePath = "$entryPath-cert.pub"
                        val fallbackCertificatePath = "${entryPath.substringBeforeLast('.', entryPath)}-cert.pub"
                        val certificate = zipContentByPath[certificatePath]
                            ?: zipContentByPath.entries.firstOrNull {
                                it.key.equals(certificatePath, ignoreCase = true) ||
                                        it.key.equals(fallbackCertificatePath, ignoreCase = true)
                            }?.value
                        val entryFileName = entryPath.substringAfterLast('/').trim()
                        val resolvedName = parsedKey.name.ifBlank { entryFileName }
                        validKeys += IdentityKeyData(
                            name = resolvedName,
                            privateKey = parsedKey.privateKey,
                            certificate = certificate ?: parsedKey.certificate,
                        )
                    } catch (_: Exception) {
                    }
                }

                withContext(Dispatchers.Main) {
                    importError = null
                    importKeyTypeDescription = null
                    if (validKeys.isEmpty()) {
                        zipKeys = emptyList()
                        certificate = null
                        importError = zipNoValidKeysMsg
                        return@withContext
                    }

                    privateKey = ""
                    certificate = null
                    zipKeys = validKeys
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    zipKeys = emptyList()
                    certificate = null
                    importKeyTypeDescription = null
                    importError = e.message ?: invalidKeyFormatMsg
                }
                Log.e("AddIdentityScreen", "Error parsing zip", e)
            }
        }
    }

    fun parseAndSetKey(content: String) {
        coroutineScope.launch(Dispatchers.IO) {
            // Check if the content is a base64-encoded zip file
            val trimmed = content.trim()
            if (!trimmed.startsWith("-----")) {
                val decoded = decodeBase64OrNull(trimmed)
                if (decoded != null && isZipContent(decoded)) {
                    parseAndSetZipKeys(decoded)
                    return@launch
                }
            }

            try {
                val parsedKey = parsePrivateKeyContent(content)

                withContext(Dispatchers.Main) {
                    importError = null
                    zipKeys = emptyList()
                    certificate = parsedKey.certificate
                    privateKey = buildString {
                        append(parsedKey.privateKey)
                        if (!parsedKey.certificate.isNullOrBlank()) {
                            append("\n")
                            append(parsedKey.certificate)
                        }
                    }
                    if (parsedKey.name.isNotBlank()) {
                        name = parsedKey.name
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    zipKeys = emptyList()
                    certificate = null
                    privateKey = content
                    importError = e.message ?: invalidKeyFormatMsg
                }
                Log.e("AddIdentityScreen", "Error parsing key", e)
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val content = inputStream.readBytes()
                    if (isZipContent(content)) {
                        parseAndSetZipKeys(content)
                    } else {
                        parseAndSetKey(content.toString(StandardCharsets.UTF_8))
                    }
                }
            }
        },
    )

    val qrScanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            parseAndSetKey(result.contents)
        }
    }

    val scanQrCodeKeyPrompt = stringResource(R.string.scan_qr_code_key_prompt)
    LaunchedEffect(scanQrCode) {
        if (scanQrCode) {
            scanQrCode = false
            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            options.setPrompt(scanQrCodeKeyPrompt)
            options.setBeepEnabled(false)
            options.setOrientationLocked(false)
            qrScanLauncher.launch(options)
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.unsaved_changes_title)) },
            text = { Text(stringResource(R.string.unsaved_key_changes_text)) },
            properties = DialogProperties(dismissOnClickOutside = false),
            onDismissRequest = { showSaveDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        handleSave()
                    },
                ) {
                    Text(stringResource(R.string.save_and_leave))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onNavigateUp()
                    },
                ) {
                    Text(stringResource(R.string.discard_and_leave))
                }
            },
        )
    }

    if (hasZipKeys) {
        AlertDialog(
            title = { Text(stringResource(R.string.zip_keys_detected_title)) },
            text = { Text(zipSummary!!) },
            properties = DialogProperties(dismissOnClickOutside = false),
            onDismissRequest = { zipKeys = emptyList() },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        saveZipKeys()
                    },
                ) {
                    Text(stringResource(R.string.tab_import))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        zipKeys = emptyList()
                        certificate = null
                    },
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (isGenerating) {
        AlertDialog(
            title = { Text(stringResource(R.string.generating_key_title)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.please_wait_generating))
                }
            },
            onDismissRequest = { /* cannot be dismissed */ },
            confirmButton = {},
        )
    }

    BackHandler(enabled = hasUnsavedChanges && isFormValid && !isGenerating) {
        showSaveDialog = true
    }

    LaunchedEffect(privateKey, selectedTabIndex) {
        isKeyContentValid = if (selectedTabIndex == 0 || selectedTabIndex == 2) {
            if (privateKey.isBlank()) {
                false
            } else {
                try {
                    val (privateKeyContent, parsedCertificate) = splitPrivateKeyAndCertificate(privateKey)
                    val keyPair = KeyPair.load(JSch(), privateKeyContent.toByteArray(), null)
                    if (keyTypes.containsKey(keyPair.keyType)) {
                        importKeyTypeDescription = keyTypes[keyPair.keyType]
                        if (keyPair.keyType == KeyPair.RSA) {
                            importKeyTypeDescription += " (${keyPair.keySize}-bit)"
                        }
                    } else {
                        importKeyTypeDescription = keyPair.keyTypeString
                    }
                    certificate = parsedCertificate
                    keyPair.dispose()
                    true
                } catch (_: Exception) {
                    certificate = null
                    false
                }
            }
        } else {
            true
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_ssh_key_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            if (hasUnsavedChanges && isFormValid) {
                                showSaveDialog = true
                            } else {
                                onNavigateUp()
                            }
                        },
                        enabled = !isGenerating,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (isFormValid && !isGenerating) {
                FloatingActionButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        handleSave()
                    },
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = stringResource(R.string.save),
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.key_name_optional)) },
                placeholder = { Text(stringResource(R.string.key_name_default_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .dpadFocusable(),
                singleLine = true,
            )

            SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            selectedTabIndex = index
                        },
                        text = { Text(title) },
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                when (selectedTabIndex) {
                    0 -> {
                        ImportTab(
                            keyTypeDescription = importKeyTypeDescription,
                            error = importError,
                            onPickFile = {
                                filePickerLauncher.launch("*/*")
                            },
                            onScanQr = {
                                scanQrCode = true
                            },
                        )
                    }

                    1 -> GenerateKeyTab(
                        selectedKeyType = generateKeyType,
                        onKeyTypeSelected = { generateKeyType = it },
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
fun ImportTab(
    keyTypeDescription: String?,
    error: String?,
    onPickFile: () -> Unit,
    onScanQr: () -> Unit,
) {
    val view = LocalView.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.import_key_file_prompt))
        Button(
            onClick = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                onPickFile()
            },
        ) {
            Text(stringResource(R.string.select_file))
        }

        Text(stringResource(R.string.or_scan_qr_code))
        Button(
            onClick = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                onScanQr()
            },
        ) {
            Text(stringResource(R.string.scan_qr_code))
        }

        if (error != null) {
            Text(stringResource(R.string.error_format, error))
        } else if (keyTypeDescription != null) {
            Text(stringResource(R.string.key_type_format, keyTypeDescription))
        }
    }
}

@Composable
fun GenerateKeyTab(selectedKeyType: Pair<Int, Int?>, onKeyTypeSelected: (Pair<Int, Int?>) -> Unit) {
    val keyTypeOptions = listOf(
        "Ed25519" to Pair(KeyPair.ED25519, null),
        stringResource(R.string.key_create_type_format, "RSA", 2048) to Pair(KeyPair.RSA, 2048),
        stringResource(R.string.key_create_type_format, "RSA", 4096) to Pair(KeyPair.RSA, 4096),
    )
    val view = LocalView.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.select_key_type_prompt))
        keyTypeOptions.forEach { (label, type) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onKeyTypeSelected(type)
                    }
                    .padding(vertical = 4.dp),
            ) {
                RadioButton(
                    selected = (selectedKeyType == type),
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onKeyTypeSelected(type)
                    },
                )
                Text(label, modifier = Modifier.padding(start = 8.dp))
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
        if (text != null) {
            textFull += text
        }
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
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(privateKey) {
        withContext(Dispatchers.IO) {
            try {
                val (privateKeyContent, _) = splitPrivateKeyAndCertificate(privateKey)
                val keyPair = KeyPair.load(JSch(), privateKeyContent.toByteArray(), null)
                val comment = keyPair.publicKeyComment
                if (comment.isNotBlank()) {
                    onNameSuggestion(comment)
                }
                keyPair.dispose()
            } catch (e: Exception) {
                Log.e("AddIdentityScreen", "Error parsing key", e)
            }
        }
    }

    OutlinedTextField(
        value = privateKey,
        onValueChange = onPrivateKeyChange,
        label = { Text(stringResource(R.string.private_key)) },
        placeholder = { Text("-----BEGIN OPENSSH PRIVATE KEY-----") },
        supportingText = { if (isError) Text(stringResource(R.string.invalid_key_format)) },
        isError = isError,
        visualTransformation = NoWrapOnSpecialCharactersVisualTransformation,
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
    )
    Spacer(modifier = Modifier.height(8.dp))
    TextButton(
        onClick = {
            view.playSoundEffect(SoundEffectConstants.CLICK)
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
            contentDescription = stringResource(R.string.paste_from_clipboard),
            modifier = Modifier.padding(end = 8.dp),
        )
        Text(stringResource(R.string.paste_from_clipboard))
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun AddIdentityScreenPreview_ImportTab() {
    SSHRemoteTheme {
        AddIdentityScreen(onKeysSaved = {}, onKeyGenerated = { _, _, _, _ -> }, onNavigateUp = {})
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun ImportTabPreview() {
    SSHRemoteTheme {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                ImportTab(
                    keyTypeDescription = "RSA 4096-bit",
                    error = null,
                    onPickFile = {},
                    onScanQr = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun GenerateKeyTabPreview() {
    SSHRemoteTheme {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                GenerateKeyTab(selectedKeyType = Pair(KeyPair.ED25519, null), onKeyTypeSelected = {})
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun ManualEntryTabPreview() {
    SSHRemoteTheme {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                ManualEntryTab(
                    privateKey = "",
                    onPrivateKeyChange = {},
                    onNameSuggestion = {},
                    isError = false,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun ManualEntryTabWithErrorPreview() {
    SSHRemoteTheme {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                ManualEntryTab(
                    privateKey = "invalid key data",
                    onPrivateKeyChange = {},
                    onNameSuggestion = {},
                    isError = true,
                )
            }
        }
    }
}
