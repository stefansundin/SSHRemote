package com.stefansundin.sshremote

import android.content.Context
import android.net.Network
import android.net.nsd.DiscoveryRequest
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PatternMatcher
import android.os.ext.SdkExtensions
import android.util.Log
import androidx.annotation.RequiresExtension
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.Inet6Address
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Based on example code: https://developer.android.com/reference/android/net/nsd/NsdManager

class DiscoveryException(val errorCode: Int) : Exception("NSD error code $errorCode")

sealed interface NetworkServiceDiscoveryEvent {
    data class ServiceAvailable(val service: NsdServiceInfo) : NetworkServiceDiscoveryEvent
    data class ServiceLost(val service: NsdServiceInfo) : NetworkServiceDiscoveryEvent
}

object NetworkServiceDiscovery {
    private const val TAG = "NetworkServiceDiscovery"
    const val SSH_SERVICE_TYPE = "_ssh._tcp"

    fun getHostAddresses(nsdServiceInfo: NsdServiceInfo): List<String> {
        val addresses =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.TIRAMISU) >= 7) {
                nsdServiceInfo.hostAddresses
            } else {
                @Suppress("DEPRECATION")
                listOf(nsdServiceInfo.host)
            }
        val candidates = addresses.mapNotNull {
            if (it is Inet6Address) {
                "[${it.hostAddress}]"
            } else {
                it.hostAddress
            }
        }.toMutableList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.TIRAMISU) >= 17) {
            candidates.add(0, "${nsdServiceInfo.hostname}.local")
        }
        return candidates
    }

    @RequiresExtension(extension = Build.VERSION_CODES.TIRAMISU, version = 22)
    suspend fun findService(
        context: Context,
        serviceType: String = SSH_SERVICE_TYPE,
        nameFilter: PatternMatcher? = null,
        network: Network? = null,
    ): NsdServiceInfo? {
        Log.d(TAG, "findService($serviceType, $nameFilter)")

        return suspendCancellableCoroutine { cont ->
            val request = DiscoveryRequest.Builder(serviceType)
                .setServiceNameFilter(nameFilter)
                .setNetwork(network)
                .setFlags(DiscoveryRequest.FLAG_SHOW_PICKER)
                .build()

            val listener = object : NsdManager.ServiceInfoCallback {
                override fun onServiceUpdated(serviceInfo: NsdServiceInfo) {
                    Log.d(TAG, "Service updated: $serviceInfo")
                    if (cont.isActive) {
                        cont.resume(serviceInfo)
                    }
                }

                override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {
                    cont.resumeWithException(DiscoveryException(errorCode))
                }

                override fun onServiceInfoCallbackUnregistered() {
                    // This is called if the user dismisses the picker
                    if (cont.isActive) {
                        cont.resume(null)
                    }
                }

                override fun onServiceLost() {}
            }

            val nsdManager = context.getSystemService(NsdManager::class.java)
            nsdManager.registerServiceInfoCallback(request, Runnable::run, listener)
            cont.invokeOnCancellation {
                nsdManager.unregisterServiceInfoCallback(listener)
            }
        }
    }

    fun discoverServicesLegacy(
        context: Context,
        serviceType: String,
        nameFilter: PatternMatcher? = null,
    ): Flow<NetworkServiceDiscoveryEvent> = callbackFlow {
        Log.d(TAG, "discoverServicesLegacy($serviceType, $nameFilter)")

        val lock =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || SdkExtensions.getExtensionVersion(Build.VERSION_CODES.TIRAMISU) < 7) {
                context.getSystemService(WifiManager::class.java)
                    .createMulticastLock("${context.packageName}:mdns").apply { acquire() }
            } else {
                null
            }
        val nsdManager = context.getSystemService(NsdManager::class.java)
        val pendingResolves = Channel<NsdServiceInfo>(Channel.UNLIMITED)

        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onServiceFound(info: NsdServiceInfo) {
                Log.d(TAG, "Service found: $info")
                // Apps implement their own logic for which services count —
                // here that's the same name filter as before.
                if (nameFilter?.match(info.serviceName) == false) {
                    return
                }
                pendingResolves.trySend(info)
            }

            override fun onServiceLost(info: NsdServiceInfo) {
                Log.d(TAG, "Service lost: $info")
                trySend(NetworkServiceDiscoveryEvent.ServiceLost(info))
            }

            override fun onStartDiscoveryFailed(type: String, err: Int) {
                close(DiscoveryException(err))
            }

            override fun onDiscoveryStarted(type: String) {}
            override fun onDiscoveryStopped(type: String) {}
            override fun onStopDiscoveryFailed(type: String, err: Int) {}
        }

        nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

        // Resolve found services one at a time, in the order they arrived,
        // instead of calling resolveService() concurrently.
        val resolverJob = launch {
            for (found in pendingResolves) {
                try {
                    val resolved = resolveLegacy(nsdManager, found)
                    send(NetworkServiceDiscoveryEvent.ServiceAvailable(resolved))
                } catch (e: DiscoveryException) {
                    Log.d(TAG, "Resolve failed for ${found.serviceName}: ${e.errorCode}")
                }
            }
        }

        awaitClose {
            resolverJob.cancel()
            pendingResolves.close()
            try {
                nsdManager.stopServiceDiscovery(discoveryListener)
            } catch (_: IllegalArgumentException) {
            }
            lock?.release()
        }
    }

    private suspend fun resolveLegacy(
        nsdManager: NsdManager,
        info: NsdServiceInfo,
    ): NsdServiceInfo = suspendCancellableCoroutine { cont ->
        val resolveListener = object : NsdManager.ResolveListener {
            override fun onServiceResolved(info: NsdServiceInfo) {
                Log.d(TAG, "Service resolved: $info")
                cont.resume(info)
            }

            override fun onResolveFailed(info: NsdServiceInfo, err: Int) {
                Log.d(TAG, "Service resolution failed: $info")
                cont.resumeWithException(DiscoveryException(err))
            }
        }
        Log.d(TAG, "nsdManager.resolveService($info)")
        @Suppress("DEPRECATION")
        nsdManager.resolveService(info, resolveListener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.TIRAMISU) >= 7) {
            cont.invokeOnCancellation {
                try {
                    nsdManager.stopServiceResolution(resolveListener)
                } catch (_: IllegalArgumentException) {
                }
            }
        }
    }
}
