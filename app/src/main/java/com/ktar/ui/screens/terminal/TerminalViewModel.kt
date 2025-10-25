package com.ktar.ui.screens.terminal

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ktar.data.model.CommandResult
import com.ktar.ssh.SSHManager
import com.ktar.ssh.SSHSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel for the terminal screen.
 * Supports both standard command execution (exec) and persistent shell mode (PTY).
 * 
 * v1.4.0: Added persistent shell with real-time streaming output.
 */
class TerminalViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TerminalUiState())
    val uiState: StateFlow<TerminalUiState> = _uiState.asStateFlow()

    private var sshSession: SSHSession? = null
    private val sshManager = SSHManager()

    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    // Persistent shell support
    private var outputPollingJob: Job? = null
    private var pollInterval = 100L // Adaptive polling interval

    companion object {
        private const val TAG = "TerminalViewModel"
        private const val MAX_OUTPUT_LINES = 10000
        private const val MIN_POLL_INTERVAL = 50L
        private const val MAX_POLL_INTERVAL = 500L
    }

    /**
     * Sets the active SSH session and starts persistent interactive shell.
     */
    fun setSession(session: SSHSession, useShellMode: Boolean = true) {
        sshSession = session
        
        val connectionStartTime = System.currentTimeMillis()
        
        if (useShellMode) {
            // Start persistent shell with PTY
            viewModelScope.launch {
                try {
                    session.startShell()
                    Log.d(TAG, "Persistent interactive shell started with PTY")
                    
                    // Wait for shell initialization
                    delay(500)
                    
                    // Start output polling
                    startOutputPolling()
                    
                    _uiState.update { 
                        it.copy(
                            isConnected = true, 
                            shellMode = true,
                            ptyEnabled = true,
                            hostName = "${session.host.username}@${session.host.host}:${session.host.port}",
                            connectionTime = connectionStartTime
                        ) 
                    }
                    
                    addSystemMessage("✅ Conectado a ${session.host.host}:${session.host.port} como ${session.host.username}")
                    addSystemMessage("🔧 Shell interativo ativo - Terminal PTY habilitado")
                    addSystemMessage("💡 Digite 'exit' para desconectar")
                    
                    // Read and display initial output (banner, MOTD)
                    delay(200)
                    readInitialOutput()
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting shell", e)
                    addErrorMessage("❌ Erro ao iniciar shell interativo: ${e.message}")
                    addSystemMessage("⚠️ Revertendo para modo exec padrão")
                    _uiState.update { it.copy(isConnected = true, shellMode = false) }
                }
            }
        } else {
            // Legacy exec mode
            _uiState.update { 
                it.copy(
                    isConnected = true, 
                    shellMode = false,
                    hostName = "${session.host.username}@${session.host.host}:${session.host.port}",
                    connectionTime = connectionStartTime
                ) 
            }
            addSystemMessage("Conectado a ${session.host.host}:${session.host.port} como ${session.host.username}")
            addSystemMessage("Digite 'exit' para desconectar")
        }
    }
    
    /**
     * Starts continuous polling of shell output with adaptive interval.
     */
    private fun startOutputPolling() {
        outputPollingJob?.cancel()
        
        outputPollingJob = viewModelScope.launch {
            var emptyReadCount = 0
            
            while (isActive) {
                try {
                    val output = sshSession?.readFromShell()
                    
                    if (!output.isNullOrEmpty()) {
                        addOutputMessage(output)
                        emptyReadCount = 0
                        
                        // Speed up polling when active
                        pollInterval = maxOf(MIN_POLL_INTERVAL, pollInterval - 10)
                        Log.v("SSH_POLL", "Active output - interval: ${pollInterval}ms, bytes: ${output.length}")
                        
                        // Update poll interval in UI state
                        _uiState.update { it.copy(currentPollInterval = pollInterval) }
                    } else {
                        emptyReadCount++
                        
                        // Slow down polling when idle
                        if (emptyReadCount > 5) {
                            pollInterval = minOf(MAX_POLL_INTERVAL, pollInterval + 20)
                            _uiState.update { it.copy(currentPollInterval = pollInterval) }
                        }
                    }
                    
                    delay(pollInterval)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading shell output", e)
                    delay(1000) // Back off on error
                }
            }
        }
        
        Log.d(TAG, "Output polling started")
    }
    
    /**
     * Stops output polling.
     */
    private fun stopOutputPolling() {
        outputPollingJob?.cancel()
        outputPollingJob = null
        Log.d(TAG, "Output polling stopped")
    }
    
    /**
     * Reads and displays initial shell output (banner, MOTD).
     */
    private suspend fun readInitialOutput() {
        try {
            val initialOutput = sshSession?.readFromShell(maxBytes = 16384)
            if (!initialOutput.isNullOrEmpty()) {
                addOutputMessage(initialOutput)
                Log.d(TAG, "Initial output read: ${initialOutput.length} bytes")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error reading initial output", e)
        }
    }

    /**
     * Toggles PTY (interactive) mode.
     * In shell mode, this has no effect as PTY is always enabled.
     */
    fun togglePTYMode() {
        if (_uiState.value.shellMode) {
            addSystemMessage("ℹ️ Shell interativo está sempre com PTY habilitado")
            return
        }
        
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
     * Uses persistent shell if in shell mode, otherwise falls back to exec.
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
        
        // Handle clear command
        if (command.equals("clear", ignoreCase = true)) {
            clearTerminal()
            _uiState.update { it.copy(currentCommand = "") }
            return
        }

        // Clear input
        _uiState.update { it.copy(currentCommand = "") }

        if (_uiState.value.shellMode) {
            // Shell mode: send to persistent shell
            executeCommandInShell(session, command)
        } else {
            // Exec mode: traditional one-shot execution
            executeCommandWithExec(session, command)
        }
    }
    
    /**
     * Executes command by sending to persistent shell.
     */
    private fun executeCommandInShell(session: SSHSession, command: String) {
        // Add command to output (local echo)
        addCommandMessage(command)
        
        viewModelScope.launch {
            try {
                session.sendToShell(command)
                Log.d("SSH_CMD", "Command sent to shell: $command")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending command to shell", e)
                addErrorMessage("❌ Erro ao enviar comando: ${e.message}")
            }
        }
    }
    
    /**
     * Executes command using traditional exec (legacy mode).
     */
    private fun executeCommandWithExec(session: SSHSession, command: String) {
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
     * Disconnects the SSH session and stops polling.
     */
    fun disconnect() {
        stopOutputPolling()
        
        viewModelScope.launch {
            try {
                sshSession?.let { session ->
                    sshManager.closeSession(session)
                    addSystemMessage("👋 Desconectado de ${session.host.host}")
                }
            } catch (e: Exception) {
                addErrorMessage("Erro ao desconectar: ${e.message}")
            } finally {
                sshSession = null
                _uiState.update { 
                    it.copy(
                        isConnected = false,
                        shellMode = false,
                        ptyEnabled = false,
                        hostName = "",
                        connectionTime = 0L,
                        currentPollInterval = 100L
                    ) 
                }
            }
        }
    }

    /**
     * Clears the terminal output.
     */
    fun clearTerminal() {
        _uiState.update { it.copy(outputLines = emptyList()) }
        addSystemMessage("🧹 Terminal limpo")
    }

    private fun addCommandMessage(command: String) {
        val timestamp = dateFormat.format(Date())
        val prefix = "${_uiState.value.prompt}$command"
        addOutputLine(TerminalLine(prefix, TerminalLineType.COMMAND, timestamp))
    }

    private fun addOutputMessage(output: String) {
        val timestamp = dateFormat.format(Date())
        output.lines().forEach { line ->
            addOutputLine(TerminalLine(line, TerminalLineType.OUTPUT, timestamp))
        }
    }

    internal fun addErrorMessage(error: String) {
        val timestamp = dateFormat.format(Date())
        error.lines().forEach { line ->
            addOutputLine(TerminalLine(line, TerminalLineType.ERROR, timestamp))
        }
    }

    private fun addSystemMessage(message: String) {
        val timestamp = dateFormat.format(Date())
        addOutputLine(TerminalLine(message, TerminalLineType.SYSTEM, timestamp))
    }
    
    /**
     * Adds output line with buffer management.
     */
    private fun addOutputLine(line: TerminalLine) {
        _uiState.update { state ->
            val newLines = state.outputLines + line
            
            // Limit buffer to prevent memory issues
            val trimmedLines = if (newLines.size > MAX_OUTPUT_LINES) {
                Log.d(TAG, "Buffer limit reached, trimming to $MAX_OUTPUT_LINES lines")
                newLines.takeLast(MAX_OUTPUT_LINES)
            } else {
                newLines
            }
            
            state.copy(
                outputLines = trimmedLines,
                bufferUsage = trimmedLines.size
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopOutputPolling()
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
    val ptyEnabled: Boolean = false,  // PTY (interactive) mode enabled (for exec mode)
    val shellMode: Boolean = false,   // Persistent shell mode (PTY always enabled)
    val hostName: String = "",        // Connected host name
    val connectionTime: Long = 0L,    // Connection start time (millis)
    val currentPollInterval: Long = 100L, // Current polling interval (ms)
    val bufferUsage: Int = 0          // Current buffer usage (number of lines)
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
