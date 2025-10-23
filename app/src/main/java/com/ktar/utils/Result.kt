package com.ktar.utils

/**
 * Sealed class representing the result of an operation.
 * Provides better error handling than throwing exceptions.
 */
sealed class Result<out T> {
    /**
     * Successful operation with data.
     */
    data class Success<T>(val data: T) : Result<T>()
    
    /**
     * Failed operation with error information.
     */
    data class Error(
        val message: String,
        val exception: Throwable? = null,
        val code: ErrorCode = ErrorCode.UNKNOWN
    ) : Result<Nothing>()
    
    /**
     * Operation in progress.
     */
    object Loading : Result<Nothing>()
    
    /**
     * Returns true if the result is Success.
     */
    fun isSuccess(): Boolean = this is Success
    
    /**
     * Returns true if the result is Error.
     */
    fun isError(): Boolean = this is Error
    
    /**
     * Returns true if the result is Loading.
     */
    fun isLoading(): Boolean = this is Loading
    
    /**
     * Returns the data if Success, null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    /**
     * Returns the error message if Error, null otherwise.
     */
    fun errorMessage(): String? = when (this) {
        is Error -> message
        else -> null
    }
}

/**
 * Error codes for better error classification.
 */
enum class ErrorCode {
    // Network errors
    CONNECTION_FAILED,
    CONNECTION_TIMEOUT,
    HOST_UNREACHABLE,
    
    // Authentication errors
    AUTH_FAILED,
    INVALID_CREDENTIALS,
    KEY_PARSE_ERROR,
    
    // SSH errors
    SESSION_CLOSED,
    COMMAND_TIMEOUT,
    COMMAND_FAILED,
    
    // Data errors
    VALIDATION_ERROR,
    ENCRYPTION_ERROR,
    DECRYPTION_ERROR,
    
    // Storage errors
    STORAGE_ERROR,
    HOST_NOT_FOUND,
    
    // Generic
    UNKNOWN
}

/**
 * Extension function to handle Result with callbacks.
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(data)
    }
    return this
}

/**
 * Extension function to handle errors.
 */
inline fun <T> Result<T>.onError(action: (String, ErrorCode, Throwable?) -> Unit): Result<T> {
    if (this is Result.Error) {
        action(message, code, exception)
    }
    return this
}

/**
 * Extension function to handle loading state.
 */
inline fun <T> Result<T>.onLoading(action: () -> Unit): Result<T> {
    if (this is Result.Loading) {
        action()
    }
    return this
}
