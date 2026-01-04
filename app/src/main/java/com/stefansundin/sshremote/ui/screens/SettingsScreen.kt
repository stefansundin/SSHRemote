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

import android.Manifest
import android.app.Activity
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.SoundEffectConstants
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.stefansundin.sshremote.BuildConfig
import com.stefansundin.sshremote.data.settings.ImportStrategy
import com.stefansundin.sshremote.data.settings.SettingsEvent
import com.stefansundin.sshremote.data.settings.SettingsViewModel
import com.stefansundin.sshremote.ui.components.ColorSettingDialog
import com.stefansundin.sshremote.ui.components.HapticFeedbackSettingDialog
import com.stefansundin.sshremote.ui.components.ThemeSettingDialog
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.Color as AndroidColor

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp, bottom = 8.dp),
    )
    content()
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val view = LocalView.current

    Column(
        modifier = modifier
            .clickable(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onClick()
                },
            )
            .padding(vertical = 12.dp, horizontal = 16.dp),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                onCheckedChange(!checked)
            }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun ImportSettingsDialog(
    onDismissRequest: () -> Unit,
    onImport: (ImportStrategy) -> Unit,
) {
    val view = LocalView.current

    AlertDialog(
        title = { Text("Import settings") },
        text = { Text("In case of conflicts with existing hosts, do you want to update or duplicate?\n\nYou can also choose Replace which will delete all existing hosts.") },
        properties = DialogProperties(dismissOnClickOutside = false),
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onImport(ImportStrategy.Upsert)
                    },
                ) {
                    Text("Update")
                }
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onImport(ImportStrategy.Duplicate)
                    },
                ) {
                    Text("Duplicate")
                }
                TextButton(
                    onClick = {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onImport(ImportStrategy.Replace)
                    },
                ) {
                    Text("Replace")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onDismissRequest()
                },
            ) {
                Text("Cancel")
            }
        },
    )
}

