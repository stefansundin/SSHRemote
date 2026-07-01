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

package com.stefansundin.sshremote.data.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.GZIPOutputStream

@Suppress("SpellCheckingInspection")
class SettingsImporterTest {
    @Test
    fun decodeImportedSettingsPayload_returnsRawJsonWithoutAttemptingBase64Decode() {
        var decoderCalled = false
        val json = "\uFEFF  \n{\"hosts\":[]}"

        val result = decodeImportedSettingsPayload(json) {
            decoderCalled = true
            error("Base64 decode should not have been attempted")
        }

        assertEquals(json, result)
        assertFalse(decoderCalled)
    }

    @Test
    fun decodeImportedSettingsPayload_decompressesBase64EncodedGzipPayload() {
        val json = "{\"hosts\":[],\"theme\":\"System\"}"
        val encoded = Base64.getMimeEncoder().encodeToString(gzip(json))

        val result = decodeImportedSettingsPayload(encoded, Base64.getMimeDecoder()::decode)

        assertEquals(json, result)
    }

    @Test
    fun decodeImportedSettingsPayload_returnsOriginalStringWhenBase64PayloadIsNotGzip() {
        val encoded = Base64.getMimeEncoder().encodeToString("not gzip".toByteArray())

        val result = decodeImportedSettingsPayload(encoded, Base64.getMimeDecoder()::decode)

        assertEquals(encoded, result)
    }

    @Test
    fun decodeImportedSettingsPayload_throwsImportExceptionForCorruptGzipPayload() {
        val corruptPayload = byteArrayOf(0x1f, 0x8b.toByte(), 0x08, 0x00)
        val encoded = Base64.getMimeEncoder().encodeToString(corruptPayload)

        val exception = assertThrows(ImportException::class.java) {
            decodeImportedSettingsPayload(encoded, Base64.getMimeDecoder()::decode)
        }

        assertTrue(exception.message?.contains("Invalid file format") == true)
    }

    @Test
    fun parseImportedSettingsPreview_returnsHostCountFromImportedSettings() {
        val preview = parseImportedSettingsPreview("{\"hosts\":[{},{}]}")

        assertEquals(2, preview.hostCount)
        assertEquals(0, preview.conflictingHostCount)
    }

    @Test
    fun parseImportedSettingsPreview_countsHostsThatConflictByInternalId() {
        val preview = parseImportedSettingsPreview(
            data = "{\"hosts\":[{\"id\":\"host-1\"},{\"id\":\"host-2\"},{\"id\":\"host-3\"}]}",
            existingHostIds = setOf("host-2", "host-3", "host-4"),
        )

        assertEquals(3, preview.hostCount)
        assertEquals(2, preview.conflictingHostCount)
    }

    @Test
    fun parseImportedSettings_throwsImportExceptionWhenHostsFieldIsMissing() {
        val exception = assertThrows(ImportException::class.java) {
            parseImportedSettings("{\"theme\":\"SYSTEM\"}")
        }

        assertEquals("Invalid file format", exception.message)
    }

    @Suppress("SameParameterValue")
    private fun gzip(data: String): ByteArray {
        val out = ByteArrayOutputStream()
        GZIPOutputStream(out).use { gzip ->
            gzip.write(data.toByteArray())
        }
        return out.toByteArray()
    }
}
