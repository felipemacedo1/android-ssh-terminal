package com.ktar.core.sftp

/**
 * Result of an SFTP operation.
 */
sealed class SFTPResult {
    data class Success(val message: String) : SFTPResult()
    data class Error(val error: String) : SFTPResult()
    data class Progress(val percent: Int, val bytesTransferred: Long, val totalBytes: Long) : SFTPResult()
}
