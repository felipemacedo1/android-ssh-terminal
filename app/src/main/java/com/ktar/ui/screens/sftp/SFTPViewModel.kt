package com.ktar.ui.screens.sftp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ktar.core.sftp.SFTPFile
import com.ktar.core.sftp.SFTPManager
import com.ktar.core.sftp.SFTPProgressListener
import com.ktar.core.sftp.SFTPResult
import com.ktar.ssh.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for SFTP operations.
 * Manages file listing, upload, and download operations.
 */
class SFTPViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow<SFTPState>(SFTPState.Idle)
    val state: StateFlow<SFTPState> = _state.asStateFlow()

    private val _currentPath = MutableStateFlow("/")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val _files = MutableStateFlow<List<SFTPFile>>(emptyList())
    val files: StateFlow<List<SFTPFile>> = _files.asStateFlow()

    private var sftpManager: SFTPManager? = null
    private var sessionId: String? = null

    companion object {
        private const val TAG = "SFTPViewModel"
    }

    /**
     * Initializes SFTP connection for a given session.
     */
    fun initialize(sessionId: String) {
        if (this.sessionId == sessionId && sftpManager != null) {
            return // Already initialized
        }

        this.sessionId = sessionId
        viewModelScope.launch {
            try {
                val session = sessionManager.getSession(sessionId)
                if (session == null) {
                    _state.value = SFTPState.Error("SSH session not found")
                    return@launch
                }

                val manager = SFTPManager(session)
                val result = manager.connect()
                
                if (result.isSuccess) {
                    sftpManager = manager
                    Log.d(TAG, "SFTP initialized for session $sessionId")
                    listRemoteFiles(_currentPath.value)
                } else {
                    _state.value = SFTPState.Error("Failed to connect SFTP: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize SFTP", e)
                _state.value = SFTPState.Error("Failed to initialize SFTP: ${e.message}")
            }
        }
    }

    /**
     * Lists files in the specified remote path.
     */
    fun listRemoteFiles(path: String) {
        viewModelScope.launch {
            try {
                _state.value = SFTPState.Loading
                val manager = sftpManager ?: run {
                    _state.value = SFTPState.Error("SFTP not initialized")
                    return@launch
                }

                val result = manager.listFiles(path)
                if (result.isSuccess) {
                    val fileList = result.getOrNull() ?: emptyList()
                    _files.value = fileList
                    _currentPath.value = path
                    _state.value = SFTPState.Listing(fileList)
                    Log.d(TAG, "Listed ${fileList.size} files in $path")
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    _state.value = SFTPState.Error("Failed to list files: $error")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error listing files", e)
                _state.value = SFTPState.Error("Error listing files: ${e.message}")
            }
        }
    }

    /**
     * Uploads a file to the remote server.
     */
    fun uploadFile(localPath: String, remotePath: String) {
        viewModelScope.launch {
            try {
                _state.value = SFTPState.Loading
                val manager = sftpManager ?: run {
                    _state.value = SFTPState.Error("SFTP not initialized")
                    return@launch
                }

                val progressListener = SFTPProgressListener { transferred, total ->
                    val percent = if (total > 0) ((transferred * 100) / total).toInt() else 0
                    _state.value = SFTPState.Progress(percent, transferred, total)
                }

                val result = manager.uploadFile(localPath, remotePath, progressListener)
                when (result) {
                    is SFTPResult.Success -> {
                        _state.value = SFTPState.Success(result.message)
                        // Refresh file list
                        listRemoteFiles(_currentPath.value)
                    }
                    is SFTPResult.Error -> {
                        _state.value = SFTPState.Error(result.error)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading file", e)
                _state.value = SFTPState.Error("Upload failed: ${e.message}")
            }
        }
    }

    /**
     * Downloads a file from the remote server.
     */
    fun downloadFile(remotePath: String, localPath: String) {
        viewModelScope.launch {
            try {
                _state.value = SFTPState.Loading
                val manager = sftpManager ?: run {
                    _state.value = SFTPState.Error("SFTP not initialized")
                    return@launch
                }

                val progressListener = SFTPProgressListener { transferred, total ->
                    val percent = if (total > 0) ((transferred * 100) / total).toInt() else 0
                    _state.value = SFTPState.Progress(percent, transferred, total)
                }

                val result = manager.downloadFile(remotePath, localPath, progressListener)
                when (result) {
                    is SFTPResult.Success -> {
                        _state.value = SFTPState.Success(result.message)
                    }
                    is SFTPResult.Error -> {
                        _state.value = SFTPState.Error(result.error)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading file", e)
                _state.value = SFTPState.Error("Download failed: ${e.message}")
            }
        }
    }

    /**
     * Navigates to parent directory.
     */
    fun navigateUp() {
        val current = _currentPath.value
        if (current != "/" && current.isNotEmpty()) {
            val parent = current.substringBeforeLast('/', "/")
            listRemoteFiles(if (parent.isEmpty()) "/" else parent)
        }
    }

    /**
     * Clears the current state message.
     */
    fun clearState() {
        if (_state.value is SFTPState.Success || _state.value is SFTPState.Error) {
            _state.value = SFTPState.Idle
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            sftpManager?.disconnect()
            sftpManager = null
        }
    }
}

/**
 * Represents the state of SFTP operations.
 */
sealed class SFTPState {
    object Idle : SFTPState()
    object Loading : SFTPState()
    data class Success(val message: String) : SFTPState()
    data class Error(val error: String) : SFTPState()
    data class Listing(val files: List<SFTPFile>) : SFTPState()
    data class Progress(val percent: Int, val bytesTransferred: Long, val totalBytes: Long) : SFTPState()
}
