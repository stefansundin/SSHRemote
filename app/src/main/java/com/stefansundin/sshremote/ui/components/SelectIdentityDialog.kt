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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        title = { Text("Select public key") },
        text = {
            if (identities.isEmpty()) {
                Text("No keys found. Create an SSH key in the app settings.")
            } else {
                Column {
                    Text(
                        text = "Key will be added to host's ~/.ssh/authorized_keys file.",
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
                Text("Cancel")
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
