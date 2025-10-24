package com.ktar.core.sftp

/**
 * Represents a remote file or directory in SFTP.
 */
data class SFTPFile(
    val name: String,
    val path: String,
    val size: Long,
    val isDirectory: Boolean,
    val lastModified: Long,
    val permissions: String
)
