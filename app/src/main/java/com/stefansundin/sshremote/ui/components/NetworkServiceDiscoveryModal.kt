package com.stefansundin.sshremote.ui.components

import android.app.Application
import android.net.nsd.NsdServiceInfo
import android.os.PatternMatcher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stefansundin.sshremote.NetworkServiceDiscovery
import com.stefansundin.sshremote.NetworkServiceDiscoveryEvent
import com.stefansundin.sshremote.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.TreeMap

// This code is only used before Android 17 where there is no DiscoveryRequest.FLAG_SHOW_PICKER.

data class NetworkServiceDiscoveryUiState(
    val isSearching: Boolean = false,
    val services: List<NsdServiceInfo> = emptyList(),
    val error: String? = null,
)

class NetworkServiceDiscoveryViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(NetworkServiceDiscoveryUiState())
    val uiState: StateFlow<NetworkServiceDiscoveryUiState> = _uiState.asStateFlow()

    private val servicesByName = TreeMap<String, NsdServiceInfo>()
    private var searchJob: Job? = null

    fun startSearch(
        serviceType: String,
        nameFilter: PatternMatcher? = null,
    ) {
        if (searchJob?.isActive == true) {
            return
        }
        servicesByName.clear()
        _uiState.value = NetworkServiceDiscoveryUiState(isSearching = true)
        searchJob = viewModelScope.launch {
            NetworkServiceDiscovery.discoverServicesLegacy(getApplication(), serviceType, nameFilter)
                .catch { e ->
                    if (e is CancellationException) {
                        throw e
                    }
                    _uiState.update { it.copy(isSearching = false, error = e.message) }
                }
                .collect { event ->
                    when (event) {
                        is NetworkServiceDiscoveryEvent.ServiceAvailable ->
                            servicesByName[event.service.serviceName] = event.service

                        is NetworkServiceDiscoveryEvent.ServiceLost ->
                            servicesByName.remove(event.service.serviceName)
                    }
                    _uiState.update { it.copy(services = servicesByName.values.toList()) }
                }
        }
    }

    fun stopSearch() {
        searchJob?.cancel()
        searchJob = null
        servicesByName.clear()
        _uiState.value = NetworkServiceDiscoveryUiState()
    }

    override fun onCleared() {
        searchJob?.cancel()
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NetworkServiceDiscoveryViewModel(application) as T
                }
            }
    }
}

/**
 * A bottom sheet that scans for [serviceType] and lets the user pick a service.
 * Scanning starts as soon as the sheet appears and stops the moment it leaves composition.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkServiceDiscoveryModal(
    modifier: Modifier = Modifier,
    serviceType: String = NetworkServiceDiscovery.SSH_SERVICE_TYPE,
    nameFilter: PatternMatcher? = null,
    onServiceSelected: (NsdServiceInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: NetworkServiceDiscoveryViewModel = viewModel(
        factory = NetworkServiceDiscoveryViewModel.factory(context.applicationContext as Application),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(serviceType, nameFilter) {
        viewModel.startSearch(serviceType, nameFilter)
        onDispose { viewModel.stopSearch() }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.choose_a_device), style = MaterialTheme.typography.titleMedium)
                if (uiState.isSearching) {
                    Spacer(Modifier.width(12.dp))
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                }
            }
            Spacer(Modifier.padding(top = 12.dp))

            when {
                uiState.error != null -> {
                    Text(uiState.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.padding(top = 12.dp))
                    Button(onClick = { viewModel.startSearch(serviceType, nameFilter) }) {
                        Text(stringResource(R.string.retry))
                    }
                }

                uiState.services.isEmpty() -> {
                    Text(
                        stringResource(if (uiState.isSearching) R.string.looking_for_devices else R.string.no_devices_found),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                else -> {
                    LazyColumn {
                        items(uiState.services, key = { it.serviceName }) { service ->
                            val addresses =
                                NetworkServiceDiscovery.getHostAddresses(service).map { "${it}:${service.port}" }

                            ListItem(
                                headlineContent = { Text(service.serviceName) },
                                supportingContent = { Text(addresses.joinToString("\n")) },
                                modifier = Modifier.clickable { onServiceSelected(service) },
                            )
                        }
                    }
                }
            }
        }
    }
}
