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

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme

@Composable
fun TextWithInlineIcon(
    text: String,
    placeholder: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    iconContentDescription: String? = null,
) {
    val parts = text.split(placeholder)
    if (parts.size <= 1) {
        Text(text = text, style = style, modifier = modifier)
        return
    }
    val annotatedString = buildAnnotatedString {
        parts.forEachIndexed { index, part ->
            append(part)
            if (index < parts.size - 1) {
                appendInlineContent("icon", placeholder)
            }
        }
    }
    val inlineContent = mapOf(
        "icon" to InlineTextContent(
            Placeholder(
                width = style.fontSize,
                height = style.fontSize,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
            ),
        ) {
            Icon(icon, contentDescription = iconContentDescription, modifier = Modifier)
        },
    )
    Text(
        text = annotatedString,
        inlineContent = inlineContent,
        style = style,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun TextWithInlineIconPreview() {
    SSHRemoteTheme {
        TextWithInlineIcon(
            "Tap the ? button for help.",
            "?",
            Icons.AutoMirrored.Filled.Help,
            iconContentDescription = "Help",
        )
    }
}
