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
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.ui.screens.sampleHosts
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme

@Composable
fun SelectHostDialog(
    hosts: List<Host>,
    onHostSelected: (Host) -> Unit,
    onDismiss: () -> Unit,
) {
    val view = LocalView.current

    AlertDialog(
        title = { Text(stringResource(R.string.select_host)) },
        text = {
            if (hosts.isEmpty()) {
                Text(stringResource(R.string.no_hosts_found))
            } else {
                LazyColumn {
                    items(hosts, key = { it.id }) { host ->
                        ListItem(
                            headlineContent = { Text(host.name) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    onHostSelected(host)
                                },
                        )
                        HorizontalDivider()
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

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 600,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 2.0f,
)
@Composable
private fun SelectHostDialogPreview() {
    SSHRemoteTheme {
        Surface {
            SelectHostDialog(
                hosts = sampleHosts,
                onHostSelected = {},
                onDismiss = {},
            )
        }
    }
}
