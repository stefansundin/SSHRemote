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

package com.stefansundin.sshremote.data.identity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import com.stefansundin.sshremote.data.CryptoManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.i2p.crypto.eddsa.EdDSASecurityProvider
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator
import java.io.ByteArrayOutputStream
import java.io.StringWriter
import java.security.KeyPairGenerator
import java.security.Security

interface IIdentityListViewModel {
    val identities: StateFlow<List<Identity>?>
    val eventFlow: Flow<IdentityEvent>
    fun showPublicKeyFor(identity: Identity)
    fun exportPublicKeyFor(identity: Identity)
}

interface IRemoteControlIdentityViewModel {
    val identities: StateFlow<List<Identity>?>
    suspend fun getPublicKey(identity: Identity): String
}

class IdentityViewModel(
    private val identityRepository: IdentityRepository,
    private val cryptoManager: CryptoManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel(), IIdentityListViewModel, IRemoteControlIdentityViewModel {

    private val _eventChannel = Channel<IdentityEvent>(Channel.BUFFERED)
    override val eventFlow = _eventChannel.receiveAsFlow()

    private var lastDeletedKey: Identity? = null

    override val identities: StateFlow<List<Identity>?> = identityRepository.getAll()
        .flowOn(ioDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    fun insert(name: String, privateKey: String) {
        viewModelScope.launch {
            try {
                val encryptedPrivateKey = cryptoManager.encrypt(privateKey.toByteArray())
                identityRepository.insert(
                    Identity(
                        name = name,
                        encryptedPrivateKey = encryptedPrivateKey,
                    ),
                )
                _eventChannel.send(IdentityEvent.KeyAdded)
            } catch (_: Exception) {
                _eventChannel.send(IdentityEvent.Error("Failed to save the imported key."))
            }
        }
    }

    suspend fun generateAndInsert(name: String, type: Int, size: Int?, comment: String) {
        withContext(ioDispatcher) {
            try {
                val privateKey = generateKeyPair(type, size, comment)
                val encryptedPrivateKey = cryptoManager.encrypt(privateKey.toByteArray())
                identityRepository.insert(
                    Identity(
                        name = name,
                        encryptedPrivateKey = encryptedPrivateKey,
                    ),
                )
                _eventChannel.send(IdentityEvent.KeyAdded)
            } catch (e: Exception) {
                _eventChannel.send(IdentityEvent.Error("Failed to generate and save key pair: ${e.message}"))
            }
        }
    }

    private suspend fun generateKeyPair(type: Int, size: Int?, comment: String): String =
        withContext(ioDispatcher) {
            if (type == KeyPair.ED25519) {
                generateEd25519KeyPair(comment)
            } else {
                generateJschKeyPair(type, size!!, comment)
            }
        }

    private fun generateJschKeyPair(type: Int, size: Int, comment: String): String {
        val jsch = JSch()
        val keyPair = KeyPair.genKeyPair(jsch, type, size)
        keyPair.setPublicKeyComment(comment)

        val privateKeyPem = ByteArrayOutputStream().use {
            keyPair.writePrivateKey(it)
            it.toString()
        }

        keyPair.dispose()
        return privateKeyPem
    }

    private fun generateEd25519KeyPair(comment: String): String {
        // JSch can't export ED25519 keys. https://github.com/mwiede/jsch/issues/118
        // Generate the key pair using the pre-registered security provider
        val provider = Security.getProvider(EdDSASecurityProvider.PROVIDER_NAME) ?: EdDSASecurityProvider()
        val generator = KeyPairGenerator.getInstance("EdDSA", provider)
        val keyPair = generator.generateKeyPair()

        // Format the private key to modern OpenSSH PEM format (PKCS8)
        // It would be nice if the comment was written in here but I can't figure out how without writing the PEM ourselves.
        val privateKeyPem = StringWriter().use { stringWriter ->
            JcaPEMWriter(stringWriter).use { pemWriter ->
                val pkcs8Generator = JcaPKCS8Generator(keyPair.private, null)
                pemWriter.writeObject(pkcs8Generator)
            }
            stringWriter.toString()
        }

        return privateKeyPem
    }

    fun rename(identity: Identity, newName: String) {
        viewModelScope.launch {
            try {
                identityRepository.update(identity.copy(name = newName))
            } catch (e: Exception) {
                _eventChannel.send(IdentityEvent.Error("Failed to rename key: ${e.message}"))
            }
        }
    }

    fun delete(identity: Identity) {
        viewModelScope.launch {
            try {
                lastDeletedKey = identity
                identityRepository.delete(identity)
            } catch (_: Exception) {
                _eventChannel.send(IdentityEvent.Error("Failed to delete the key."))
            }
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            lastDeletedKey?.let { identityRepository.insert(it) }
        }
    }

    override fun showPublicKeyFor(identity: Identity) {
        viewModelScope.launch {
            try {
                val publicKey = getPublicKey(identity)
                _eventChannel.send(IdentityEvent.ShowPublicKey(publicKey))
            } catch (e: Exception) {
                _eventChannel.send(IdentityEvent.Error("Failed to derive public key: ${e.message}"))
            }
        }
    }

    override fun exportPublicKeyFor(identity: Identity) {
        viewModelScope.launch {
            try {
                val publicKey = getPublicKey(identity)
                // Extract key type from the public key string (e.g., "ssh-rsa")
                val keyType = publicKey.split(" ")[0].replace("ssh-", "")
                val filename = "${identity.name}_id_${keyType}.pub"
                _eventChannel.send(IdentityEvent.ExportPublicKey(filename, publicKey))
            } catch (e: Exception) {
                _eventChannel.send(IdentityEvent.Error("Failed to prepare key for export: ${e.message}"))
            }
        }
    }

    override suspend fun getPublicKey(identity: Identity): String = withContext(ioDispatcher) {
        val privateKey = cryptoManager.decrypt(identity.encryptedPrivateKey)
        val keyPair = KeyPair.load(JSch(), privateKey, null)
        val outputStream = ByteArrayOutputStream()
        val comment = identity.name.ifEmpty { keyPair.publicKeyComment }
        keyPair.writePublicKey(outputStream, comment)
        val publicKeyString = outputStream.toString(Charsets.UTF_8.name())
        keyPair.dispose()
        publicKeyString.trim()
    }
}

sealed class IdentityEvent {
    data class ShowPublicKey(val publicKey: String) : IdentityEvent()
    data class ExportPublicKey(val filename: String, val content: String) : IdentityEvent()

    data class Error(val message: String) : IdentityEvent()
    data object KeyAdded : IdentityEvent()
}
