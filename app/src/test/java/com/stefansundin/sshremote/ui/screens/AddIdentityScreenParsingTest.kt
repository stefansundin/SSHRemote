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
