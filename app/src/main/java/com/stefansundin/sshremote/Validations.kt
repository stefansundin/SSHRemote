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

package com.stefansundin.sshremote

import android.util.Patterns

object Validations {
    fun validateName(name: String): Boolean {
        // Name must not be blank.
        return name.isNotBlank()
    }

    fun validateHostname(host: String): Boolean {
        if (host.isBlank()) return false

        // Check if it's a valid IP Address.
        if (validateIpAddress(host)) return true

        // For hostnames, we'll be more flexible than a strict regex.
        // First, disallow invalid characters like spaces.
        if (host.contains(" ")) return false

        // Allow single-label hostnames (like 'localhost' or 'raspberrypi').
        // We check if it contains no dots but is otherwise valid.
        if (!host.contains(".")) {
            // A simple check for valid characters in a single-label hostname.
            return host.all { it.isLetterOrDigit() || it == '-' }
        }

        // For multi-label hostnames (like 'example.com'), use Android's domain pattern.
        // We add a modification to allow a trailing dot, which is technically valid.
        return Patterns.DOMAIN_NAME.matcher(host).matches() ||
                (host.endsWith('.') && Patterns.DOMAIN_NAME.matcher(host.dropLast(1)).matches())
    }

    fun validateIpAddress(host: String): Boolean = validateIPv4(host) || validateIPv6(host)

    private fun validateIPv4(s: String): Boolean {
        val parts = s.split(".")
        if (parts.size != 4) return false
        for (p in parts) {
            if (p.isEmpty() || p.length > 3) return false
            if (!p.all { it.isDigit() }) return false
            if (p.length > 1 && p[0] == '0') return false // Reject leading zeros ("192.168.001.1")
            if (p.toInt() > 255) return false
        }
        return true
    }

    private fun validateIPv6(raw: String): Boolean {
        if (raw.isEmpty()) return false

        var address = raw
        // Allow optional brackets, e.g. "[::1]".
        // JSch works both with and without brackets for IPv6 addresses.
        if (address.length >= 2 && address.startsWith("[") && address.endsWith("]")) {
            address = address.substring(1, address.length - 1)
        }

        // Allow an optional zone/scope id, e.g. "fe80::1%eth0" for link-local addresses.
        val percentIndex = address.indexOf('%')
        if (percentIndex >= 0) {
            val zone = address.substring(percentIndex + 1)
            if (zone.isEmpty() || zone.contains('%')) {
                return false
            }
            address = address.substring(0, percentIndex)
        }

        if (address.isEmpty()
            || !address.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' || it == ':' || it == '.' }
            || address.contains(":::")
        ) {
            return false
        }

        val compressCount = Regex("::").findAll(address).count()
        if (compressCount > 1) {
            return false
        }
        val hasCompression = compressCount == 1

        val left: String
        val right: String
        if (hasCompression) {
            val idx = address.indexOf("::")
            left = address.substring(0, idx)
            right = address.substring(idx + 2)
        } else {
            left = address
            right = ""
        }

        fun splitGroups(part: String): List<String> =
            if (part.isEmpty()) emptyList() else part.split(":")

        val leftGroups = splitGroups(left)
        val rightGroups = if (hasCompression) {
            splitGroups(right)
        } else emptyList()

        // Only the very last group in the overall sequence may be an embedded IPv4 address.
        // e.g. "::ffff:192.168.1.1" or "64:ff9b::192.0.2.1"
        val lastSequenceIsRight = hasCompression && rightGroups.isNotEmpty()

        fun validateGroups(groups: List<String>, allowV4Last: Boolean): Int? {
            var units = 0
            groups.forEachIndexed { index, group ->
                val isLast = index == groups.lastIndex
                if (isLast && allowV4Last && group.contains('.')) {
                    if (!validateIPv4(group)) return null
                    units += 2 // an embedded IPv4 address fills 2 of the 8 hextets
                } else {
                    if (group.contains('.')) return null
                    if (group.isEmpty() || group.length > 4) return null
                    if (!group.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }) return null
                    units += 1
                }
            }
            return units
        }

        val leftUnits = validateGroups(leftGroups, allowV4Last = !lastSequenceIsRight) ?: return false
        val rightUnits = if (hasCompression) {
            validateGroups(rightGroups, allowV4Last = lastSequenceIsRight) ?: return false
        } else 0

        val totalUnits = leftUnits + rightUnits
        return if (hasCompression) totalUnits <= 7 else totalUnits == 8
    }

    fun validateUser(user: String): Boolean {
        if (user.isBlank()) return false

        // Allow letters, numbers, underscore, and hyphen.
        val userRegex = """^[a-zA-Z0-9_-]+$""".toRegex()
        return userRegex.matches(user)
    }

    fun validatePort(port: String): Boolean {
        val portNumber = port.toIntOrNull()

        // Port must be a number between 1 and 65535.
        return portNumber != null && portNumber in 1..65535
    }

}
