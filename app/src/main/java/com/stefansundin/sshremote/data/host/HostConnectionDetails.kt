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

import com.stefansundin.sshremote.data.CryptoManager
import com.stefansundin.sshremote.data.identity.IdentityRepository
import com.stefansundin.sshremote.data.knownhost.KnownHostRepository
import com.stefansundin.sshremote.data.password.PasswordDao
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

/**
 * A simple data holder for the information needed to establish an SSH connection.
 */
data class HostConnectionDetails(
    val hostname: String,
    val port: Int,
    val user: String,
    val password: String?,
    val identities: List<Identity>?,
    val knownHosts: List<String>,
    val sshConfig: String,
) {
    data class Identity(
        val name: String,
        val privateKey: String,
        val certificate: String?,
    )
}

suspend fun Host.toConnectionDetails(
    identityRepository: IdentityRepository,
    knownHostRepository: KnownHostRepository,
    passwordDao: PasswordDao,
    cryptoManager: CryptoManager,
): HostConnectionDetails {
    val identities = coroutineScope {
        identityIds?.map { id ->
            async { identityRepository.get(id).first() }
        }?.awaitAll()?.filterNotNull() ?: identityRepository.getAll().first()
    }.map { identity ->
        HostConnectionDetails.Identity(
            name = identity.name,
            privateKey = cryptoManager.decryptToString(identity.encryptedPrivateKey),
            certificate = identity.encryptedCertificate?.let { cryptoManager.decryptToString(it) },
        )
    }
    val password = passwordId?.let { id ->
        passwordDao.getPassword(id)?.let { password ->
            cryptoManager.decryptToString(password.encryptedPassword)
        }
    }
    val knownHosts = knownHosts + knownHostRepository.getAll().first().map { it.line }
    return HostConnectionDetails(
        hostname = hostname,
        port = port,
        user = user,
        password = password,
        identities = identities,
        knownHosts = knownHosts,
        sshConfig = sshConfig ?: Host.DEFAULT_SSH_CONFIG,
    )
}
