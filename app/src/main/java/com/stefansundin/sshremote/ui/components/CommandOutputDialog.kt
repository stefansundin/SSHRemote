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
import android.content.res.Configuration
import android.view.SoundEffectConstants
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.boswelja.markdown.material3.MarkdownDocument
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.launch

@Composable
fun CommandOutputDialog(
    output: String,
    renderMarkdown: Boolean = false,
    onDismiss: () -> Unit,
) {
    val clipboard = LocalClipboard.current
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val view = LocalView.current
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()
    val maxDialogHeight = with(density) { windowInfo.containerSize.height.toDp() * 0.9f }
    val trimmedOutput = remember(output) { output.trimEnd() }
    val formattedOutput = remember(trimmedOutput) { expandTabsForDisplay(trimmedOutput) }
    val markdownOutput = remember(trimmedOutput) { sanitizeMarkdownForRenderer(trimmedOutput) }

    AlertDialog(
        modifier = Modifier
            .widthIn(max = 560.dp)
            .heightIn(max = maxDialogHeight),
        title = { Text(stringResource(R.string.command_output)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (renderMarkdown) {
                    MarkdownDocument(
                        markdownOutput,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    PlainTextOutput(formattedOutput)
                }
            }
        },
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        confirmButton = {
            Button(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.close))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    val clipData = ClipData.newPlainText(resources.getString(R.string.command_output), output)
                    scope.launch { clipboard.setClipEntry(clipData.toClipEntry()) }
                },
            ) {
                Icon(
                    Icons.Outlined.ContentCopy,
                    contentDescription = stringResource(R.string.copy),
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.copy))
            }
        },
    )
}

private fun sanitizeMarkdownForRenderer(text: String): String {
    // The Markdown renderer currently crashes on checkboxes
    val withoutTaskListCheckboxes = Regex("""^(\s*(?:[-*+]|\d+\.)\s+)\[([ xX])]\s+""", RegexOption.MULTILINE)
        .replace(text) { match ->
            val prefix = match.groupValues[1]
            val marker = if (match.groupValues[2].equals("x", ignoreCase = true)) "☑ " else "☐ "
            prefix + marker
        }

    // It also crashes on links
    return Regex("""\[(.+?)]\(([^)\n]*)\)""")
        .replace(withoutTaskListCheckboxes) { match ->
            val label = match.groupValues[1]
            val destination = match.groupValues[2].trim()
            if (destination.isEmpty()) label else "$label ($destination)"
        }
        .replace("[", "\\[")
        .replace("]", "\\]")
}

@Composable
private fun PlainTextOutput(output: String) {
    SelectionContainer {
        Text(
            text = output,
            style = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
        )
    }
}

private fun expandTabsForDisplay(text: String, tabWidth: Int = 8): String {
    if (!text.contains('\t')) return text

    val expanded = StringBuilder(text.length)
    var column = 0
    for (char in text) {
        when (char) {
            '\t' -> {
                val spaces = tabWidth - (column % tabWidth)
                repeat(spaces) { expanded.append(' ') }
                column += spaces
            }

            '\n' -> {
                expanded.append(char)
                column = 0
            }

            '\r' -> {
                expanded.append(char)
                column = 0
            }

            else -> {
                expanded.append(char)
                column += 1
            }
        }
    }
    return expanded.toString()
}

@Suppress("SpellCheckingInspection")
@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 600,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun CommandOutputDialogPreview() {
    SSHRemoteTheme {
        Surface {
            CommandOutputDialog(
                output = "Linux pi 6.12.34+rpt-rpi-2712 #1 SMP PREEMPT Debian 1:6.12.34-1+rpt1~bookworm (2025-06-26) aarch64 GNU/Linux\n",
                renderMarkdown = false,
                onDismiss = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 600,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun CommandOutputDialogPreview_Markdown() {
    SSHRemoteTheme {
        Surface {
            CommandOutputDialog(
                output = """
                    # Heading
                    
                    - Item 1
                    - Item 2
                    
                    1. Item 3
                    2. Item 4
                    
                    **BOLD**
                    
                    _Emphasis_
                    
                    - [ ] Checkbox
                    - [x] Checked checkbox
                """.trimIndent(),
                renderMarkdown = true,
                onDismiss = {},
            )
        }
    }
}
