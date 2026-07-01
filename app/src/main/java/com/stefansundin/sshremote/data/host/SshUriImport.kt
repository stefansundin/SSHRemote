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

import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class ImportedSshHost(
    val name: String? = null,
    val hostname: String? = null,
    val port: Int? = null,
    val user: String? = null,
    val knownHosts: List<String> = emptyList(),
)

fun parseSshUri(sshUri: String): ImportedSshHost? {
    val uri = runCatching { URI(sshUri) }.getOrNull() ?: return null
    if (!uri.scheme.equals("ssh", ignoreCase = true)) return null

    val queryParameters = parseQueryParameters(uri.rawQuery)
    val hostname = uri.host?.takeIf { it.isNotBlank() }
    val port = uri.port.takeIf { it != -1 }
    val user = uri.userInfo?.takeIf { it.isNotBlank() }
    val knownHosts = queryParameters["hostKey[]"].orEmpty().filter { it.isNotBlank() }
    val name =
        queryParameters["name"]
            ?.lastOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: hostname

    if (hostname == null && port == null && user == null && name == null && knownHosts.isEmpty()) {
        return null
    }

    return ImportedSshHost(
        name = name,
        hostname = hostname,
        port = port,
        user = user,
        knownHosts = knownHosts,
    )
}

private fun parseQueryParameters(rawQuery: String?): Map<String, List<String>> {
    if (rawQuery.isNullOrEmpty()) return emptyMap()

    return rawQuery
        .split("&")
        .filter { it.isNotEmpty() }
        .groupBy(
            keySelector = { parameter ->
                decodeQueryComponent(parameter.substringBefore("=", missingDelimiterValue = parameter))
            },
            valueTransform = { parameter ->
                decodeQueryComponent(parameter.substringAfter("=", missingDelimiterValue = ""))
            },
        )
}

private fun decodeQueryComponent(value: String): String {
    return URLDecoder.decode(value, StandardCharsets.UTF_8.name())
}
