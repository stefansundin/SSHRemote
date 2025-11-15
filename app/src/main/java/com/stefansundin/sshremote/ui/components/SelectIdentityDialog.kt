package com.stefansundin.sshremote.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.data.identity.Identity

@Composable
fun SelectIdentityDialog(
    identities: List<Identity>,
    onIdentitySelected: (Identity) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Identity to Copy") },
        text = {
            LazyColumn {
                items(identities) { identity ->
                    Text(
                        text = identity.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onIdentitySelected(identity) }
                            .padding(vertical = 16.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
