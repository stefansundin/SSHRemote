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

package com.stefansundin.sshremote.data.knownhost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface IKnownHostViewModel {
    val knownHosts: StateFlow<List<KnownHost>>
    fun addKnownHost(line: String)
    fun addKnownHosts(lines: List<String>)
    fun deleteKnownHost(knownHost: KnownHost)
    fun undoDeleteKnownHost()
    fun clearKnownHosts()
}

class KnownHostViewModel(private val knownHostRepository: KnownHostRepository) : ViewModel(),
    IKnownHostViewModel {
    private var lastDeletedKnownHost: KnownHost? = null

    override val knownHosts: StateFlow<List<KnownHost>> =
        knownHostRepository.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    override fun addKnownHost(line: String) {
        viewModelScope.launch {
            knownHostRepository.insert(KnownHost(line = line))
        }
    }

    override fun addKnownHosts(lines: List<String>) {
        if (lines.isEmpty()) {
            return
        }

        viewModelScope.launch {
            lines.forEach { line ->
                knownHostRepository.insert(KnownHost(line = line))
            }
        }
    }

    override fun deleteKnownHost(knownHost: KnownHost) {
        viewModelScope.launch {
            lastDeletedKnownHost = knownHost
            knownHostRepository.delete(knownHost)
        }
    }

    override fun undoDeleteKnownHost() {
        viewModelScope.launch {
            lastDeletedKnownHost?.let { knownHostRepository.insert(it) }
        }
    }

    override fun clearKnownHosts() {
        viewModelScope.launch {
            knownHostRepository.deleteAll()
        }
    }
}
