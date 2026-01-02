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

    @Suppress("DEPRECATION")
    fun validateHost(host: String): Boolean {
        if (host.isBlank()) return false

        // Check if it's a valid IP Address using Android's built-in pattern.
        val isIpAddress = Patterns.IP_ADDRESS.matcher(host).matches()
        if (isIpAddress) {
            return true
        }

        // For hostnames, we'll be more flexible than a strict regex.
        // First, disallow invalid characters like spaces.
        if (host.contains(" ")) {
            return false
        }

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
