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

package com.stefansundin.sshremote.data.adhoccommand

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdHocCommandViewModel(private val adHocCommandRepository: AdHocCommandRepository) : ViewModel() {
    val adHocCommands: StateFlow<List<AdHocCommand>> =
        adHocCommandRepository.getAdHocCommands()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun addAdHocCommand(command: String) {
        viewModelScope.launch {
            adHocCommandRepository.insert(AdHocCommand(command = command))
        }
    }

    fun deleteAdHocCommand(adHocCommand: AdHocCommand) {
        viewModelScope.launch {
            adHocCommandRepository.delete(adHocCommand)
        }
    }

    fun clearAdHocCommands() {
        viewModelScope.launch {
            adHocCommandRepository.clear()
        }
    }
}
