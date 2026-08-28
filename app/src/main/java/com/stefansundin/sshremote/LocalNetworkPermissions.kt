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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.UnknownHostException

object LocalNetworkPermissions {
    val PERMISSION = Manifest.permission.ACCESS_LOCAL_NETWORK

    fun isGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.CINNAMON_BUN) {
            return true
        }
        return ContextCompat.checkSelfPermission(context, PERMISSION) == PackageManager.PERMISSION_GRANTED
    }

    // Matches the definition of a local network from https://developer.android.com/privacy-and-security/local-network-definition:
    // RFC1918 private ranges, CGNAT, link-local, loopback, IPv6 unique local addresses, plus multicast and broadcast addresses.
    fun isLocalAddress(address: InetAddress): Boolean {
        if (address.isAnyLocalAddress || address.isLoopbackAddress) {
            return true
        }

        val octets = address.address

        if (address is Inet4Address) {
            val b0 = octets[0].toInt() and 0xFF
            val b1 = octets[1].toInt() and 0xFF

            // 1. Link Local: 169.254.0.0/16
            if (b0 == 169 && b1 == 254) return true

            // 2. CGNAT: 100.64.0.0/10 (range 100.64.0.0 – 100.127.255.255)
            if (b0 == 100 && b1 in 64..127) return true

            // 3. RFC 1918 Private Networks:
            // - 10.0.0.0/8
            if (b0 == 10) return true
            // - 172.16.0.0/12 (range 172.16.0.0 – 172.31.255.255)
            if (b0 == 172 && b1 in 16..31) return true
            // - 192.168.0.0/16
            if (b0 == 192 && b1 == 168) return true

            // 4. IPv4 Broadcast: 255.255.255.255
            if (octets.all { (it.toInt() and 0xFF) == 0xFF }) return true

            // 5. IPv4 Multicast: 224.0.0.0/4 (range 224.0.0.0 – 239.255.255.255)
            if (b0 in 224..239) return true

        } else if (address is Inet6Address) {
            // 1. Unique local addresses (ULA): fc00::/7
            if ((octets[0].toInt() and 0xFE) == 0xFC) return true

            // 2. Link-local IPv6 addresses: fe80::/10
            if ((octets[0].toInt() and 0xFF) == 0xFE && (octets[1].toInt() and 0xC0) == 0x80) return true

            // 3. IPv6 Multicast: ff00::/8
            if ((octets[0].toInt() and 0xFF) == 0xFF) return true
        }

        return false
    }

    suspend fun isLocalHost(hostname: String): Boolean = withContext(Dispatchers.IO) {
        if (hostname.endsWith(".local", ignoreCase = true)) {
            return@withContext true
        }
        try {
            InetAddress.getAllByName(hostname).any { isLocalAddress(it) }
        } catch (e: UnknownHostException) {
            false
        }
    }
}

@Composable
fun rememberLocalNetworkPermissionRequest(): suspend (String) -> Boolean {
    val context = LocalContext.current
    var result by remember { mutableStateOf<Boolean?>(null) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        result = granted
    }
    return remember {
        suspend { hostname: String ->
            if (!LocalNetworkPermissions.isGranted(context) && LocalNetworkPermissions.isLocalHost(hostname)) {
                result = null
                launcher.launch(LocalNetworkPermissions.PERMISSION)
                snapshotFlow { result }
                    .filterNotNull()
                    .first()
                    .also { result = null }
            } else {
                true
            }
        }
    }
}
