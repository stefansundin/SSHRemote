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

package com.stefansundin.sshremote.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

interface ICryptoManager {
    fun encrypt(data: ByteArray): ByteArray
    fun encrypt(data: String): ByteArray
    fun decrypt(encryptedDataWithIv: ByteArray): ByteArray
    fun decryptToString(encryptedDataWithIv: ByteArray): String
}

/**
 * Manages encryption and decryption using AES/GCM/NoPadding.
 *
 * GCM provides authenticated encryption, ensuring confidentiality and integrity.
 * A 128-bit authentication tag is automatically appended and verified on decryption.
 *
 * Keys are 256-bit AES, generated and stored in the Android KeyStore.
 */
class CryptoManager: ICryptoManager {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private fun getSecretKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: generateSecretKey()
    }

    /**
     * Generates a new AES secret key and stores it in the Android KeyStore.
     * The key size defaults to 256 bits, the strongest option for AES.
     */
    private fun generateSecretKey(): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build(),
            )
        }.generateKey()
    }

    /**
     * Encrypts the given byte array.
     * @param data The plaintext data to encrypt.
     * @return A byte array containing the 12-byte IV followed by the ciphertext and authentication tag.
     */
    override fun encrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secretKey = getSecretKey()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(data)
        return iv + ciphertext
    }

    /**
     * Encrypts the given string by first converting it to a UTF-8 byte array.
     * @param data The plaintext string to encrypt.
     * @return A byte array containing the IV, ciphertext, and authentication tag.
     */
    override fun encrypt(data: String): ByteArray {
        return encrypt(data.toByteArray(Charsets.UTF_8))
    }

    /**
     * Decrypts the given byte array.
     * @param encryptedDataWithIv A byte array containing the 12-byte IV followed by the
     * ciphertext and authentication tag.
     * @return The original plaintext data.
     */
    override fun decrypt(encryptedDataWithIv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secretKey = getSecretKey()
        val iv = encryptedDataWithIv.copyOfRange(0, GCM_IV_SIZE_BYTES)
        val ciphertext = encryptedDataWithIv.copyOfRange(GCM_IV_SIZE_BYTES, encryptedDataWithIv.size)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(ciphertext)
    }

    /**
     * Decrypts the given byte array and converts the result to a UTF-8 string.
     * @param encryptedDataWithIv The encrypted data to decrypt.
     * @return The original plaintext string.
     */
    override fun decryptToString(encryptedDataWithIv: ByteArray): String {
        return decrypt(encryptedDataWithIv).toString(Charsets.UTF_8)
    }

    companion object {
        private const val KEYSTORE_ALIAS = "data_encryption_key"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
        private const val GCM_IV_SIZE_BYTES = 12
        private const val GCM_TAG_LENGTH_BITS = 128
    }
}
