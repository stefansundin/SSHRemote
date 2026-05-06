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

import java.util.Base64

@Suppress("SpellCheckingInspection")
private val knownHostKeyTypePrefixes = listOf("ssh-", "ecdsa-", "sk-")
private val knownHostKeyDataPattern = Regex("^[A-Za-z0-9+/=_-]+$")

fun extractKnownHostCandidateLines(content: String): List<String> {
    return content.lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .toList()
}

fun isValidKnownHostLine(line: String): Boolean {
    val parts = line.trim().split(Regex("\\s+"))
    if (parts.isEmpty()) {
        return false
    }

    var index = 0
    if (parts[index].startsWith("@")) {
        if (parts[index].length == 1 || parts.size < 4) {
            return false
        }
        index++
    }

    if (parts.size - index < 3) {
        return false
    }

    val hosts = parts[index]
    val keyType = parts[index + 1]
    val keyData = parts[index + 2]

    if (hosts.isBlank()) {
        return false
    }

    val hasSupportedKeyTypePrefix = knownHostKeyTypePrefixes.any { keyType.startsWith(it) }
    val hasKnownHostStyleKeyType = hasSupportedKeyTypePrefix || keyType.contains("@openssh.com")
    if (!hasKnownHostStyleKeyType) {
        return false
    }

    val decodedKey = decodeKnownHostKeyOrNull(keyData) ?: return false
    return isValidSshPublicKeyBlob(keyType, decodedKey)
}

private fun decodeKnownHostKeyOrNull(content: String): ByteArray? {
    val trimmed = content.trim()
    if (trimmed.isEmpty() || !knownHostKeyDataPattern.matches(trimmed)) {
        return null
    }

    return try {
        Base64.getDecoder().decode(trimmed)
    } catch (_: IllegalArgumentException) {
        try {
            Base64.getUrlDecoder().decode(trimmed)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}

private fun isValidSshPublicKeyBlob(keyType: String, decodedKey: ByteArray): Boolean {
    if (decodedKey.size < 4) {
        return false
    }

    val keyTypeLength = (
            ((decodedKey[0].toInt() and 0xff) shl 24) or
                    ((decodedKey[1].toInt() and 0xff) shl 16) or
                    ((decodedKey[2].toInt() and 0xff) shl 8) or
                    (decodedKey[3].toInt() and 0xff)
            )
    if (keyTypeLength <= 0 || decodedKey.size < 4 + keyTypeLength) {
        return false
    }

    val embeddedKeyType = decodedKey.copyOfRange(4, 4 + keyTypeLength).decodeToString()
    return embeddedKeyType == keyType
}

fun parseKnownHostLines(content: String): List<String> {
    return extractKnownHostCandidateLines(content)
        .filter(::isValidKnownHostLine)
}

fun countInvalidKnownHostLines(content: String): Int {
    return extractKnownHostCandidateLines(content)
        .count { !isValidKnownHostLine(it) }
}
