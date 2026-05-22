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
import com.stefansundin.sshremote.data.knownhost.KnownHostRepository
import com.stefansundin.sshremote.data.password.Password
import com.stefansundin.sshremote.data.password.PasswordDao
import com.stefansundin.sshremote.data.settings.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs
import kotlin.time.measureTimedValue

interface IEditHostViewModel {
    suspend fun isPasswordLost(passwordId: String): Boolean
}

interface IRemoteControlHostViewModel {
    fun connect(host: Host)
    fun runRemoteControlCommand(key: RemoteControlKey)
    fun clearCommandOutput()
    suspend fun runCommand(
        command: String,
        showOutput: Boolean,
        renderOutputAsMarkdown: Boolean = false,
        isRetry: Boolean = false,
//        reuseShell: Boolean = true,
    ): Result

    fun setVolume(percent: Int)
}

class HostViewModel(
    private val repository: HostRepository,
    private val identityRepository: IdentityRepository,
    private val knownHostRepository: KnownHostRepository,
    private val sshRepository: SshRepository,
    private val cryptoManager: CryptoManager,
    private val settingsRepository: SettingsRepository,
    private val passwordDao: PasswordDao,
) : ViewModel(), IEditHostViewModel, IRemoteControlHostViewModel {

    private val _uiState = MutableStateFlow(RemoteUiState())
    val uiState = _uiState.asStateFlow()

    val allHosts: StateFlow<List<Host>?> = repository.getAll()
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    val activeHost: StateFlow<Host?> = combine(
        allHosts,
        _uiState.map { it.hostId }.distinctUntilChanged(),
    ) { hosts, hostId ->
        hosts?.find { it.id == hostId }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

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

        viewModelScope.launch {
            activeHost
                .map { it?.smartVolume?.readCurrentVolume }
                .distinctUntilChanged()
                .collectLatest { readCurrentVolume ->
                    if (readCurrentVolume == true) {
                        updateVolume()
                        updateMuted()
                    } else {
                        _uiState.update {
                            it.copy(volume = null, muted = null)
                        }
                    }
                }
        }
    }

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

    override suspend fun isPasswordLost(passwordId: String): Boolean {
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

    override fun connect(host: Host) {
        connectionJob?.cancel()
        connectionJob = viewModelScope.launch {
            handleConnection(host)
        }
    }

    private suspend fun handleConnection(host: Host) {
        _uiState.update {
            RemoteUiState(
                hostId = host.id,
                connectionStatus = ConnectionStatus.CONNECTING,
                hapticFeedback = it.hapticFeedback,
            )
        }
        try {
            val connectionDetails = host.toConnectionDetails(
                identityRepository = identityRepository,
                knownHostRepository = knownHostRepository,
                passwordDao = passwordDao,
                cryptoManager = cryptoManager,
            )

            val hostKeyUsed = sshRepository.connect(connectionDetails)
            persistAcceptedHostKey(repository, host, hostKeyUsed)

            _uiState.update {
                it.copy(connectionStatus = ConnectionStatus.CONNECTED)
            }
            updateVolume()
            updateMuted()

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

    override suspend fun runCommand(
        command: String,
        showOutput: Boolean,
        renderOutputAsMarkdown: Boolean,
        isRetry: Boolean,
//        reuseShell: Boolean,
    ): Result {
        if (_uiState.value.connectionStatus != ConnectionStatus.CONNECTED) {
            _uiState.update {
                it.copy(error = "Not connected")
            }
            return Result.Error("Not connected", isConnectionError = true)
        }
        _uiState.update {
            it.copy(
                isLoading = true,
                commandOutput = null,
                commandOutputIsMarkdown = false,
                error = null,
            )
        }

        val (result, duration) = try {
            measureTimedValue {
//            if (reuseShell) {
//                sshRepository.executeCommandReuseShell(command)
//            } else {
                sshRepository.executeCommand(command)
//            }
            }
        } catch (e: CancellationException) {
            _uiState.update { it.copy(isLoading = false) }
            throw e
        }
        Log.d("HostViewModel", "executeCommand for '${command}' took $duration")

        when (result) {
            is Result.Success -> {
                _uiState.update {
                    it.copy(
                        commandOutput = if (showOutput) result.output else null,
                        commandOutputIsMarkdown = showOutput && renderOutputAsMarkdown,
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
                    val host = activeHost.value
                    if (host != null) {
                        handleConnection(host)
                        if (_uiState.value.connectionStatus == ConnectionStatus.CONNECTED) {
                            return runCommand(command, showOutput, renderOutputAsMarkdown, isRetry = true)
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
                    _uiState.update {
                        if (result.isConnectionError) {
                            it.copy(
                                commandOutput = null,
                                commandOutputIsMarkdown = false,
                                error = "Failed to reconnect.",
                                connectionStatus = ConnectionStatus.DISCONNECTED,
                                isLoading = false,
                            )
                        } else {
                            it.copy(
                                commandOutput = result.message,
                                commandOutputIsMarkdown = false,
                                error = null,
                                isLoading = false,
                            )
                        }
                    }
                }
            }
        }
        return result
    }

    override fun runRemoteControlCommand(key: RemoteControlKey) {
        runRemoteControlCommandWithResult(key)
    }

    fun runRemoteControlCommandWithResult(key: RemoteControlKey, onComplete: ((Result) -> Unit)? = null) {
        val command = activeHost.value?.remoteCommands?.get(key)
        if (command != null) {
            val oldVolume = uiState.value.volume
            viewModelScope.launch {
                val result = runCommand(
                    command = command.command,
                    showOutput = command.showOutput,
                    renderOutputAsMarkdown = command.renderOutputAsMarkdown,
                )
                if (result is Result.Success) {
                    when (key) {
                        RemoteControlKey.VOLUME_UP, RemoteControlKey.VOLUME_DOWN -> {
                            updateVolume()
                            if (uiState.value.volume == "0%" || oldVolume == "0%") {
                                // Maybe unnecessary? Maybe we can assume it is muted if volume is "0%"?
                                updateMuted()
                            }
                        }

                        RemoteControlKey.MUTE -> {
                            updateMuted()
                        }

                        else -> Unit
                    }
                }
                onComplete?.invoke(result)
            }
        } else {
            Log.e("HostViewModel", "No command found for key: $key")
            onComplete?.invoke(Result.Error("No command found for key: $key"))
        }
    }

    override fun setVolume(percent: Int) {
        viewModelScope.launch {
            runCommand(
                command = "pactl set-sink-volume @DEFAULT_SINK@ $percent%",
                showOutput = false,
            )
            updateVolume()
            updateMuted()
        }
    }

    suspend fun readVolume(): String? {
        val result = sshRepository.executeCommandReuseShell("pactl get-sink-volume @DEFAULT_SINK@")
        Log.d("HostViewModel", "readVolume result: $result")
        if (result is Result.Success) {
            // Extracts the first percent value from this output:
            // Volume: front-left: 37345 /  57% / -14.65 dB,   front-right: 37345 /  57% / -14.65 dB
            val volume = Regex("""\d+\s*%""").find(result.output)?.value
            return volume
        }
        return null
    }

    suspend fun readMuted(): Boolean? {
        val result = sshRepository.executeCommandReuseShell("pactl get-sink-mute @DEFAULT_SINK@")
        Log.d("HostViewModel", "readMuted result: $result")
        if (result is Result.Success) {
            // TODO: Is this output localized?
            val muted = (result.output.trim() == "Mute: yes")
            return muted
        }
        return null
    }

    suspend fun updateVolume() {
        val hostToUse = activeHost.value
        if (hostToUse?.smartVolume?.readCurrentVolume == true) {
            val volume = readVolume()
            if (volume != null) {
                _uiState.update { it.copy(volume = volume) }
            }
        }
    }

    suspend fun updateMuted() {
        val hostToUse = activeHost.value
        if (hostToUse?.smartVolume?.readCurrentVolume == true) {
            val muted = readMuted()
            if (muted != null) {
                _uiState.update { it.copy(muted = muted) }
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

                    activeHost.value?.remoteCommands?.get(key)?.let { command ->
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

    override fun clearCommandOutput() {
        _uiState.update { it.copy(commandOutput = null, commandOutputIsMarkdown = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun updateRemoteCommands(hostId: String, remoteCommands: Map<RemoteControlKey, Command>) {
        viewModelScope.launch {
            repository.updateRemoteCommands(hostId, remoteCommands)
        }
    }

    fun updateStartScreen(hostId: String, startScreen: RemoteControlScreen) {
        viewModelScope.launch {
            repository.updateStartScreen(hostId, startScreen)
        }
    }
}

data class RemoteUiState(
    val hostId: String? = null,
    val commandOutput: String? = null,
    val commandOutputIsMarkdown: Boolean = false,
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
    private val knownHostRepository: KnownHostRepository,
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
                knownHostRepository,
                sshRepository,
                cryptoManager,
                settingsRepository,
                passwordDao,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
