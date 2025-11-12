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

package com.stefansundin.sshremote.data.sshkey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.stefansundin.sshremote.data.CryptoManager

class SshKeyViewModelFactory(
    private val sshKeyRepository: SshKeyRepository,
    private val cryptoManager: CryptoManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SshKeyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SshKeyViewModel(sshKeyRepository, cryptoManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
