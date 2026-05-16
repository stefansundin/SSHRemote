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

import android.util.Log
import com.jcraft.jsch.HostKey

private fun HostKey.toKnownHostsLine(): String {
    return "${marker.orEmpty()} $host $type $key ${comment.orEmpty()}".trim()
}

suspend fun persistAcceptedHostKey(
    repository: HostRepository,
    host: Host,
    hostKey: HostKey?,
): Host {
    if (hostKey == null) return host

    val knownHostsLine = hostKey.toKnownHostsLine()
    if (host.knownHosts.contains(knownHostsLine)) return host

    Log.d("HostKeyPersistence", "New host key for ${host.name}: $knownHostsLine")
    val updatedKnownHosts = host.knownHosts + knownHostsLine
    repository.updateKnownHosts(host.id, updatedKnownHosts)
    return host.copy(knownHosts = updatedKnownHosts)
}
