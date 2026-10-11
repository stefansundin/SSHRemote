package com.stefansundin.sshremote.data.host

import androidx.annotation.Keep

/**
 * Captures network service discovery metadata.
 */
@Keep
data class ServiceInfo(
    val serviceName: String,
    val serviceType: String,
    val hostAddresses: List<String> = emptyList(),
    val port: Int? = null,
    val attributes: Map<String, String> = emptyMap(),
    val networkHandle: Long? = null,
)
