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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.ui.dpadFocusable
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.launch

@Composable
fun PublicKeyDialog(publicKey: String, onDismiss: () -> Unit) {
    val clipboard = LocalClipboard.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    AlertDialog(
        title = { Text(stringResource(R.string.public_key_title)) },
        text = {
            // A scrollable text field is good for long keys
            OutlinedTextField(
                value = publicKey,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .dpadFocusable(),
                visualTransformation = NoWrapOnSpecialCharactersVisualTransformation,
            )
        },
        onDismissRequest = onDismiss,
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
            val label = stringResource(R.string.public_ssh_key_label)
            TextButton(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    val clipData = ClipData.newPlainText(label, publicKey)
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

/**
 * Cool visual transformer that prevents certain characters from causing a line break.
 * Public SSH keys are full of slashes and pluses which can make the dialog look funny.
 */
object NoWrapOnSpecialCharactersVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        if (!original.contains('/') && !original.contains('+')) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val transformed = original.replace("/", "/\u2060").replace("+", "+\u2060")

        return TransformedText(
            AnnotatedString(transformed),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    val limitedOffset = offset.coerceAtMost(original.length)
                    var specialChars = 0
                    for (i in 0 until limitedOffset) {
                        if (original[i] == '/' || original[i] == '+') {
                            specialChars++
                        }
                    }
                    return limitedOffset + specialChars
                }

                override fun transformedToOriginal(offset: Int): Int {
                    val limitedOffset = offset.coerceAtMost(transformed.length)
                    var joiners = 0
                    for (i in 0 until limitedOffset) {
                        if (transformed[i] == '\u2060') {
                            joiners++
                        }
                    }
                    return limitedOffset - joiners
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun PublicKeyDialogPreview() {
    SSHRemoteTheme {
        Surface {
            PublicKeyDialog(
                publicKey = "ssh-ed25519 AAAA... Comment",
                onDismiss = {},
            )
        }
    }
}
