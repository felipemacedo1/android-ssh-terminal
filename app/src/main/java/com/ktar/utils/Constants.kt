package com.ktar.utils

/**
 * Application-wide constants.
 */
object Constants {
    
    // SSH Connection
    const val DEFAULT_SSH_PORT = 22
    const val CONNECTION_TIMEOUT_MS = 10000
    const val COMMAND_TIMEOUT_MS = 30000
    const val MAX_RECONNECT_ATTEMPTS = 3
    
    // Security
    const val ENCRYPTION_KEY_ALIAS = "ssh_terminal_key"
    const val ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding"
    const val KEY_SIZE = 256
    const val GCM_TAG_LENGTH = 128
    
    // DataStore
    const val HOSTS_DATASTORE_NAME = "ssh_hosts"
    
    // Navigation
    object Routes {
        const val HOST_LIST = "host_list"
        const val CONNECTION = "connection"
        const val CONNECTION_WITH_ID = "connection/{hostId}"
        const val TERMINAL = "terminal/{sessionId}"
    }
    
    // Terminal
    const val MAX_TERMINAL_LINES = 1000
    const val TERMINAL_BUFFER_SIZE = 8192
    
    // Logging
    const val LOG_TAG = "SSHTerminal"
}
