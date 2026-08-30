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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stefansundin.sshremote.R
import com.stefansundin.sshremote.Result
import com.stefansundin.sshremote.data.host.Command
import com.stefansundin.sshremote.data.host.ConnectionStatus
import com.stefansundin.sshremote.data.host.Host
import com.stefansundin.sshremote.data.host.IRemoteControlHostViewModel
import com.stefansundin.sshremote.data.host.RemoteControlKey
import com.stefansundin.sshremote.ui.theme.AppDimens
import com.stefansundin.sshremote.ui.theme.SSHRemoteTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandList(
    commands: List<Command>,
    hostViewModel: IRemoteControlHostViewModel,
    modifier: Modifier = Modifier,
    connectionStatus: ConnectionStatus? = null,
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val verticalScrollState = rememberScrollState()

    if (commands.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.no_commands_added),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.edit_remote_to_add_commands),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp),
            )
        }
    } else {
        ScrollbarContainer(
            modifier = modifier,
            verticalScrollState = verticalScrollState,
        ) { contentModifier ->
            Column(
                modifier = contentModifier
                    .padding(16.dp)
                    .verticalScroll(verticalScrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                commands.forEach { command ->
                    TooltipBox(
                        state = rememberTooltipState(),
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                            TooltipAnchorPosition.Below,
                            AppDimens.tooltipAnchorOffset,
                        ),
                        tooltip = {
                            PlainTooltip {
                                Text(command.command ?: stringResource(R.string.error))
                            }
                        },
                    ) {
                        Button(
                            onClick = {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                val commandText = command.command ?: return@Button
                                scope.launch {
                                    hostViewModel.runCommand(
                                        command = commandText,
                                        showOutput = command.showOutput,
                                        renderOutputAsMarkdown = command.renderOutputAsMarkdown,
                                        reuseShell = false,
                                    )
                                }
                            },
                            enabled = connectionStatus == ConnectionStatus.CONNECTED,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(command.displayText())
                        }
                    }
                }
            }
        }
    }
}

private val fakeRemoteControlHostViewModel = object : IRemoteControlHostViewModel {
    override fun connect(host: Host) {}
    override fun runRemoteControlCommand(key: RemoteControlKey) {}
    override fun clearCommandOutput() {}
    override suspend fun runCommand(
        command: String,
        showOutput: Boolean,
        renderOutputAsMarkdown: Boolean,
        isRetry: Boolean,
        reuseShell: Boolean,
    ): Result {
        return Result.Success("")
    }

    override fun setVolume(percent: Int) {}
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun CommandListPreview() {
    SSHRemoteTheme {
        Surface {
            CommandList(
                commands = listOf(
                    Command("uptime", name = "Uptime"),
                    Command("whoami"),
                ),
                hostViewModel = fakeRemoteControlHostViewModel,
                connectionStatus = ConnectionStatus.CONNECTED,
            )
        }
    }
}

@Preview(showBackground = true, name = "Empty")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2.0f)
@Composable
private fun CommandListPreview_Empty() {
    SSHRemoteTheme {
        Surface {
            CommandList(
                commands = emptyList(),
                hostViewModel = fakeRemoteControlHostViewModel,
            )
        }
    }
}
