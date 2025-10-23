package com.ktar.ui.screens.hostlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ktar.data.datastore.HostDataStore
import com.ktar.data.model.Host
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the host list screen.
 */
class HostListViewModel(
    private val hostDataStore: HostDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<HostListUiState>(HostListUiState.Loading)
    val uiState: StateFlow<HostListUiState> = _uiState.asStateFlow()

    init {
        loadHosts()
    }

    /**
     * Loads all saved hosts.
     */
    private fun loadHosts() {
        viewModelScope.launch {
            hostDataStore.hostsFlow.collect { hosts ->
                _uiState.value = if (hosts.isEmpty()) {
                    HostListUiState.Empty
                } else {
                    HostListUiState.Success(hosts)
                }
            }
        }
    }

    /**
     * Deletes a host.
     *
     * @param hostId ID of the host to delete
     */
    fun deleteHost(hostId: String) {
        viewModelScope.launch {
            try {
                hostDataStore.deleteHost(hostId)
            } catch (e: Exception) {
                _uiState.value = HostListUiState.Error(e.message ?: "Failed to delete host")
            }
        }
    }

    /**
     * Updates the last used timestamp for a host.
     *
     * @param hostId ID of the host
     */
    fun updateLastUsed(hostId: String) {
        viewModelScope.launch {
            hostDataStore.updateLastUsed(hostId)
        }
    }
}

/**
 * UI state for the host list screen.
 */
sealed class HostListUiState {
    object Loading : HostListUiState()
    object Empty : HostListUiState()
    data class Success(val hosts: List<Host>) : HostListUiState()
    data class Error(val message: String) : HostListUiState()
}
