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

package com.stefansundin.sshremote.ui.components

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.view.SoundEffectConstants
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.mikepenz.markdown.compose.components.CurrentComponentsBridge
import com.mikepenz.markdown.compose.components.MarkdownComponentModel
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownCodeFence
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.rememberMarkdownState
import com.stefansundin.sshremote.R
import kotlinx.coroutines.launch

enum class MarkdownHorizontalOverflow {
    Default,
    Wrap,
    Scroll,
}

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    selectableText: Boolean = true,
    codeBlockOverflow: MarkdownHorizontalOverflow = MarkdownHorizontalOverflow.Wrap,
    tableOverflow: MarkdownHorizontalOverflow = MarkdownHorizontalOverflow.Wrap,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val clipboard = LocalClipboard.current
    val resources = LocalResources.current
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val scope = rememberCoroutineScope()
    var selectedLink by rememberSaveable { mutableStateOf<String?>(null) }
    val maxDialogHeight = with(density) { windowInfo.containerSize.height.toDp() * 0.9f }
    val markdownState = rememberMarkdownState(markdown)

    val interceptingUriHandler = remember {
        object : UriHandler {
            override fun openUri(uri: String) {
                selectedLink = uri
            }
        }
    }

    val components = remember(codeBlockOverflow, tableOverflow) {
        when {
            codeBlockOverflow == MarkdownHorizontalOverflow.Default && tableOverflow == MarkdownHorizontalOverflow.Default -> null
            codeBlockOverflow == MarkdownHorizontalOverflow.Default -> {
                markdownComponents(
                    table = { model: MarkdownComponentModel ->
                        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            CurrentComponentsBridge.table(model)
                        }
                    },
                )
            }
            tableOverflow != MarkdownHorizontalOverflow.Scroll -> {
                markdownComponents(
                    codeFence = { model: MarkdownComponentModel ->
                        MarkdownCodeComponent(model = model, overflow = codeBlockOverflow, fenced = true)
                    },
                    codeBlock = { model: MarkdownComponentModel ->
                        MarkdownCodeComponent(model = model, overflow = codeBlockOverflow, fenced = false)
                    },
                )
            }
            else -> {
                markdownComponents(
                    codeFence = { model: MarkdownComponentModel ->
                        MarkdownCodeComponent(model = model, overflow = codeBlockOverflow, fenced = true)
                    },
                    codeBlock = { model: MarkdownComponentModel ->
                        MarkdownCodeComponent(model = model, overflow = codeBlockOverflow, fenced = false)
                    },
                    table = { model: MarkdownComponentModel ->
                        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            CurrentComponentsBridge.table(model)
                        }
                    },
                )
            }
        }
    }

    CompositionLocalProvider(LocalUriHandler provides interceptingUriHandler) {
        OptionalSelectionContainer(selectableText = selectableText) {
            if (components == null) {
                Markdown(
                    markdownState,
                    modifier = modifier,
                )
            } else {
                Markdown(
                    markdownState,
                    modifier = modifier,
                    components = components,
                )
            }
        }
    }

    selectedLink?.let { url ->
        MarkdownLinkDialog(
            url = url,
            maxDialogHeight = maxDialogHeight,
            onDismiss = { selectedLink = null },
            onCopy = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                val clipData = ClipData.newPlainText(resources.getString(R.string.link_destination), url)
                scope.launch { clipboard.setClipEntry(clipData.toClipEntry()) }
            },
            onOpenInBrowser = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                if (openLinkInBrowser(context, url)) {
                    selectedLink = null
                }
            },
        )
    }
}

@Composable
private fun MarkdownCodeComponent(
    model: MarkdownComponentModel,
    overflow: MarkdownHorizontalOverflow,
    fenced: Boolean,
) {
    if (fenced) {
        MarkdownCodeFence(
            content = model.content,
            node = model.node,
            style = model.typography.code,
        ) { code, language, style ->
            MarkdownCodeContent(
                code = code,
                language = language,
                style = style,
                overflow = overflow,
            )
        }
    } else {
        MarkdownCodeBlock(
            content = model.content,
            node = model.node,
            style = model.typography.code,
        ) { code, language, style ->
            MarkdownCodeContent(
                code = code,
                language = language,
                style = style,
                overflow = overflow,
            )
        }
    }
}

@Composable
private fun OptionalSelectionContainer(
    selectableText: Boolean,
    content: @Composable () -> Unit,
) {
    if (selectableText) {
        SelectionContainer(content = content)
    } else {
        content()
    }
}

@Composable
private fun MarkdownCodeContent(
    code: String,
    language: String?,
    style: androidx.compose.ui.text.TextStyle,
    overflow: MarkdownHorizontalOverflow,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
    ) {
        Column {
            if (!language.isNullOrBlank()) {
                Text(
                    text = language,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }

            val contentModifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            val textContent: @Composable () -> Unit = {
                Text(
                    text = code,
                    style = style,
                    softWrap = overflow != MarkdownHorizontalOverflow.Scroll,
                    overflow = TextOverflow.Clip,
                    modifier = contentModifier,
                )
            }

            if (overflow == MarkdownHorizontalOverflow.Scroll) {
                Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    textContent()
                }
            } else {
                textContent()
            }
        }
    }
}

@Composable
private fun MarkdownLinkDialog(
    url: String,
    maxDialogHeight: Dp,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onOpenInBrowser: () -> Unit,
) {
    val canOpenInBrowser = remember(url) { canOpenInBrowser(url) }

    AlertDialog(
        modifier = Modifier
            .widthIn(max = 560.dp)
            .heightIn(max = maxDialogHeight),
        title = { Text(stringResource(R.string.link_destination)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                SelectionContainer {
                    Text(
                        text = url,
                        style = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                    )
                }
            }
        },
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        confirmButton = {
            if (canOpenInBrowser) {
                Button(onClick = onOpenInBrowser) {
                    Text(stringResource(R.string.open_in_browser))
                }
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onCopy) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = stringResource(R.string.copy),
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.copy))
                }
                Spacer(Modifier.size(8.dp))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.close))
                }
            }
        },
    )
}

private fun canOpenInBrowser(url: String): Boolean {
    val scheme = url.toUri().scheme?.lowercase() ?: return false
    return scheme == "http" || scheme == "https"
}

private fun openLinkInBrowser(context: Context, url: String): Boolean {
    if (!canOpenInBrowser(url)) return false

    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        .addCategory(Intent.CATEGORY_BROWSABLE)
    return runCatching {
        context.startActivity(intent)
        true
    }.getOrDefault(false)
}
