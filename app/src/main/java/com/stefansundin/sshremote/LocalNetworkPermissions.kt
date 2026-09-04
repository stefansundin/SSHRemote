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
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.UnknownHostException

private fun ipv4ToInt(octets: ByteArray): Int {
    return (octets[0].toInt() and 0xFF shl 24) or
            (octets[1].toInt() and 0xFF shl 16) or
            (octets[2].toInt() and 0xFF shl 8) or
            (octets[3].toInt() and 0xFF)
}

private class Ipv4Range(baseAddress: String, prefixBits: Int) {
    private val mask = if (prefixBits == 0) 0 else -1 shl (32 - prefixBits)
    private val base = ipv4ToInt(InetAddress.getByName(baseAddress).address) and mask

    fun contains(addr: Int): Boolean = (addr and mask) == base
}

object LocalNetworkPermissions {
    @RequiresApi(Build.VERSION_CODES.CINNAMON_BUN)
    val PERMISSION = Manifest.permission.ACCESS_LOCAL_NETWORK

    @RequiresApi(Build.VERSION_CODES.CINNAMON_BUN)
    fun isGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, PERMISSION) == PackageManager.PERMISSION_GRANTED
    }

    private val IPV4_LOCAL_RANGES = listOf(
        Ipv4Range("169.254.0.0", 16),       // 169.254.0.0/16     - Link-local (APIPA)
        Ipv4Range("100.64.0.0", 10),        // 100.64.0.0/10      - Carrier-grade NAT / shared address space (RFC 6598)
        Ipv4Range("10.0.0.0", 8),           // 10.0.0.0/8         - Private network (RFC 1918)
        Ipv4Range("172.16.0.0", 12),        // 172.16.0.0/12      - Private network (RFC 1918)
        Ipv4Range("192.168.0.0", 16),       // 192.168.0.0./16    - Private network (RFC 1918)
        Ipv4Range("224.0.0.0", 4),          // 224.0.0.0/4        - Multicast
        Ipv4Range("255.255.255.255", 32),   // 255.255.255.255/32 - Limited broadcast
    )

    // Matches the definition of a local network from https://developer.android.com/privacy-and-security/local-network-definition:
    // RFC1918 private ranges, CGNAT, link-local, loopback, IPv6 unique local addresses, plus multicast and broadcast addresses.
    fun isLocalAddress(address: InetAddress): Boolean {
        if (address.isAnyLocalAddress || address.isLoopbackAddress) {
            return true
        }

        val octets = address.address
        if (address is Inet4Address) {
            val addr = ipv4ToInt(octets)
            return IPV4_LOCAL_RANGES.any { it.contains(addr) }
        } else if (address is Inet6Address) {
            val byte0 = octets[0].toInt() and 0xFF
            val byte1 = octets[1].toInt() and 0xFF
            return (byte0 and 0xFE) == 0xFC                        // fc00::/7  - Unique Local Address (ULA)
                    || (byte0 == 0xFE && (byte1 and 0xC0) == 0x80) // fe80::/10 - Link-local
                    || (byte0 == 0xFF)                             // ff00::/8  - Multicast
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
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        result = granted
    }

    var primerRequest by remember { mutableStateOf<CompletableDeferred<Boolean>?>(null) }

    primerRequest?.let { deferred ->
        AlertDialog(
            title = { Text(stringResource(R.string.allow_local_network_access)) },
            text = {
                Text(stringResource(R.string.local_network_access_permission_explanation))
            },
            properties = DialogProperties(dismissOnClickOutside = false),
            onDismissRequest = {
                deferred.complete(false)
                primerRequest = null
            },
            confirmButton = {
                Button(
                    onClick = {
                        deferred.complete(true)
                        primerRequest = null
                    },
                ) {
                    Text(stringResource(R.string.cont))
                }
            },
        )
    }

    return remember {
        suspend { hostname: String ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.CINNAMON_BUN
                || LocalNetworkPermissions.isGranted(context)
                || !LocalNetworkPermissions.isLocalHost(hostname)
            ) {
                true
            } else {
                val deferred = CompletableDeferred<Boolean>()
                primerRequest = deferred

                if (!deferred.await()) {
                    false // user declined the primer
                } else {
                    result = null
                    launcher.launch(LocalNetworkPermissions.PERMISSION)
                    snapshotFlow { result }
                        .filterNotNull()
                        .first()
                        .also { result = null }
                }
            }
        }
    }
}
