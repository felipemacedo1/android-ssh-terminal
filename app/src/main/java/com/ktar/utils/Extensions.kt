package com.ktar.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Extension functions for common operations.
 */

/**
 * Formats a Date to a human-readable string.
 */
fun Date.toFormattedString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(this)
}

/**
 * Formats milliseconds since epoch to a human-readable string.
 */
fun Long.toFormattedDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    return Date(this).toFormattedString(pattern)
}

/**
 * Formats milliseconds since epoch to a relative time string (e.g., "2 hours ago").
 */
fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    
    return when {
        diff < 60_000 -> "Agora mesmo"
        diff < 3_600_000 -> "${diff / 60_000} min atrás"
        diff < 86_400_000 -> "${diff / 3_600_000}h atrás"
        diff < 604_800_000 -> "${diff / 86_400_000}d atrás"
        else -> this.toFormattedDate("dd/MM/yyyy")
    }
}

/**
 * Checks if a string is a valid hostname or IP address.
 */
fun String.isValidHost(): Boolean {
    if (this.isBlank()) return false
    
    // Check if it's a valid IP address
    val ipRegex = Regex(
        "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"
    )
    if (ipRegex.matches(this)) return true
    
    // Check if it's a valid hostname
    val hostnameRegex = Regex(
        "^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$"
    )
    return hostnameRegex.matches(this) || this == "localhost"
}

/**
 * Validates port number.
 */
fun String.isValidPort(): Boolean {
    val port = this.toIntOrNull() ?: return false
    return port in 1..65535
}

/**
 * Truncates a string to a maximum length and adds ellipsis if needed.
 */
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    return if (this.length <= maxLength) {
        this
    } else {
        this.substring(0, maxLength - ellipsis.length) + ellipsis
    }
}

/**
 * Masks a password/secret for logging purposes.
 */
fun String.maskSecret(): String {
    return if (this.isEmpty()) {
        "[empty]"
    } else if (this.length <= 4) {
        "****"
    } else {
        "${this.substring(0, 2)}${"*".repeat(this.length - 4)}${this.substring(this.length - 2)}"
    }
}
