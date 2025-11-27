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

package com.stefansundin.sshremote.data.host

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stefansundin.sshremote.HapticFeedback
import com.stefansundin.sshremote.Result
import com.stefansundin.sshremote.SshRepository
import com.stefansundin.sshremote.data.CryptoManager
import com.stefansundin.sshremote.data.decryptString
import com.stefansundin.sshremote.data.identity.IdentityRepository
import com.stefansundin.sshremote.data.settings.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
import kotlin.math.abs
import kotlin.time.measureTimedValue

class HostViewModel(
    private val repository: HostRepository,
    private val identityRepository: IdentityRepository,
    private val sshRepository: SshRepository,
    private val cryptoManager: CryptoManager,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemoteUiState())
    val uiState = _uiState.asStateFlow()

    private var hostStateJob: Job? = null

    private var lastDeletedHost: Host? = null

    private var cloneHost: Host? = null

    private var mouseMoveJob: Job? = null
    private var pendingDx = 0f
    private var pendingDy = 0f
    private var activeMouseMoveTemplate: String? = null
    private var mousePanJob: Job? = null
    private var pendingPanDx = 0f
    private var pendingPanDy = 0f

    init {
        viewModelScope.launch {
            settingsRepository.hapticFeedback.collectLatest { hapticFeedback ->
                _uiState.update { it.copy(hapticFeedback = hapticFeedback) }
            }
        }
    }

    val allHosts: StateFlow<List<Host>> = repository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    suspend fun upsert(host: Host) {
        repository.upsert(host)
    }

    fun delete(host: Host) = viewModelScope.launch {
        lastDeletedHost = host
        repository.delete(host)
    }

    fun undoDelete() = viewModelScope.launch {
        lastDeletedHost?.let { repository.upsert(it) }
    }

    fun setCloneHost(host: Host) {
        cloneHost = host
    }

    fun getCloneHost(): Host? {
        val host = cloneHost
        cloneHost = null
        return host
    }

    fun updateActiveHostInUiState(host: Host) {
        _uiState.update {
            it.copy(host = host)
        }
    }

    fun setActiveHost(host: Host) {
        hostStateJob?.cancel()
        hostStateJob = viewModelScope.launch {
            repository.get(host.id).filterNotNull().collectLatest { updatedHost ->
                _uiState.update {
                    it.copy(host = updatedHost)
                }
            }
        }
    }

    fun connect(host: Host) {
        setActiveHost(host)
        viewModelScope.launch {
            handleConnection(host)
        }
    }

    private suspend fun handleConnection(host: Host) {
        _uiState.update {
            it.copy(
                host = host,
                connectionStatus = ConnectionStatus.CONNECTING,
                error = null,
            )
        }
        try {
            val identities = coroutineScope {
                host.identityIds?.map { id ->
                    async { identityRepository.get(id).first() }
                }?.awaitAll()?.filterNotNull() ?: identityRepository.getAll().first()
            }

            val privateKeys = identities.map { key ->
                Pair(key.name, cryptoManager.decrypt(key.encryptedPrivateKey).toString(Charsets.UTF_8))
            }

            val password = if (host.encryptedPassword != null) {
                decryptString(host.encryptedPassword, cryptoManager)
            } else null

            val connectionDetails = HostConnectionDetails(
                hostname = host.hostname,
                port = host.port,
                user = host.user,
                password = password,
                privateKeys = privateKeys,
                knownHosts = host.knownHosts,
            )

            val newKnownHosts = sshRepository.connect(connectionDetails)
            if (newKnownHosts != connectionDetails.knownHosts) {
                val updatedHost = host.copy(knownHosts = newKnownHosts)
                repository.upsert(updatedHost)
            }
            _uiState.update { it.copy(connectionStatus = ConnectionStatus.CONNECTED) }

        } catch (e: Exception) {
            Log.e("HostViewModel", "Error connecting to host", e)
            _uiState.update {
                it.copy(
                    connectionStatus = ConnectionStatus.DISCONNECTED,
                    error = e.message,
                )
            }
        }
    }

    fun runCommand(command: String, showOutput: Boolean, isRetry: Boolean = false, reuseShell: Boolean = true) {
        if (_uiState.value.connectionStatus != ConnectionStatus.CONNECTED) {
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, commandOutput = null, error = null) }

            val (result, duration) = measureTimedValue {
                if (reuseShell) {
                    sshRepository.executeCommandReuseShell(command)
                } else {
                    sshRepository.executeCommand(command)
                }
            }
            Log.d("HostViewModel", "executeCommand for '${command}' took $duration")

            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            commandOutput = if (showOutput) result.output else null,
                            isLoading = false,
                        )
                    }
                }

                is Result.Error -> {
                    if (result.isConnectionError && !isRetry) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Connection lost. Reconnecting...",
                            )
                        }
                        val host = _uiState.value.host
                        if (host != null) {
                            handleConnection(host)
                            if (_uiState.value.connectionStatus == ConnectionStatus.CONNECTED) {
                                runCommand(command, showOutput, isRetry = true)
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    connectionStatus = ConnectionStatus.DISCONNECTED,
                                    error = "Cannot reconnect, no active host.",
                                    isLoading = false,
                                )
                            }
                        }
                    } else {
                        val errorMessage = if (result.isConnectionError) {
                            "Failed to reconnect."
                        } else {
                            result.message
                        }
                        _uiState.update {
                            it.copy(
                                commandOutput = if (!result.isConnectionError) errorMessage else null,
                                error = if (result.isConnectionError) errorMessage else null,
                                connectionStatus = if (result.isConnectionError) ConnectionStatus.DISCONNECTED else it.connectionStatus,
                                isLoading = false,
                            )
                        }
                    }
                }
            }
        }
    }

    fun onMouseMove(dx: Float, dy: Float, template: String) {
        if (_uiState.value.connectionStatus != ConnectionStatus.CONNECTED) {
            return
        }
        pendingDx += dx
        pendingDy += dy
        activeMouseMoveTemplate = template

        if (mouseMoveJob?.isActive != true) {
            mouseMoveJob = viewModelScope.launch {
                while (pendingDx.toInt() != 0 || pendingDy.toInt() != 0) {
                    val cx = pendingDx.toInt()
                    val cy = pendingDy.toInt()
                    pendingDx -= cx
                    pendingDy -= cy

                    val cmdStr = activeMouseMoveTemplate!!
                        .replace("%dx", cx.toString())
                        .replace("%dy", cy.toString())

                    val (result, duration) = measureTimedValue {
                        sshRepository.executeCommandReuseShell(cmdStr)
                    }
                    Log.d("HostViewModel", "executeCommand for '${cmdStr}' took $duration")
                }
            }
        }
    }

    fun onMousePan(dx: Float, dy: Float) {
        if (_uiState.value.connectionStatus != ConnectionStatus.CONNECTED) {
            return
        }
        pendingPanDx += dx
        pendingPanDy += dy

        if (mousePanJob?.isActive != true) {
            mousePanJob = viewModelScope.launch {
                while (pendingPanDx.toInt() != 0 || pendingPanDy.toInt() != 0) {
                    val panDx = pendingPanDx
                    val panDy = pendingPanDy
                    pendingPanDx = 0f
                    pendingPanDy = 0f

                    val key = if (abs(panDx) > abs(panDy)) {
                        if (panDx > 0) RemoteControlKey.MOUSE_PAN_RIGHT else RemoteControlKey.MOUSE_PAN_LEFT
                    } else {
                        if (panDy > 0) RemoteControlKey.MOUSE_PAN_DOWN else RemoteControlKey.MOUSE_PAN_UP
                    }

                    _uiState.value.host?.remoteCommands?.get(key)?.let { command ->
                        val (result, duration) = measureTimedValue {
                            sshRepository.executeCommandReuseShell(command.command)
                        }
                        Log.d("HostViewModel", "executeCommand for '${command.command}' took $duration")
                    }
                }
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            sshRepository.disconnect()
            hostStateJob?.cancel()
            mouseMoveJob?.cancel()
            mousePanJob?.cancel()

            pendingDx = 0f
            pendingDy = 0f
            activeMouseMoveTemplate = null
            pendingPanDx = 0f
            pendingPanDy = 0f

            _uiState.update {
                RemoteUiState(hapticFeedback = it.hapticFeedback)
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

data class RemoteUiState(
    val host: Host? = null,
    val commandOutput: String? = null,
    val isLoading: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val error: String? = null,
    val hapticFeedback: HapticFeedback = HapticFeedback.Medium,
)

class HostViewModelFactory(
    private val repository: HostRepository,
    private val identityRepository: IdentityRepository,
    private val sshRepository: SshRepository,
    private val cryptoManager: CryptoManager,
    private val settingsRepository: SettingsRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HostViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HostViewModel(
                repository,
                identityRepository,
                sshRepository,
                cryptoManager,
                settingsRepository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
