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

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import com.stefansundin.sshremote.data.CryptoManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
import kotlin.concurrent.atomics.update

class SshKeyViewModel(
    private val sshKeyRepository: SshKeyRepository,
    private val cryptoManager: CryptoManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _eventChannel = Channel<SshKeyEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    private var lastDeletedKey: SshKey? = null

    val sshKeys: StateFlow<List<SshKey>> = sshKeyRepository.getAllKeys()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    companion object {
        init {
            if (Security.getProvider(EdDSASecurityProvider.PROVIDER_NAME) == null) {
                Security.addProvider(EdDSASecurityProvider())
            }
        }
    }

    fun insert(name: String, privateKey: String) {
        viewModelScope.launch {
            try {
                val encryptedPrivateKey = cryptoManager.encrypt(privateKey.toByteArray())
                sshKeyRepository.insert(
                    SshKey(
                        name = name,
                        encryptedPrivateKey = encryptedPrivateKey,
                    ),
                )
            } catch (_: Exception) {
                _eventChannel.send(SshKeyEvent.Error("Failed to save the imported key."))
            }
        }
    }

    fun generateAndInsert(name: String, type: Int, comment: String) {
        viewModelScope.launch {
            try {
                val (privateKey, publicKey) = generateKeyPair(type, comment)
                val encryptedPrivateKey = cryptoManager.encrypt(privateKey.toByteArray())
                sshKeyRepository.insert(
                    SshKey(
                        name = name,
                        encryptedPrivateKey = encryptedPrivateKey,
                    ),
                )
                _eventChannel.send(SshKeyEvent.ShowPublicKey(publicKey))
            } catch (e: Exception) {
                _eventChannel.send(SshKeyEvent.Error("Failed to generate and save key pair: ${e.message}"))
            }
        }
    }

    private suspend fun generateKeyPair(type: Int, comment: String): Pair<String, String> =
        withContext(ioDispatcher) { // Use the injected dispatcher
            if (type == KeyPair.ED25519) {
                generateEd25519KeyPair(comment)
            } else {
                generateJschKeyPair(type, comment)
            }
        }

    private fun generateJschKeyPair(type: Int, comment: String): Pair<String, String> {
        val jsch = JSch()
        // For RSA, 2048 is a reasonable default. For other types, JSch ignores it.
        val keyPair = KeyPair.genKeyPair(jsch, type, 2048)
        keyPair.setPublicKeyComment(comment)

        val privateKeyString = ByteArrayOutputStream().use {
            keyPair.writePrivateKey(it)
            it.toString()
        }
        val publicKeyString = ByteArrayOutputStream().use {
            keyPair.writePublicKey(it, comment)
            it.toString()
        }

        keyPair.dispose()
        return Pair(privateKeyString, publicKeyString)
    }

    private fun generateEd25519KeyPair(comment: String): Pair<String, String> {
        // JSch can't export ED25519 keys. https://github.com/mwiede/jsch/issues/118
        // Generate the key pair using the pre-registered security provider
        val generator = KeyPairGenerator.getInstance("EdDSA")
        val keyPair = generator.generateKeyPair()

        // Format the private key to modern OpenSSH PEM format (PKCS8)
        val privateKeyPem = StringWriter().use { stringWriter ->
            JcaPEMWriter(stringWriter).use { pemWriter ->
                val pkcs8Generator = JcaPKCS8Generator(keyPair.private, null)
                pemWriter.writeObject(pkcs8Generator)
            }
            stringWriter.toString()
        }

        // Format the public key to the standard OpenSSH format (ssh-ed25519 AAAA...)
        val publicKeyBytes = keyPair.public.encoded
        // The actual key part is the last 32 bytes of the encoded public key
        val ed25519Key = publicKeyBytes.takeLast(32).toByteArray()

        val outputStream = ByteArrayOutputStream()
        val keyType = "ssh-ed25519".toByteArray()

        // Encode the public key in the required format: [key_type_len][key_type][key_len][key]
        outputStream.write(byteArrayOf(0, 0, 0, keyType.size.toByte()))
        outputStream.write(keyType)
        outputStream.write(byteArrayOf(0, 0, 0, ed25519Key.size.toByte()))
        outputStream.write(ed25519Key)

        val publicKeyString = "ssh-ed25519 ${
            Base64.encodeToString(
                outputStream.toByteArray(),
                Base64.NO_WRAP,
            )
        } $comment"

        return Pair(privateKeyPem, publicKeyString)
    }

    fun rename(sshKey: SshKey, newName: String) {
        viewModelScope.launch {
            try {
                sshKeyRepository.update(sshKey.copy(name = newName))
            } catch (e: Exception) {
                _eventChannel.send(SshKeyEvent.Error("Failed to rename key: ${e.message}"))
            }
        }
    }

    fun delete(sshKey: SshKey) {
        viewModelScope.launch {
            try {
                lastDeletedKey = sshKey
                sshKeyRepository.delete(sshKey)
            } catch (_: Exception) {
                _eventChannel.send(SshKeyEvent.Error("Failed to delete the key."))
            }
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            lastDeletedKey?.let { sshKeyRepository.insert(it) }
        }
    }

    fun showPublicKeyFor(sshKey: SshKey) {
        viewModelScope.launch {
            try {
                val publicKey = getPublicKey(sshKey)
                _eventChannel.send(SshKeyEvent.ShowPublicKey(publicKey))
            } catch (e: Exception) {
                _eventChannel.send(SshKeyEvent.Error("Failed to derive public key: ${e.message}"))
            }
        }
    }

    fun exportPublicKeyFor(sshKey: SshKey) {
        viewModelScope.launch {
            try {
                val publicKey = getPublicKey(sshKey)
                // Extract key type from the public key string (e.g., "ssh-rsa")
                val keyType = publicKey.split(" ")[0].replace("ssh-", "")
                val filename = "${sshKey.name}_id_${keyType}.pub"
                _eventChannel.send(SshKeyEvent.ExportPublicKey(filename, publicKey))
            } catch (e: Exception) {
                _eventChannel.send(SshKeyEvent.Error("Failed to prepare key for export: ${e.message}"))
            }
        }
    }

    private suspend fun getPublicKey(sshKey: SshKey): String = withContext(ioDispatcher) {
        val privateKey = cryptoManager.decrypt(sshKey.encryptedPrivateKey)
        val keypair = KeyPair.load(JSch(), privateKey, null)
        val outputStream = ByteArrayOutputStream()
        val comment = sshKey.name.ifEmpty { keypair.publicKeyComment }
        keypair.writePublicKey(outputStream, comment)
        val publicKeyString = outputStream.toString(Charsets.UTF_8.name())
        keypair.dispose()
        publicKeyString.trim()
    }
}

sealed class SshKeyEvent {
    data class ShowPublicKey(val publicKey: String) : SshKeyEvent()
    data class ExportPublicKey(val filename: String, val content: String) : SshKeyEvent()

    data class Error(val message: String) : SshKeyEvent()
}
