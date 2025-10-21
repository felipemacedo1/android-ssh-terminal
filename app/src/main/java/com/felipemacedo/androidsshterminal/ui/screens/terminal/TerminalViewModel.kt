package com.felipemacedo.androidsshterminal.ui.screens.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felipemacedo.androidsshterminal.data.model.CommandResult
import com.felipemacedo.androidsshterminal.ssh.SSHManager
import com.felipemacedo.androidsshterminal.ssh.SSHSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel for the terminal screen.
 */
class TerminalViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TerminalUiState())
    val uiState: StateFlow<TerminalUiState> = _uiState.asStateFlow()

    private var sshSession: SSHSession? = null
    private val sshManager = SSHManager()

    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    /**
     * Sets the active SSH session.
     */
    fun setSession(session: SSHSession) {
        sshSession = session
        addSystemMessage("Conectado a ${session.host.host}:${session.host.port} como ${session.host.username}")
        addSystemMessage("Digite 'exit' para desconectar")
    }

    /**
     * Updates the current command input.
     */
    fun updateCommand(command: String) {
        _uiState.update { it.copy(currentCommand = command) }
    }

    /**
     * Executes the current command.
     */
    fun executeCommand() {
        val command = _uiState.value.currentCommand.trim()
        if (command.isEmpty()) return

        val session = sshSession
        if (session == null) {
            addErrorMessage("Sessão SSH não conectada")
            return
        }

        // Handle exit command
        if (command.equals("exit", ignoreCase = true)) {
            disconnect()
            return
        }

        // Clear input
        _uiState.update { it.copy(currentCommand = "") }

        // Add command to output
        addCommandMessage(command)

        // Execute command
        _uiState.update { it.copy(isExecuting = true) }

        viewModelScope.launch {
            try {
                val result = sshManager.executeCommand(session, command)
                
                if (result.success) {
                    if (result.output.isNotEmpty()) {
                        addOutputMessage(result.output)
                    }
                    if (result.error.isNotEmpty()) {
                        addErrorMessage(result.error)
                    }
                } else {
                    addErrorMessage(result.error.ifEmpty { "Comando falhou com código ${result.exitCode}" })
                }
            } catch (e: Exception) {
                addErrorMessage("Erro ao executar comando: ${e.message}")
            } finally {
                _uiState.update { it.copy(isExecuting = false) }
            }
        }
    }

    /**
     * Disconnects the SSH session.
     */
    fun disconnect() {
        viewModelScope.launch {
            try {
                sshSession?.let { session ->
                    sshManager.closeSession(session)
                    addSystemMessage("Desconectado de ${session.host.host}")
                }
            } catch (e: Exception) {
                addErrorMessage("Erro ao desconectar: ${e.message}")
            } finally {
                sshSession = null
                _uiState.update { it.copy(isConnected = false) }
            }
        }
    }

    /**
     * Clears the terminal output.
     */
    fun clearTerminal() {
        _uiState.update { it.copy(outputLines = emptyList()) }
        addSystemMessage("Terminal limpo")
    }

    private fun addCommandMessage(command: String) {
        val timestamp = dateFormat.format(Date())
        val prefix = "${_uiState.value.prompt}$command"
        _uiState.update {
            it.copy(outputLines = it.outputLines + TerminalLine(prefix, TerminalLineType.COMMAND, timestamp))
        }
    }

    private fun addOutputMessage(output: String) {
        val timestamp = dateFormat.format(Date())
        output.lines().forEach { line ->
            _uiState.update {
                it.copy(outputLines = it.outputLines + TerminalLine(line, TerminalLineType.OUTPUT, timestamp))
            }
        }
    }

    private fun addErrorMessage(error: String) {
        val timestamp = dateFormat.format(Date())
        error.lines().forEach { line ->
            _uiState.update {
                it.copy(outputLines = it.outputLines + TerminalLine(line, TerminalLineType.ERROR, timestamp))
            }
        }
    }

    private fun addSystemMessage(message: String) {
        val timestamp = dateFormat.format(Date())
        _uiState.update {
            it.copy(outputLines = it.outputLines + TerminalLine(message, TerminalLineType.SYSTEM, timestamp))
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            sshSession?.let { sshManager.closeSession(it) }
        }
    }
}

/**
 * UI state for the terminal screen.
 */
data class TerminalUiState(
    val outputLines: List<TerminalLine> = emptyList(),
    val currentCommand: String = "",
    val isExecuting: Boolean = false,
    val isConnected: Boolean = true,
    val prompt: String = "$ "
)

/**
 * Represents a line in the terminal output.
 */
data class TerminalLine(
    val text: String,
    val type: TerminalLineType,
    val timestamp: String
)

/**
 * Type of terminal line.
 */
enum class TerminalLineType {
    COMMAND,    // User command
    OUTPUT,     // Command output
    ERROR,      // Error output
    SYSTEM      // System message
}
