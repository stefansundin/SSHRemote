package com.stefansundin.sshremote.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KnownHostParsingTest {
    @Suppress("SpellCheckingInspection")
    private val validKeyData = "AAAAC3NzaC1lZDI1NTE5AAAAIBIHuHo3VqPB3RWBkpFBpn4YDhfNJ7LI5kFjR5O2p9Qw"

    @Suppress("SpellCheckingInspection")
    private val validCertificateKeyData =
        "AAAAHHNzaC1yc2EtY2VydC12MDFAb3BlbnNzaC5jb20AAAAgONiQ4M6WM6nMKOenKOz08kWm6dp0IFEwmkheo2maKQ4AAAADAQABAAAAgQDGzm9sCg1D465xSl2c4/svwZO2bs/Ar4s+d8aJaxWiOLXo1qkkZkxd571yBRmsPgEUC2tpU8lFjCbU2JtrtEfeJzbJ3unqL3aFXY4G9XlqDRYFdf/4hZgm71R7zUtQXixyr7NT3HWIBluJnr7LEUbTONZUQYpVImP/fMl6ykkxoQAAAAAAAAAAAAAAAQAAAAJwaQAAAAYAAAACcGkAAAAAAAAAAP//////////AAAAAAAAAIIAAAAVcGVybWl0LVgxMS1mb3J3YXJkaW5nAAAAAAAAABdwZXJtaXQtYWdlbnQtZm9yd2FyZGluZwAAAAAAAAAWcGVybWl0LXBvcnQtZm9yd2FyZGluZwAAAAAAAAAKcGVybWl0LXB0eQAAAAAAAAAOcGVybWl0LXVzZXItcmMAAAAAAAAAAAAAADMAAAALc3NoLWVkMjU1MTkAAAAgVBqK4PcN893KbFi8DTqEhu0Xf2XkOXLJKjJ2a8K0p3AAAABTAAAAC3NzaC1lZDI1NTE5AAAAQLG80AToroiVcRQTkLhvrMIN0HKMDGoxyBvwNARL1FclkEJ6VD5VHIe+8ua/OaS3aMX4WKa/oujFy2G+p0u1RQI="

    @Test
    fun parseKnownHostLines_keepsValidEntriesAndSkipsCommentsBlankLinesAndInvalidLines() {
        val validCertificateAuthority = "@cert-authority host.example.com ssh-ed25519 $validKeyData"
        val validHashedHost = "|1|bXlzYWx0|bXloYXNo ssh-ed25519 $validKeyData comment"
        val invalidBase64 = "host.example.com ssh-ed25519 not-base64"
        val invalidKeyType = "host.example.com definitely-not-a-key-type $validKeyData"

        val input = listOf(
            "# first comment",
            "   # second comment with leading whitespace",
            "",
            validCertificateAuthority,
            invalidBase64,
            "   ",
            validHashedHost,
            invalidKeyType,
        ).joinToString("\n")

        assertEquals(
            listOf(validCertificateAuthority, validHashedHost),
            parseKnownHostLines(input),
        )
        assertEquals(2, countInvalidKnownHostLines(input))
    }

    @Test
    fun parseKnownHostLines_handlesCrLfAndTrimsEachLine() {
        @Suppress("SpellCheckingInspection")
        val validLine =
            "host.example.com ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIBIHuHo3VqPB3RWBkpFBpn4YDhfNJ7LI5kFjR5O2p9Qw"
        val input = "  $validLine  \r\n\r\n# comment\r\n"

        assertEquals(listOf(validLine), parseKnownHostLines(input))
        assertEquals(0, countInvalidKnownHostLines(input))
    }

    @Test
    fun isValidKnownHostLine_acceptsBracketedHostAndOpenSshKeyTypes() {
        assertTrue(
            isValidKnownHostLine(
                "[host.example.com]:2222,[2001:db8::1]:22 ssh-ed25519 $validKeyData",
            ),
        )
        assertTrue(
            isValidKnownHostLine(
                "host.example.com ssh-rsa-cert-v01@openssh.com $validCertificateKeyData",
            ),
        )
    }

    @Test
    fun isValidKnownHostLine_rejectsTooFewFieldsAndEmptyMarker() {
        assertFalse(isValidKnownHostLine("host.example.com ssh-ed25519"))
        assertFalse(isValidKnownHostLine("@ host.example.com ssh-ed25519 $validKeyData"))
    }
}
