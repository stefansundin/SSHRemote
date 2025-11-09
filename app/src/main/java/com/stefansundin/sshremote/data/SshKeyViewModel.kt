/*
SSH Remote
Copyright (C) 2025  Stefan Sundin

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.stefansundin.sshremote.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import com.stefansundin.sshremote.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator
import java.io.ByteArrayOutputStream
import java.io.StringWriter
import java.security.KeyPairGenerator
import java.security.Security
import android.util.Base64
import androidx.compose.runtime.mutableStateOf
import net.i2p.crypto.eddsa.EdDSASecurityProvider

class SshKeyViewModel(
    private val sshKeyRepository: SshKeyRepository,
    private val cryptoManager: CryptoManager,
) : ViewModel() {

    private val _newPublicKeyFlow = MutableSharedFlow<String>()
    val newPublicKeyFlow = _newPublicKeyFlow.asSharedFlow()
    val keyToExport = mutableStateOf<SshKey?>(null)

    val sshKeys: StateFlow<List<SshKey>> = sshKeyRepository.getAllKeys()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun insert(name: String, privateKey: String) {
        viewModelScope.launch {
            val encryptedPrivateKey = cryptoManager.encrypt(privateKey.toByteArray())
            sshKeyRepository.insert(SshKey(name = name, encryptedPrivateKey = encryptedPrivateKey))
        }
    }

    fun generateAndInsert(name: String, type: Int, comment: String) {
        viewModelScope.launch {
            val (privateKey, publicKey) = generateKeyPair(type, comment)
            val encryptedPrivateKey = cryptoManager.encrypt(privateKey.toByteArray())

            sshKeyRepository.insert(SshKey(name = name, encryptedPrivateKey = encryptedPrivateKey))

            _newPublicKeyFlow.emit(publicKey)
        }
    }

    private suspend fun generateKeyPair(type: Int, comment: String): Pair<String, String> =
        withContext(Dispatchers.IO) {
            if (type == KeyPair.ED25519) {
                // Use Bouncy Castle for Ed25519
                generateEd25519KeyPair(comment)
            } else {
                // Use JSch for other types (e.g., RSA)
                generateJschKeyPair(type, comment)
            }
        }

    private fun generateJschKeyPair(type: Int, comment: String): Pair<String, String> {
        val jsch = JSch()
        // For RSA, 2048 is a reasonable default. For other types, JSch ignores it.
        val keyPair = KeyPair.genKeyPair(jsch, type, 2048)
        keyPair.setPublicKeyComment(comment)

        val privateKeyString =
            ByteArrayOutputStream().use {
                keyPair.writePrivateKey(it)
                it.toString()
            }
        val publicKeyString =
            ByteArrayOutputStream().use {
                keyPair.writePublicKey(it, comment)
                it.toString()
            }

        keyPair.dispose()
        return Pair(privateKeyString, publicKeyString)
    }

    private fun generateEd25519KeyPair(comment: String): Pair<String, String> {
        Security.addProvider(EdDSASecurityProvider())
        // 1. Generate the key pair using Bouncy Castle provider
        val generator = KeyPairGenerator.getInstance("EdDSA")
        val keyPair = generator.generateKeyPair()

        // 2. Format the private key to modern OpenSSH PEM format
        val privateKeyPem = StringWriter().use { stringWriter ->
            JcaPEMWriter(stringWriter).use { pemWriter ->
                val pkcs8Generator = JcaPKCS8Generator(keyPair.private, null)
                pemWriter.writeObject(pkcs8Generator)
            }
            stringWriter.toString()
        }

        // 3. Format the public key to the standard OpenSSH format (ssh-ed25519 AAAA...)
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

        val publicKeyString = "ssh-ed25519 ${Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)} $comment"

        return Pair(privateKeyPem, publicKeyString)
    }

    fun delete(sshKey: SshKey) {
        viewModelScope.launch {
            sshKeyRepository.delete(sshKey)
        }
    }
}
