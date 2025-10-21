package com.felipemacedo.androidsshterminal.ui.screens.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felipemacedo.androidsshterminal.data.datastore.HostDataStore
import com.felipemacedo.androidsshterminal.data.model.AuthMethod
import com.felipemacedo.androidsshterminal.data.model.Host
import com.felipemacedo.androidsshterminal.data.security.SecurityManager
import com.felipemacedo.androidsshterminal.ssh.SSHManager
import com.felipemacedo.androidsshterminal.ssh.SSHSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

/**
 * ViewModel for the connection screen.
 */
class ConnectionViewModel(
    private val hostDataStore: HostDataStore,
    private val securityManager: SecurityManager,
    private val sshManager: SSHManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectionUiState())
    val uiState: StateFlow<ConnectionUiState> = _uiState.asStateFlow()

    /**
     * Loads a host for editing.
     *
     * @param hostId ID of the host to load
     */
    fun loadHost(hostId: String) {
        viewModelScope.launch {
            hostDataStore.hostsFlow.collect { hosts ->
                hosts.find { it.id == hostId }?.let { host ->
                    _uiState.value = _uiState.value.copy(
                        name = host.name,
                        host = host.host,
                        port = host.port.toString(),
                        username = host.username,
                        authMethod = host.authMethod,
                        password = host.password?.let { securityManager.decrypt(it) } ?: "",
                        privateKey = host.privateKey?.let { securityManager.decrypt(it) } ?: "",
                        isEditMode = true,
                        editingHostId = hostId
                    )
                }
            }
        }
    }

    /**
     * Updates form field values.
     */
    fun updateName(value: String) {
        _uiState.value = _uiState.value.copy(name = value)
    }

    fun updateHost(value: String) {
        _uiState.value = _uiState.value.copy(host = value)
    }

    fun updatePort(value: String) {
        _uiState.value = _uiState.value.copy(port = value)
    }

    fun updateUsername(value: String) {
        _uiState.value = _uiState.value.copy(username = value)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun updatePrivateKey(value: String) {
        _uiState.value = _uiState.value.copy(privateKey = value)
    }

    fun updateAuthMethod(value: AuthMethod) {
        _uiState.value = _uiState.value.copy(authMethod = value)
    }

    /**
     * Validates and saves the host configuration.
     *
     * @return true if save was successful
     */
    fun saveHost(onSuccess: () -> Unit) {
        val state = _uiState.value

        // Validate inputs
        if (state.name.isBlank()) {
            _uiState.value = state.copy(error = "Connection name is required")
            return
        }
        if (state.host.isBlank()) {
            _uiState.value = state.copy(error = "Host is required")
            return
        }
        val portNum = state.port.toIntOrNull()
        if (portNum == null || portNum !in 1..65535) {
            _uiState.value = state.copy(error = "Invalid port number")
            return
        }
        if (state.username.isBlank()) {
            _uiState.value = state.copy(error = "Username is required")
            return
        }
        if (state.authMethod == AuthMethod.PASSWORD && state.password.isBlank()) {
            _uiState.value = state.copy(error = "Password is required")
            return
        }
        if (state.authMethod == AuthMethod.PRIVATE_KEY && state.privateKey.isBlank()) {
            _uiState.value = state.copy(error = "Private key is required")
            return
        }

        _uiState.value = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // Encrypt sensitive data
                val encryptedPassword = if (state.password.isNotBlank()) {
                    securityManager.encrypt(state.password)
                } else null

                val encryptedKey = if (state.privateKey.isNotBlank()) {
                    securityManager.encrypt(state.privateKey)
                } else null

                // Create host object
                val host = Host(
                    id = state.editingHostId ?: UUID.randomUUID().toString(),
                    name = state.name,
                    host = state.host,
                    port = portNum,
                    username = state.username,
                    authMethod = state.authMethod,
                    password = encryptedPassword,
                    privateKey = encryptedKey
                )

                // Save to datastore
                hostDataStore.saveHost(host)

                _uiState.value = state.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save host"
                )
            }
        }
    }

    /**
     * Tests the SSH connection.
     */
    fun testConnection() {
        val state = _uiState.value

        // Validate inputs
        if (state.host.isBlank() || state.username.isBlank()) {
            _uiState.value = state.copy(error = "Please fill in host and username")
            return
        }

        val portNum = state.port.toIntOrNull() ?: 22

        _uiState.value = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val testHost = Host(
                    id = UUID.randomUUID().toString(),
                    name = "Test",
                    host = state.host,
                    port = portNum,
                    username = state.username,
                    authMethod = state.authMethod
                )

                val success = when (state.authMethod) {
                    AuthMethod.PASSWORD -> {
                        sshManager.testConnection(testHost, password = state.password)
                    }
                    AuthMethod.PRIVATE_KEY -> {
                        sshManager.testConnection(testHost, privateKey = state.privateKey)
                    }
                }

                _uiState.value = if (success) {
                    state.copy(
                        isLoading = false,
                        testResult = "Connection successful!"
                    )
                } else {
                    state.copy(
                        isLoading = false,
                        error = "Connection failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = e.message ?: "Connection failed"
                )
            }
        }
    }

    /**
     * Clears error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, testResult = null)
    }
}

/**
 * UI state for the connection screen.
 */
data class ConnectionUiState(
    val name: String = "",
    val host: String = "",
    val port: String = "22",
    val username: String = "",
    val password: String = "",
    val privateKey: String = "",
    val authMethod: AuthMethod = AuthMethod.PASSWORD,
    val isLoading: Boolean = false,
    val error: String? = null,
    val testResult: String? = null,
    val isEditMode: Boolean = false,
    val editingHostId: String? = null
)
