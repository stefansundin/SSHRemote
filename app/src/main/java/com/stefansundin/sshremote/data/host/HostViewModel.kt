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

package com.stefansundin.sshremote.data.host

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stefansundin.sshremote.HapticFeedback
import com.stefansundin.sshremote.Result
import com.stefansundin.sshremote.SshRepository
import com.stefansundin.sshremote.data.CryptoManager
import com.stefansundin.sshremote.data.identity.IdentityRepository
import com.stefansundin.sshremote.data.password.Password
import com.stefansundin.sshremote.data.password.PasswordDao
import com.stefansundin.sshremote.data.settings.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs
import kotlin.time.measureTimedValue

class HostViewModel(
    private val repository: HostRepository,
    private val identityRepository: IdentityRepository,
    private val sshRepository: SshRepository,
    private val cryptoManager: CryptoManager,
    private val settingsRepository: SettingsRepository,
    private val passwordDao: PasswordDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemoteUiState())
    val uiState = _uiState.asStateFlow()

    private var connectionJob: Job? = null
    private var lastDeletedHost: Host? = null
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

    val allHosts: StateFlow<List<Host>?> = repository.getAll()
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    suspend fun saveHost(host: Host, password: String?) {
        var hostToSave = host
        if (password != null) {
            // Delete the old password if it exists
            host.passwordId?.let { oldPasswordId ->
                passwordDao.delete(oldPasswordId)
            }

            if (password.isEmpty()) {
                // Clear the password
                hostToSave = host.copy(passwordId = null)
            } else {
                // Update with new password
                val encryptedPassword = cryptoManager.encrypt(password)
                val passwordEntity = Password(encryptedPassword = encryptedPassword)
                passwordDao.insert(passwordEntity)
                hostToSave = host.copy(passwordId = passwordEntity.id)
            }
        }

        // If password is null, no changes are made to the passwordId and the host is saved as is
        repository.upsert(hostToSave)
    }

    fun cloneHost(host: Host, onHostCloned: (String) -> Unit) {
        viewModelScope.launch {
            val newPasswordId = host.passwordId?.let { passwordId ->
                passwordDao.getPassword(passwordId)?.let { password ->
                    val newPassword = Password(encryptedPassword = password.encryptedPassword)
                    passwordDao.insert(newPassword)
                    newPassword.id
                }
            }
            val clonedHost = host.copy(
                id = UUID.randomUUID().toString(),
                name = "Copy of ${host.name}",
                passwordId = newPasswordId,
            )
            repository.insert(clonedHost)
            onHostCloned(clonedHost.id)
        }
    }

    suspend fun isPasswordLost(passwordId: String): Boolean {
        return passwordDao.getPassword(passwordId) == null
    }

    suspend fun upsert(host: Host) {
        repository.upsert(host)
    }

    fun delete(host: Host) = viewModelScope.launch {
        lastDeletedHost = host
        host.passwordId?.let { passwordDao.delete(it) }
        repository.delete(host)
    }

    fun undoDelete() = viewModelScope.launch {
        lastDeletedHost?.let { repository.upsert(it) }
    }

    fun updateActiveHostInUiState(host: Host) {
        _uiState.update {
            it.copy(host = host)
        }
    }

    fun connect(host: Host) {
        connectionJob?.cancel()
        connectionJob = viewModelScope.launch {
            handleConnection(host)
        }
    }

    private suspend fun handleConnection(host: Host) {
        _uiState.update {
            RemoteUiState(
                host = host,
                connectionStatus = ConnectionStatus.CONNECTING,
                hapticFeedback = it.hapticFeedback,
            )
        }
        try {
            val identities = coroutineScope {
                host.identityIds?.map { id ->
                    async { identityRepository.get(id).first() }
                }?.awaitAll()?.filterNotNull() ?: identityRepository.getAll().first()
            }

            val privateKeys = identities.map { key ->
                Pair(key.name, cryptoManager.decryptToString(key.encryptedPrivateKey))
            }

            val password = host.passwordId?.let {
                passwordDao.getPassword(it)?.let { password ->
                    cryptoManager.decryptToString(password.encryptedPassword)
                }
            }

            val connectionDetails = HostConnectionDetails(
                hostname = host.hostname,
                port = host.port,
                user = host.user,
                password = password,
                privateKeys = privateKeys,
                knownHosts = host.knownHosts,
                sshConfig = host.sshConfig ?: Host.DEFAULT_SSH_CONFIG,
            )

            val newKnownHosts = sshRepository.connect(connectionDetails)
            if (newKnownHosts != connectionDetails.knownHosts) {
                val updatedHost = host.copy(knownHosts = newKnownHosts)
                repository.upsert(updatedHost)
            }
            _uiState.update {
                it.copy(connectionStatus = ConnectionStatus.CONNECTED)
            }
            readVolume()
            readMuted()

        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            if (!currentCoroutineContext().isActive) {
                Log.d("HostViewModel", "Connection cancelled, ignoring error: ${e.message}")
                return
            }
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

    fun runRemoteControlCommand(key: RemoteControlKey) {
        val command = uiState.value.host?.remoteCommands?.get(key)
        if (command != null) {
            val oldVolume = uiState.value.volume
            runCommand(command.command, command.showOutput)
            viewModelScope.launch {
                when (key) {
                    RemoteControlKey.VOLUME_UP, RemoteControlKey.VOLUME_DOWN -> {
                        readVolume()
                        if (uiState.value.volume == "0%" || oldVolume == "0%") {
                            // Maybe unnecessary? Maybe we can assume it is muted if volume is "0%"?
                            readMuted()
                        }
                    }

                    RemoteControlKey.MUTE -> {
                        readMuted()
                    }

                    else -> Unit
                }
            }
        }
    }

    suspend fun readVolume(): String? {
        val host = _uiState.value.host
        if (host?.smartVolume?.readCurrentVolume == true) {
            val result = sshRepository.executeCommandReuseShell("pactl get-sink-volume @DEFAULT_SINK@")
            Log.d("HostViewModel", "readVolume result: $result")
            if (result is Result.Success) {
                // Extracts the first percent value from this output:
                // Volume: front-left: 37345 /  57% / -14.65 dB,   front-right: 37345 /  57% / -14.65 dB
                val volume = Regex("""\d+\s*%""").find(result.output)?.value
                _uiState.update { it.copy(volume = volume) }
                return volume
            }
        }
        return null
    }

    suspend fun readMuted(): Boolean? {
        val host = _uiState.value.host
        if (host?.smartVolume?.readCurrentVolume == true) {
            val result = sshRepository.executeCommandReuseShell("pactl get-sink-mute @DEFAULT_SINK@")
            Log.d("HostViewModel", "readMuted result: $result")
            if (result is Result.Success) {
                // TODO: Is this output localized?
                val muted = (result.output.trim() == "Mute: yes")
                _uiState.update { it.copy(muted = muted) }
                return muted
            }
        }
        return null
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
        connectionJob?.cancel()
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

        viewModelScope.launch {
            sshRepository.disconnect()
        }
    }

    fun clearCommandOutput() {
        _uiState.update { it.copy(commandOutput = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun updateRemoteCommands(host: Host, remoteCommands: Map<RemoteControlKey, Command>) {
        viewModelScope.launch {
            val updatedHost = host.copy(remoteCommands = remoteCommands)
            repository.upsert(updatedHost)
        }
    }
}

data class RemoteUiState(
    val host: Host? = null,
    val commandOutput: String? = null,
    val isLoading: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val error: String? = null,
    val hapticFeedback: HapticFeedback = HapticFeedback.Medium,
    val volume: String? = null,
    val muted: Boolean? = null,
)

class HostViewModelFactory(
    private val repository: HostRepository,
    private val identityRepository: IdentityRepository,
    private val sshRepository: SshRepository,
    private val cryptoManager: CryptoManager,
    private val settingsRepository: SettingsRepository,
    private val passwordDao: PasswordDao,
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
                passwordDao,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
