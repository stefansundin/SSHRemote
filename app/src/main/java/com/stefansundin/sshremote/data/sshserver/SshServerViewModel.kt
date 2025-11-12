/*
 * SSH Remote
 * Copyright (C) 2025  Stefan Sundin
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

package com.stefansundin.sshremote.data.sshserver

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stefansundin.sshremote.Result
import com.stefansundin.sshremote.SshRepository
import com.stefansundin.sshremote.data.CryptoManager
import com.stefansundin.sshremote.data.decryptString
import com.stefansundin.sshremote.data.sshkey.SshKeyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SshServerViewModel(
    private val repository: SshServerRepository,
    private val sshKeyRepository: SshKeyRepository,
    private val sshRepository: SshRepository,
    private val cryptoManager: CryptoManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SshTerminalUiState())
    val uiState = _uiState.asStateFlow()

    private var currentServer: SshServer? = null
    private var serverStateJob: Job? = null

    val allServers: StateFlow<List<SshServer>> = repository.getAllServers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun upsert(server: SshServer) = viewModelScope.launch {
        repository.upsert(server)
    }

    fun delete(server: SshServer) = viewModelScope.launch {
        repository.delete(server)
    }

    fun setActiveServer(server: SshServer) {
        currentServer = server
        serverStateJob?.cancel()
        serverStateJob = viewModelScope.launch {
            repository.getServerById(server.id).filterNotNull().collectLatest { updatedServer ->
                currentServer = updatedServer
                _uiState.update {
                    it.copy(
                        serverName = updatedServer.name,
                        commands = updatedServer.commands,
                    )
                }
            }
        }
    }

    fun connectToServer(server: SshServer) {
        setActiveServer(server)
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    serverName = server.name,
                    connectionStatus = ConnectionStatus.CONNECTING,
                )
            }
            try {
                val sshKeys = server.sshKeyIds?.map { id ->
                    async { sshKeyRepository.getKeyById(id).filterNotNull().first() }
                }?.awaitAll() ?: sshKeyRepository.getAllKeys().first()

                val privateKeys = sshKeys.map { key ->
                    cryptoManager.decrypt(key.encryptedPrivateKey).toString(Charsets.UTF_8)
                }

                val password = if (server.encryptedPassword != null) {
                    decryptString(server.encryptedPassword, cryptoManager)
                } else null

                val connectionDetails = SshServerConnectionDetails(
                    host = server.host,
                    port = server.port,
                    user = server.user,
                    password = password,
                    privateKeys = privateKeys,
                    knownHosts = server.knownHosts,
                )

                val newKnownHosts = sshRepository.connect(connectionDetails)
                if (newKnownHosts != connectionDetails.knownHosts) {
                    val updatedServer = server.copy(knownHosts = newKnownHosts)
                    repository.upsert(updatedServer)
                }
                _uiState.update { it.copy(connectionStatus = ConnectionStatus.CONNECTED) }

            } catch (e: Exception) {
                Log.e("SshServerViewModel", "Error connecting to server", e)
                _uiState.update {
                    it.copy(
                        connectionStatus = ConnectionStatus.DISCONNECTED,
                        error = e.message,
                    )
                }
            }
        }
    }

    fun runCommand(command: Command) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, commandOutput = null) }

            val result = sshRepository.executeCommand(command.command)
            val output = try {
                when (result) {
                    is Result.Success -> result.output
                    is Result.Error -> result.message
                }
            } catch (e: Exception) {
                "Error executing command: ${e.message}"
            }

            _uiState.update {
                it.copy(
                    commandOutput = if (result is Result.Error || command.showOutput) output else null,
                    isLoading = false,
                )
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            sshRepository.disconnect()
            _uiState.update {
                it.copy(
                    connectionStatus = ConnectionStatus.DISCONNECTED,
                    error = null,
                )
            }
        }
    }

    fun clearCommandOutput() {
        _uiState.update { it.copy(commandOutput = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

enum class ConnectionStatus {
    CONNECTED,
    CONNECTING,
    DISCONNECTED
}

// Data class to hold the UI state for the terminal screen
data class SshTerminalUiState(
    val serverName: String? = null,
    val commandOutput: String? = null,
    val isLoading: Boolean = false,
    val commands: List<Command> = emptyList(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val error: String? = null,
)

class SshServerViewModelFactory(
    private val repository: SshServerRepository,
    private val sshKeyRepository: SshKeyRepository,
    private val sshRepository: SshRepository,
    private val cryptoManager: CryptoManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SshServerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SshServerViewModel(
                repository,
                sshKeyRepository,
                sshRepository,
                cryptoManager,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
