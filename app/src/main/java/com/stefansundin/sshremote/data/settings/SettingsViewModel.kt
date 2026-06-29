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

package com.stefansundin.sshremote.data.settings

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stefansundin.sshremote.HapticFeedback
import com.stefansundin.sshremote.Theme
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommandRepository
import com.stefansundin.sshremote.data.host.HostRepository
import com.stefansundin.sshremote.data.knownhost.KnownHostRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface ISettingsViewModel {
    val theme: StateFlow<Theme>
    val useDynamicColors: StateFlow<Boolean>
    val backgroundColor: StateFlow<Color?>
    val primaryColor: StateFlow<Color?>
    val onPrimaryColor: StateFlow<Color?>
    val hapticFeedback: StateFlow<HapticFeedback>
    val keepScreenOn: StateFlow<Boolean>
    val showWhenLocked: StateFlow<Boolean>
    val notificationsEnabled: StateFlow<Boolean>
    val strictHostKeyChecking: StateFlow<Boolean>
    val allowPasswordPrompting: StateFlow<Boolean>
    val shareTargetEnabled: StateFlow<Boolean>
    val hasHosts: StateFlow<Boolean>
    val eventFlow: SharedFlow<SettingsEvent>

    fun setTheme(theme: Theme)
    fun setUseDynamicColors(useDynamicColors: Boolean)
    fun setBackgroundColor(color: Color?)
    fun setPrimaryColor(color: Color?)
    fun setOnPrimaryColor(color: Color?)
    fun setHapticFeedback(hapticFeedback: HapticFeedback)
    fun setKeepScreenOn(keepScreenOn: Boolean)
    fun setShowWhenLocked(showWhenLocked: Boolean)
    fun setNotificationsEnabled(notificationsEnabled: Boolean)
    fun setStrictHostKeyChecking(strictHostKeyChecking: Boolean)
    fun setAllowPasswordPrompting(allowPasswordPrompting: Boolean)
    fun setShareTargetEnabled(shareTargetEnabled: Boolean)
    fun exportSettings(context: Context, uri: Uri)
    suspend fun exportSettingsToString(context: Context): String
    fun importSettings(context: Context, uri: Uri, importStrategy: ImportStrategy)
    fun importSettings(context: Context, json: String, importStrategy: ImportStrategy)
}

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val hostRepository: HostRepository,
    private val knownHostRepository: KnownHostRepository,
    private val adHocCommandRepository: AdHocCommandRepository,
) : ViewModel(), ISettingsViewModel {

    private val _eventFlow = MutableSharedFlow<SettingsEvent>()
    override val eventFlow = _eventFlow.asSharedFlow()

    override val theme: StateFlow<Theme> = settingsRepository.theme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = Theme.SYSTEM,
        )

    override fun setTheme(theme: Theme) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
    }

    override val useDynamicColors: StateFlow<Boolean> = settingsRepository.useDynamicColors
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true,
        )

    override fun setUseDynamicColors(useDynamicColors: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseDynamicColors(useDynamicColors)
        }
    }

    override val backgroundColor: StateFlow<Color?> = settingsRepository.backgroundColor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    override fun setBackgroundColor(color: Color?) {
        viewModelScope.launch {
            settingsRepository.setBackgroundColor(color)
        }
    }

    override val primaryColor: StateFlow<Color?> = settingsRepository.primaryColor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    override fun setPrimaryColor(color: Color?) {
        viewModelScope.launch {
            settingsRepository.setPrimaryColor(color)
        }
    }

    override val onPrimaryColor: StateFlow<Color?> = settingsRepository.onPrimaryColor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    override fun setOnPrimaryColor(color: Color?) {
        viewModelScope.launch {
            settingsRepository.setOnPrimaryColor(color)
        }
    }

    override val hapticFeedback: StateFlow<HapticFeedback> = settingsRepository.hapticFeedback
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = HapticFeedback.Medium,
        )

    override fun setHapticFeedback(hapticFeedback: HapticFeedback) {
        viewModelScope.launch {
            settingsRepository.setHapticFeedback(hapticFeedback)
        }
    }

    override val keepScreenOn: StateFlow<Boolean> = settingsRepository.keepScreenOn
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true,
        )

    override fun setKeepScreenOn(keepScreenOn: Boolean) {
        viewModelScope.launch {
            settingsRepository.setKeepScreenOn(keepScreenOn)
        }
    }

    override val showWhenLocked: StateFlow<Boolean> = settingsRepository.showWhenLocked
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false,
        )

    override fun setShowWhenLocked(showWhenLocked: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowWhenLocked(showWhenLocked)
        }
    }

    override val notificationsEnabled: StateFlow<Boolean> = settingsRepository.notificationsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false,
        )

    override fun setNotificationsEnabled(notificationsEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(notificationsEnabled)
        }
    }

    override val strictHostKeyChecking: StateFlow<Boolean> = settingsRepository.strictHostKeyChecking
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true,
        )

    override fun setStrictHostKeyChecking(strictHostKeyChecking: Boolean) {
        viewModelScope.launch {
            settingsRepository.setStrictHostKeyChecking(strictHostKeyChecking)
        }
    }

    override val allowPasswordPrompting: StateFlow<Boolean> = settingsRepository.allowPasswordPrompting
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true,
        )

    override fun setAllowPasswordPrompting(allowPasswordPrompting: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAllowPasswordPrompting(allowPasswordPrompting)
        }
    }

    override val shareTargetEnabled: StateFlow<Boolean> = settingsRepository.shareTargetEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false,
        )

    override fun setShareTargetEnabled(shareTargetEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShareTargetEnabled(shareTargetEnabled)
        }
    }

    override val hasHosts: StateFlow<Boolean> = hostRepository.getAll().map { it.isNotEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false,
        )

    override fun exportSettings(context: Context, uri: Uri) {
        viewModelScope.launch {
            SettingsExporter(
                context,
                settingsRepository,
                hostRepository,
                knownHostRepository,
                adHocCommandRepository,
            ).export(uri)
        }
    }

    override suspend fun exportSettingsToString(context: Context): String {
        return SettingsExporter(
            context,
            settingsRepository,
            hostRepository,
            knownHostRepository,
            adHocCommandRepository,
        ).exportToString()
    }

    override fun importSettings(context: Context, uri: Uri, importStrategy: ImportStrategy) {
        viewModelScope.launch {
            try {
                val (count, requestNotificationPermission, theme) =
                    SettingsImporter(
                        context,
                        settingsRepository,
                        hostRepository,
                        knownHostRepository,
                        adHocCommandRepository,
                    )
                        .import(uri, importStrategy)
                if (theme != null) {
                    setTheme(theme)
                }
                _eventFlow.emit(SettingsEvent.ImportSuccess(count))
                if (requestNotificationPermission) {
                    _eventFlow.emit(SettingsEvent.RequestPostNotificationsPermission)
                }
            } catch (e: ImportException) {
                _eventFlow.emit(SettingsEvent.ImportError(e.message ?: "Unknown error"))
            }
        }
    }

    override fun importSettings(context: Context, json: String, importStrategy: ImportStrategy) {
        viewModelScope.launch {
            try {
                val (count, requestNotificationPermission, theme) =
                    SettingsImporter(
                        context,
                        settingsRepository,
                        hostRepository,
                        knownHostRepository,
                        adHocCommandRepository,
                    )
                        .import(json, importStrategy)
                if (theme != null) {
                    setTheme(theme)
                }
                _eventFlow.emit(SettingsEvent.ImportSuccess(count))
                if (requestNotificationPermission) {
                    _eventFlow.emit(SettingsEvent.RequestPostNotificationsPermission)
                }
            } catch (e: ImportException) {
                _eventFlow.emit(SettingsEvent.ImportError(e.message ?: "Unknown error"))
            }
        }
    }
}

sealed class SettingsEvent {
    data class ImportSuccess(val count: Int) : SettingsEvent()
    data class ImportError(val message: String) : SettingsEvent()
    object RequestPostNotificationsPermission : SettingsEvent()
}

class SettingsViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val hostRepository: HostRepository,
    private val knownHostRepository: KnownHostRepository,
    private val adHocCommandRepository: AdHocCommandRepository,
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(
                settingsRepository,
                hostRepository,
                knownHostRepository,
                adHocCommandRepository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
