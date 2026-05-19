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

package com.stefansundin.sshremote

import com.stefansundin.sshremote.data.host.ConnectionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ActiveConnectionState(
    val hostId: String? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val isEditingRemoteControl: Boolean = false,
)

class ActiveConnectionTracker {
    private val _state = MutableStateFlow(ActiveConnectionState())
    val state: StateFlow<ActiveConnectionState> = _state.asStateFlow()

    fun update(
        hostId: String?,
        connectionStatus: ConnectionStatus,
        isEditingRemoteControl: Boolean = false,
    ) {
        _state.value = ActiveConnectionState(
            hostId = hostId,
            connectionStatus = connectionStatus,
            isEditingRemoteControl = isEditingRemoteControl,
        )
    }

    fun reset() {
        _state.value = ActiveConnectionState()
    }
}
