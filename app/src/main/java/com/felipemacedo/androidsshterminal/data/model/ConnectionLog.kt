package com.felipemacedo.androidsshterminal.data.model

/**
 * Represents a connection log entry.
 *
 * @property id Unique identifier for the log
 * @property hostId Associated host ID
 * @property timestamp Connection timestamp
 * @property status Connection status
 * @property duration Connection duration in milliseconds
 * @property errorMessage Error message if connection failed
 * @property lastCommand Last executed command
 * @property lastOutput Last command output
 */
data class ConnectionLog(
    val id: String,
    val hostId: String,
    val timestamp: Long,
    val status: ConnectionStatus,
    val duration: Long? = null,
    val errorMessage: String? = null,
    val lastCommand: String? = null,
    val lastOutput: String? = null
)

/**
 * Connection status enumeration.
 */
enum class ConnectionStatus {
    SUCCESS,
    FAILED,
    TIMEOUT,
    DISCONNECTED
}
