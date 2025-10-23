package com.ktar.data.model

/**
 * Represents an SSH host configuration.
 *
 * @property id Unique identifier for the host
 * @property name Display name for the connection
 * @property host Hostname or IP address
 * @property port SSH port (default 22)
 * @property username Username for authentication
 * @property authMethod Authentication method (password or key)
 * @property password Encrypted password (optional)
 * @property privateKey Encrypted private key content (optional)
 * @property createdAt Timestamp when the host was created
 * @property lastUsed Timestamp when the host was last used
 */
data class Host(
    val id: String,
    val name: String,
    val host: String,
    val port: Int = 22,
    val username: String,
    val authMethod: AuthMethod = AuthMethod.PASSWORD,
    val password: String? = null,
    val privateKey: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long? = null
)

/**
 * Authentication method for SSH connection.
 */
enum class AuthMethod {
    PASSWORD,
    PRIVATE_KEY
}