val ColorSaver = Saver<Color?, Int>(
    save = { it?.toArgb() ?: 0 },
    restore = { if (it != 0) Color(it) else null },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToIdentityList: () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    val savedTheme by settingsViewModel.theme.collectAsState()
    var previewTheme by rememberSaveable { mutableStateOf(savedTheme) }
    val useDynamicColors by settingsViewModel.useDynamicColors.collectAsState()
    var previewUseDynamicColors by rememberSaveable { mutableStateOf(useDynamicColors) }
    val backgroundColor by settingsViewModel.backgroundColor.collectAsState()
    var previewBackgroundColor by rememberSaveable(stateSaver = ColorSaver) { mutableStateOf(backgroundColor) }
    val primaryColor by settingsViewModel.primaryColor.collectAsState()
    var previewPrimaryColor by rememberSaveable(stateSaver = ColorSaver) { mutableStateOf(primaryColor) }
    val onPrimaryColor by settingsViewModel.onPrimaryColor.collectAsState()
    var previewOnPrimaryColor by rememberSaveable(stateSaver = ColorSaver) { mutableStateOf(onPrimaryColor) }

    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showColorDialog by rememberSaveable { mutableStateOf(false) }
    val savedHapticFeedback by settingsViewModel.hapticFeedback.collectAsState()
    var previewHapticFeedback by rememberSaveable { mutableStateOf(savedHapticFeedback) }
    var showHapticFeedbackDialog by rememberSaveable { mutableStateOf(false) }
    var importUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var importJson by rememberSaveable { mutableStateOf<String?>(null) }
    var exportJson by rememberSaveable { mutableStateOf<String?>(null) }
    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsState()
    val keepScreenOn by settingsViewModel.keepScreenOn.collectAsState()
    val strictHostKeyChecking by settingsViewModel.strictHostKeyChecking.collectAsState()
    val hasHosts by settingsViewModel.hasHosts.collectAsState()

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            settingsViewModel.setNotificationsEnabled(true)
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            uri?.let {
                settingsViewModel.exportSettings(context, uri)
            }
        },
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { importUri = it }
        },
    )

    val qrScanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            importJson = result.contents
        }
    }

    val uriHandler = LocalUriHandler.current

    // Required to react to theme changes when importing settings
    LaunchedEffect(savedTheme, useDynamicColors, backgroundColor, primaryColor, onPrimaryColor) {
        previewTheme = savedTheme
        previewUseDynamicColors = useDynamicColors
        previewBackgroundColor = backgroundColor
        previewPrimaryColor = primaryColor
        previewOnPrimaryColor = onPrimaryColor
    }

    LaunchedEffect(Unit) {
        settingsViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is SettingsEvent.ImportSuccess -> {
                    val message = "Successfully imported ${event.count} hosts."
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }

                is SettingsEvent.ImportError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is SettingsEvent.RequestPostNotificationsPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        }
    }

    importUri?.let { uri ->
        if (hasHosts) {
            ImportSettingsDialog(
                onDismissRequest = { importUri = null },
                onImport = { strategy ->
                    settingsViewModel.importSettings(context, uri, strategy)
                    importUri = null
                },
            )
        } else {
            LaunchedEffect(Unit) {
                settingsViewModel.importSettings(context, uri)
                importUri = null
            }
        }
    }

    importJson?.let { json ->
        if (hasHosts) {
            ImportSettingsDialog(
                onDismissRequest = { importJson = null },
                onImport = { strategy ->
                    settingsViewModel.importSettings(context, json, strategy)
                    importJson = null
                },
            )
        } else {
            LaunchedEffect(Unit) {
                settingsViewModel.importSettings(context, json)
                importJson = null
            }
        }
    }

    exportJson?.let { json ->
        ExportSettingsQrCodeDialog(
            json = json,
            onDismissRequest = { exportJson = null },
            onError = {
                exportJson = null
                Toast.makeText(
                    context,
                    "Data is too large for a QR code. Use file export instead.",
                    Toast.LENGTH_LONG,
                ).show()
            },
        )
    }

    SSHRemoteTheme(
        previewTheme,
        previewUseDynamicColors,
        {
            var scheme = this
            if (previewBackgroundColor != null) {
                scheme = scheme.copy(
                    background = previewBackgroundColor!!,
                    surface = previewBackgroundColor!!,
                )
            }
            if (previewPrimaryColor != null) {
                scheme = scheme.copy(primary = previewPrimaryColor!!)
            }
            if (previewOnPrimaryColor != null) {
                scheme = scheme.copy(onPrimary = previewOnPrimaryColor!!)
            }
            scheme
        },
    ) {
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

        if (showColorDialog) {
            ColorSettingDialog(
                useDynamicColors = previewUseDynamicColors,
                onUseDynamicColorsChange = { previewUseDynamicColors = it },
                backgroundColor = previewBackgroundColor,
                onBackgroundColorChange = { previewBackgroundColor = it },
                primaryColor = previewPrimaryColor,
                onPrimaryColorChange = { previewPrimaryColor = it },
                onPrimaryColorColor = previewOnPrimaryColor,
                onOnPrimaryColorChange = { previewOnPrimaryColor = it },
                onConfirm = {
                    settingsViewModel.setUseDynamicColors(previewUseDynamicColors)
                    settingsViewModel.setBackgroundColor(previewBackgroundColor)
                    settingsViewModel.setPrimaryColor(previewPrimaryColor)
                    settingsViewModel.setOnPrimaryColor(previewOnPrimaryColor)
                    showColorDialog = false
                },
                onDismiss = {
                    previewUseDynamicColors = useDynamicColors
                    previewBackgroundColor = backgroundColor
                    previewPrimaryColor = primaryColor
                    previewOnPrimaryColor = onPrimaryColor
                    showColorDialog = false
                },
            )
        }

        if (showHapticFeedbackDialog) {
            HapticFeedbackSettingDialog(
                currentHapticFeedback = previewHapticFeedback,
                onHapticFeedbackSelected = { newHapticFeedback ->
                    previewHapticFeedback = newHapticFeedback
                },
                onConfirm = {
                    settingsViewModel.setHapticFeedback(previewHapticFeedback)
                    showHapticFeedbackDialog = false
                },
                onDismiss = {
                    previewHapticFeedback = savedHapticFeedback
                    showHapticFeedbackDialog = false
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
                                view.playSoundEffect(SoundEffectConstants.CLICK)
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
                    .verticalScroll(rememberScrollState()),
            ) {
                SettingsGroup("Appearance") {
                    SettingsItem(
                        title = "Theme",
                        subtitle = savedTheme.name.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            previewTheme = savedTheme
                            showThemeDialog = true
                        },
                    )
                    SettingsItem(
                        title = "Colors",
                        subtitle = "Customize the application colors",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            previewUseDynamicColors = useDynamicColors
                            previewBackgroundColor = backgroundColor
                            previewPrimaryColor = primaryColor
                            previewOnPrimaryColor = onPrimaryColor
                            showColorDialog = true
                        },
                    )
                }

                HorizontalDivider()

                SettingsGroup("Remote control") {
                    SettingsItem(
                        title = "Haptic feedback",
                        subtitle = savedHapticFeedback.label,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            previewHapticFeedback = savedHapticFeedback
                            showHapticFeedbackDialog = true
                        },
                    )
                    SettingsSwitchItem(
                        title = "Keep screen on",
                        checked = keepScreenOn,
                        onCheckedChange = { settingsViewModel.setKeepScreenOn(it) },
                    )
                    SettingsSwitchItem(
                        title = "Show notification",
                        checked = notificationsEnabled,
                        onCheckedChange = {
                            if (it) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    settingsViewModel.setNotificationsEnabled(true)
                                }
                            } else {
                                settingsViewModel.setNotificationsEnabled(false)
                            }
                        },
                    )
                }

                HorizontalDivider()

                SettingsGroup("Security") {
                    SettingsItem(
                        title = "SSH Keys",
                        subtitle = "Manage SSH keys for authentication",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToIdentityList,
                    )
                    SettingsSwitchItem(
                        title = "Strict host key checking",
                        checked = strictHostKeyChecking,
                        onCheckedChange = { settingsViewModel.setStrictHostKeyChecking(it) },
                    )
                }

                HorizontalDivider()

                SettingsGroup("Data") {
                    SettingsItem(
                        title = "Export to file",
                        subtitle = "Export settings and hosts to a file.",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                            val filename = "ssh-remote-settings-${dateFormat.format(Date())}.json"
                            exportLauncher.launch(filename)
                        },
                    )
                    SettingsItem(
                        title = "Import from file",
                        subtitle = "Import settings and hosts from a file.",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { importLauncher.launch(arrayOf("application/json")) },
                    )
                    SettingsItem(
                        title = "Export to QR code",
                        subtitle = "Export settings and hosts to a QR code.",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            coroutineScope.launch {
                                exportJson = settingsViewModel.exportSettingsToString(context)
                            }
                        },
                    )
                    SettingsItem(
                        title = "Import from QR code",
                        subtitle = "Import settings and hosts from a QR code.",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val options = ScanOptions()
                            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                            options.setPrompt("Scan a QR code to import settings")
                            options.setBeepEnabled(false)
                            options.setOrientationLocked(false)
                            qrScanLauncher.launch(options)
                        },
                    )
                }

                HorizontalDivider()

                SettingsGroup("About SSH Remote") {
                    SettingsItem(
                        title = "App version",
                        subtitle = BuildConfig.VERSION_NAME,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SettingsItem(
                        title = "Author",
                        subtitle = "Developed by Stefan Sundin",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SettingsItem(
                        title = "Donate",
                        subtitle = "Support the developer",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { uriHandler.openUri("https://stefansundin.github.io/donate") },
                    )
                    SettingsItem(
                        title = "License",
                        subtitle = "GNU General Public License v3.0",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { uriHandler.openUri("https://www.gnu.org/licenses/gpl-3.0.html") },
                    )
                    SettingsItem(
                        title = "Source code",
                        subtitle = "View on GitHub",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { uriHandler.openUri("https://github.com/stefansundin/SSHRemote") },
                    )
                    SettingsItem(
                        title = "SSH library",
                        subtitle = "Using mwiede/jsch ${BuildConfig.JSCH_VERSION}",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { uriHandler.openUri("https://github.com/mwiede/jsch") },
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ExportSettingsQrCodeDialog(
    json: String,
    onDismissRequest: () -> Unit,
    onError: (Exception) -> Unit,
) {
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val configuration = LocalConfiguration.current
    val view = LocalView.current

    if (!view.isInEditMode) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            DisposableEffect(configuration.orientation) {
                val windowInsetsController = WindowCompat.getInsetsController(window, view)
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                if (isLandscape) {
                    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                    windowInsetsController.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
                onDispose {
                    if (isLandscape) {
                        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
                    }
                }
            }
        }
    }

    LaunchedEffect(json, imageSize) {
        if (imageSize.width > 0) {
            withContext(Dispatchers.Default) {
                val generatedBitmap = try {
                    val size = imageSize.width
                    val hints = mapOf(
                        EncodeHintType.CHARACTER_SET to "UTF-8",
                        EncodeHintType.MARGIN to 1,
                    )
                    val writer = QRCodeWriter()
                    val bitMatrix = writer.encode(json, BarcodeFormat.QR_CODE, size, size, hints)
                    val width = bitMatrix.width
                    val height = bitMatrix.height
                    val bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)
                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            bitmap[x, y] = if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
                        }
                    }
                    bitmap
                } catch (e: WriterException) {
                    withContext(Dispatchers.Main) {
                        onError(e)
                    }
                    null
                }
                qrCodeBitmap = generatedBitmap
            }
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        Text(
                            "Scan the QR code to import settings",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 24.dp),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .aspectRatio(1f)
                            .onSizeChanged {
                                if (it.width > 0 && it != imageSize) {
                                    imageSize = it
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (qrCodeBitmap != null) {
                            Image(
                                bitmap = qrCodeBitmap!!.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = Color.White,
                            ) {
                                CircularProgressIndicator(strokeWidth = 16.dp, modifier = Modifier.padding(128.dp))
                            }
                        }
                    }

                    if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        TextButton(
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                onDismissRequest()
                            },
                            modifier = Modifier.padding(top = 24.dp),
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}
