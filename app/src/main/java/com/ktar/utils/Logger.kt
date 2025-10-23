package com.ktar.utils

import android.util.Log

/**
 * Centralized logging utility with support for debug and release builds.
 */
object Logger {
    
    private const val TAG = Constants.LOG_TAG
    
    // Simple flag to control debug logging (set to false for release)
    private const val DEBUG_ENABLED = true
    
    /**
     * Log debug message (only in debug builds).
     */
    fun d(tag: String = TAG, message: String) {
        if (DEBUG_ENABLED) {
            Log.d(tag, message)
        }
    }
    
    /**
     * Log info message.
     */
    fun i(tag: String = TAG, message: String) {
        Log.i(tag, message)
    }
    
    /**
     * Log warning message.
     */
    fun w(tag: String = TAG, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.w(tag, message, throwable)
        } else {
            Log.w(tag, message)
        }
    }
    
    /**
     * Log error message.
     */
    fun e(tag: String = TAG, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
    
    /**
     * Log connection event.
     */
    fun logConnection(host: String, port: Int, username: String, success: Boolean) {
        val status = if (success) "SUCCESS" else "FAILED"
        i("SSH_CONNECTION", "Connection to $username@$host:$port - $status")
    }
    
    /**
     * Log command execution.
     */
    fun logCommand(command: String, exitCode: Int, duration: Long) {
        d("SSH_COMMAND", "Executed '$command' (exit: $exitCode, duration: ${duration}ms)")
    }
    
    /**
     * Log security event.
     */
    fun logSecurity(event: String, details: String? = null) {
        i("SECURITY", "Event: $event ${details?.let { "- $it" } ?: ""}")
    }
}
