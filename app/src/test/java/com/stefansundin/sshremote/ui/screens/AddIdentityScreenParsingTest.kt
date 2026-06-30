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

package com.stefansundin.sshremote.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Base64

class AddIdentityScreenParsingTest {
    @Test
    fun splitPrivateKeyAndCertificate_extractsCertificateAfterOpenSshKey() {
        val privateKey = """
            -----BEGIN OPENSSH PRIVATE KEY-----
            key-content
            -----END OPENSSH PRIVATE KEY-----
        """.trimIndent()
        val certificate = "ssh-ed25519-cert-v01@openssh.com AAAA comment"

        val (splitPrivateKey, splitCertificate) = splitPrivateKeyAndCertificate("$privateKey\n$certificate")

        assertEquals(privateKey, splitPrivateKey)
        assertEquals(certificate, splitCertificate)
    }

    @Test
    fun splitPrivateKeyAndCertificate_extractsCertificateBeforeOpenSshKey() {
        val privateKey = """
            -----BEGIN OPENSSH PRIVATE KEY-----
            key-content
            -----END OPENSSH PRIVATE KEY-----
        """.trimIndent()
        val certificate = "ssh-ed25519-cert-v01@openssh.com AAAA comment"

        val (splitPrivateKey, splitCertificate) = splitPrivateKeyAndCertificate("$certificate\n$privateKey")

        assertEquals(privateKey, splitPrivateKey)
        assertEquals(certificate, splitCertificate)
    }

    @Test
    fun splitPrivateKeyAndCertificate_returnsNullCertificateForPkcs8Key() {
        val privateKey = """
            -----BEGIN PRIVATE KEY-----
            key-content
            -----END PRIVATE KEY-----
        """.trimIndent()
        val certificate = "ssh-rsa-cert-v01@openssh.com AAAA comment"

        val (splitPrivateKey, splitCertificate) = splitPrivateKeyAndCertificate("$privateKey\n\n$certificate")

        assertEquals("$privateKey\n\n$certificate", splitPrivateKey)
        assertNull(splitCertificate)
    }

    @Test
    fun splitPrivateKeyAndCertificate_returnsNullCertificateWhenMissing() {
        val privateKey = """
            -----BEGIN OPENSSH PRIVATE KEY-----
            key-content
            -----END OPENSSH PRIVATE KEY-----
        """.trimIndent()

        val (splitPrivateKey, splitCertificate) = splitPrivateKeyAndCertificate(privateKey)

        assertEquals(privateKey, splitPrivateKey)
        assertNull(splitCertificate)
    }

    @Test
    fun decodeBase64OrNull_acceptsMimeStandardAndUrlSafeBase64() {
        val payload = "zip-bytes"
        val mimeEncoded = Base64.getMimeEncoder().encodeToString(payload.toByteArray())
        val standardEncoded = Base64.getEncoder().encodeToString(payload.toByteArray())
        val urlSafeEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray())

        assertEquals(payload, decodeBase64OrNull(mimeEncoded)?.decodeToString())
        assertEquals(payload, decodeBase64OrNull(standardEncoded)?.decodeToString())
        assertEquals(payload, decodeBase64OrNull(urlSafeEncoded)?.decodeToString())
        assertNotNull(decodeBase64OrNull(mimeEncoded))
    }
}
