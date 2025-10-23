package com.ktar.ssh

import com.ktar.data.model.AuthMethod
import com.ktar.data.model.CommandResult
import com.ktar.data.model.Host
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.userauth.keyprovider.KeyProvider
import net.schmizz.sshj.userauth.method.AuthMethod as SSHJAuthMethod
import java.io.ByteArrayOutputStream
import java.io.StringReader
import java.util.concurrent.TimeUnit

/**
 * Manages SSH connections and command execution.
 * Handles authentication, session management, and command execution.
 */
class SSHManager {

    companion object {
        private const val CONNECTION_TIMEOUT_MS = 10000
        private const val COMMAND_TIMEOUT_MS = 30000
    }

    /**
     * Creates a new SSH session.
     *
     * @param host Host configuration
     * @param password Decrypted password (if using password auth)
     * @param privateKey Decrypted private key content (if using key auth)
     * @return SSHSession instance
     * @throws Exception if connection or authentication fails
     */
    suspend fun createSession(
        host: Host,
        password: String? = null,
        privateKey: String? = null
    ): SSHSession = withContext(Dispatchers.IO) {
        val client = SSHClient()

        try {
            // Configure client
            client.addHostKeyVerifier(PromiscuousVerifier())
            client.connectTimeout = CONNECTION_TIMEOUT_MS
            client.timeout = CONNECTION_TIMEOUT_MS

            // Connect to host
            client.connect(host.host, host.port)

            // Authenticate
            when (host.authMethod) {
                AuthMethod.PASSWORD -> {
                    requireNotNull(password) { "Password is required for password authentication" }
                    client.authPassword(host.username, password)
                }
                AuthMethod.PRIVATE_KEY -> {
                    requireNotNull(privateKey) { "Private key is required for key authentication" }
                    val keyProvider = client.loadKeys(privateKey, null, null)
                    client.authPublickey(host.username, keyProvider)
                }
            }

            // Verify authentication
            if (!client.isAuthenticated) {
                throw SecurityException("Authentication failed")
            }

            SSHSession(client, host)
        } catch (e: Exception) {
            client.disconnect()
            throw e
        }
    }

    /**
     * Tests connection to a host without creating a persistent session.
     *
     * @param host Host configuration
     * @param password Decrypted password (if using password auth)
     * @param privateKey Decrypted private key content (if using key auth)
     * @return true if connection successful, false otherwise
     */
    suspend fun testConnection(
        host: Host,
        password: String? = null,
        privateKey: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val client = SSHClient()
        try {
            client.addHostKeyVerifier(PromiscuousVerifier())
            client.connectTimeout = CONNECTION_TIMEOUT_MS
            client.connect(host.host, host.port)

            when (host.authMethod) {
                AuthMethod.PASSWORD -> {
                    requireNotNull(password) { "Password required" }
                    client.authPassword(host.username, password)
                }
                AuthMethod.PRIVATE_KEY -> {
                    requireNotNull(privateKey) { "Private key required" }
                    val keyProvider = client.loadKeys(privateKey, null, null)
                    client.authPublickey(host.username, keyProvider)
                }
            }

            client.isAuthenticated
        } catch (e: Exception) {
            false
        } finally {
            client.disconnect()
        }
    }
    /**
     * Executes a command in an existing SSH session.
     *
     * @param session Active SSH session
     * @param command Command to execute
     * @return CommandResult with output and exit code
     */
    suspend fun executeCommand(session: SSHSession, command: String): CommandResult {
        return session.executeCommand(command)
    }

    /**
     * Closes an SSH session.
     *
     * @param session Session to close
     */
    suspend fun closeSession(session: SSHSession) {
        session.disconnect()
    }
}

/**
 * Represents an active SSH session.
 */
class SSHSession(
    private val client: SSHClient,
    val host: Host
) {
    private var shellSession: Session? = null
    private var shellChannel: net.schmizz.sshj.connection.channel.direct.Session.Shell? = null
    private var isActive = true

    companion object {
        private const val COMMAND_TIMEOUT_MS = 30000
    }

    /**
     * Checks if the session is still active.
     */
    fun isConnected(): Boolean = isActive && client.isConnected && client.isAuthenticated

    /**
     * Executes a single command and returns the result.
     *
     * @param command Command to execute
     * @return CommandResult with output and exit code
     */
    suspend fun executeCommand(command: String): CommandResult = withContext(Dispatchers.IO) {
        if (!isConnected()) {
            return@withContext CommandResult(
                success = false,
                output = "",
                error = "Session is not connected",
                exitCode = -1
            )
        }

        var session: Session? = null
        try {
            session = client.startSession()
            val cmd = session.exec(command)

            // Read output
            val outputStream = ByteArrayOutputStream()
            val errorStream = ByteArrayOutputStream()

            cmd.inputStream.copyTo(outputStream)
            cmd.errorStream.copyTo(errorStream)

            // Wait for command to complete
            cmd.join(COMMAND_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)

            val exitCode = cmd.exitStatus ?: -1
            val output = outputStream.toString(Charsets.UTF_8.name())
            val error = errorStream.toString(Charsets.UTF_8.name())

            CommandResult(
                success = exitCode == 0,
                output = output,
                error = error,
                exitCode = exitCode
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                output = "",
                error = e.message ?: "Command execution failed",
                exitCode = -1
            )
        } finally {
            session?.close()
        }
    }

    /**
     * Starts an interactive shell session.
     *
     * @return Session object for shell interaction
     */
    suspend fun startShell(): Session = withContext(Dispatchers.IO) {
        if (shellSession != null && shellSession?.isOpen == true) {
            return@withContext shellSession!!
        }

        val session = client.startSession()
        session.allocateDefaultPTY()
        shellSession = session
        shellChannel = session.startShell()
        session
    }

    /**
     * Sends input to the interactive shell.
     *
     * @param input Command or input to send
     */
    suspend fun sendToShell(input: String) = withContext(Dispatchers.IO) {
        val channel = shellChannel ?: throw IllegalStateException("Shell not started")
        channel.outputStream.write((input + "\n").toByteArray(Charsets.UTF_8))
        channel.outputStream.flush()
    }

    /**
     * Reads output from the shell.
     *
     * @param maxBytes Maximum bytes to read
     * @return Output string
     */
    suspend fun readFromShell(maxBytes: Int = 8192): String = withContext(Dispatchers.IO) {
        val channel = shellChannel ?: throw IllegalStateException("Shell not started")
        val buffer = ByteArray(maxBytes)
        val bytesRead = channel.inputStream.read(buffer)
        if (bytesRead > 0) {
            String(buffer, 0, bytesRead, Charsets.UTF_8)
        } else {
            ""
        }
    }

    /**
     * Closes the SSH session.
     */
    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            shellChannel?.close()
            shellChannel = null
            shellSession?.close()
            shellSession = null
            client.disconnect()
            isActive = false
        } catch (e: Exception) {
            // Ignore errors during disconnect
        }
    }
}
