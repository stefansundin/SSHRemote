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

package com.stefansundin.sshremote.data.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stefansundin.sshremote.Theme
import com.stefansundin.sshremote.data.adhoccommand.AdHocCommandRepository
import com.stefansundin.sshremote.data.host.HostRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val hostRepository: HostRepository,
    private val adHocCommandRepository: AdHocCommandRepository,
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<SettingsEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val theme: StateFlow<Theme> = settingsRepository.theme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Theme.SYSTEM,
        )

    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
    }

    val strictHostKeyChecking: StateFlow<Boolean> = settingsRepository.strictHostKeyChecking
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true,
        )

    fun setStrictHostKeyChecking(strictHostKeyChecking: Boolean) {
        viewModelScope.launch {
            settingsRepository.setStrictHostKeyChecking(strictHostKeyChecking)
        }
    }

    fun exportSettings(context: Context, uri: Uri) {
        viewModelScope.launch {
            SettingsExporter(context, settingsRepository, hostRepository, adHocCommandRepository).export(uri)
        }
    }

    fun importSettings(context: Context, uri: Uri, merge: Boolean) {
        viewModelScope.launch {
            try {
                val count =
                    SettingsImporter(context, settingsRepository, hostRepository, adHocCommandRepository)
                        .import(uri, merge)
                _eventFlow.emit(SettingsEvent.ImportSuccess(count))
            } catch (e: ImportException) {
                _eventFlow.emit(SettingsEvent.ImportError(e.message ?: "Unknown error"))
            }
        }
    }
}

sealed class SettingsEvent {
    data class ImportSuccess(val count: Int) : SettingsEvent()
    data class ImportError(val message: String) : SettingsEvent()
}

class SettingsViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val hostRepository: HostRepository,
    private val adHocCommandRepository: AdHocCommandRepository,
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsRepository, hostRepository, adHocCommandRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
