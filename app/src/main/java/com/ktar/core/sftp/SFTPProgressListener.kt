package com.ktar.core.sftp

/**
 * Listener for SFTP transfer progress.
 */
fun interface SFTPProgressListener {
    /**
     * Called when transfer progress is updated.
     * @param transferred Bytes transferred so far
     * @param total Total bytes to transfer
     */
    fun onProgress(transferred: Long, total: Long)
}
