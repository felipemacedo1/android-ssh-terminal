package com.ktar.data.model

/**
 * Represents the result of an SSH command execution.
 *
 * @property success Whether the command executed successfully
 * @property output Command output (stdout)
 * @property error Error output (stderr)
 * @property exitCode Exit code of the command
 */
data class CommandResult(
    val success: Boolean,
    val output: String,
    val error: String = "",
    val exitCode: Int = 0
)
