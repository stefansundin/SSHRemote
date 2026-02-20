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

import android.content.res.Configuration
import android.view.SoundEffectConstants
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.data.identity.Identity
import com.stefansundin.sshremote.ui.screens.sampleIdentities
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme

@Composable
fun SelectIdentityDialog(
    identities: List<Identity>,
    onIdentitySelected: (Identity) -> Unit,
    onDismiss: () -> Unit,
) {
    val view = LocalView.current

    AlertDialog(
        title = { Text(stringResource(R.string.select_public_key)) },
        text = {
            if (identities.isEmpty()) {
                Text(stringResource(R.string.no_keys_found_prompt))
            } else {
                Column {
                    Text(
                        text = stringResource(R.string.key_addition_info),
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    LazyColumn {
                        items(identities, key = { it.id }) { identity ->
                            ListItem(
                                headlineContent = { Text(identity.name) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        onIdentitySelected(identity)
                                    },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun SelectIdentityDialogPreview() {
    SSHRemoteTheme {
        Surface {
            SelectIdentityDialog(
                identities = sampleIdentities,
                onIdentitySelected = {},
                onDismiss = {},
            )
        }
    }
}
