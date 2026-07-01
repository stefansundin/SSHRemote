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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SshUriImportTest {
    @Test
    fun `toSshUri round-trips through parseSshUri`() {
        val host = ImportedSshHost(
            name = "Office Server",
            hostname = "example.com",
            port = 2222,
            user = "alice",
            knownHosts = listOf(
                "example.com ssh-ed25519 AAAAB3NzaC1lZDI1NTE5AAAA",
                "example.com ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQ",
            ),
        )

        val result = parseSshUri(host.toSshUri())

        assertEquals(host, result)
    }

    @Test
    fun `parseSshUri parses standard ssh URI fields`() {
        val result = parseSshUri(
            "ssh://alice@example.com:2222?name=Office+Server&hostKey%5B%5D=example.com+ssh-ed25519+AAAAB3NzaC1lZDI1NTE5AAAA",
        )

        requireNotNull(result)
        assertEquals("Office Server", result.name)
        assertEquals("example.com", result.hostname)
        assertEquals(2222, result.port)
        assertEquals("alice", result.user)
        assertEquals(listOf("example.com ssh-ed25519 AAAAB3NzaC1lZDI1NTE5AAAA"), result.knownHosts)
    }

    @Test
    fun `parseSshUri falls back to hostname for name and keeps multiple host keys`() {
        val result = parseSshUri(
            "ssh://pi@192.168.1.10?hostKey%5B%5D=key1&hostKey%5B%5D=key2",
        )

        requireNotNull(result)
        assertEquals("192.168.1.10", result.name)
        assertEquals("192.168.1.10", result.hostname)
        assertNull(result.port)
        assertEquals("pi", result.user)
        assertEquals(listOf("key1", "key2"), result.knownHosts)
    }

    @Test
    fun `parseSshUri rejects non-ssh URIs`() {
        assertNull(parseSshUri("https://example.com"))
        assertNull(parseSshUri("not a uri"))
    }
}
