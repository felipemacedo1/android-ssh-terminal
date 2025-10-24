package com.ktar.ui.screens.terminal

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ktar.data.model.CommandResult
import com.ktar.ssh.SSHManager
import com.ktar.ssh.SSHSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel for the terminal screen.
 * Supports both standard command execution and PTY (interactive) mode.
 */
class TerminalViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TerminalUiState())
    val uiState: StateFlow<TerminalUiState> = _uiState.asStateFlow()

    private var sshSession: SSHSession? = null
    private val sshManager = SSHManager()

    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    companion object {
        private const val TAG = "TerminalViewModel"
    }

    /**
     * Sets the active SSH session.
     */
    fun setSession(session: SSHSession) {
        sshSession = session
        addSystemMessage("Conectado a ${session.host.host}:${session.host.port} como ${session.host.username}")
        addSystemMessage("Digite 'exit' para desconectar")
    }

    /**
     * Toggles PTY (interactive) mode.
     */
    fun togglePTYMode() {
        _uiState.update { 
            val newValue = !it.ptyEnabled
            Log.d(TAG, "PTY mode toggled: $newValue")
            it.copy(ptyEnabled = newValue) 
        }
        
        if (_uiState.value.ptyEnabled) {
            addSystemMessage("⚙️ Modo interativo (PTY) ativado - comandos como vi, top, nano funcionarão")
        } else {
            addSystemMessage("⚙️ Modo padrão ativado - execução não interativa")
        }
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

        // Check if command requires PTY and warn user
        if (!_uiState.value.ptyEnabled && session.isInteractiveCommand(command)) {
            addSystemMessage("⚠️ O comando '$command' pode requerer modo interativo (PTY)")
            addSystemMessage("   Ative o modo interativo no menu se o comando não funcionar corretamente")
        }

        // Execute command
        _uiState.update { it.copy(isExecuting = true) }

        viewModelScope.launch {
            try {
                val usePTY = _uiState.value.ptyEnabled
                
                if (usePTY) {
                    Log.d("SSH_PTY", "Executing command with PTY: $command")
                } else {
                    Log.d("SSH_EXEC", "Executing command: $command")
                }
                
                val result = sshManager.executeCommand(session, command, usePTY)
                
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
                Log.e(TAG, "Error executing command", e)
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

    internal fun addErrorMessage(error: String) {
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
    val prompt: String = "$ ",
    val ptyEnabled: Boolean = false  // PTY (interactive) mode enabled
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
