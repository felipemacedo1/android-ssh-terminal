package com.ktar.ssh

import java.util.concurrent.ConcurrentHashMap

/**
 * Manages active SSH sessions.
 * Provides a centralized way to store and retrieve sessions by ID.
 */
object SessionManager {
    
    private val sessions = ConcurrentHashMap<String, SSHSession>()
    
    /**
     * Stores a session with the given ID.
     */
    fun storeSession(sessionId: String, session: SSHSession) {
        sessions[sessionId] = session
    }
    
    /**
     * Retrieves a session by ID.
     */
    fun getSession(sessionId: String): SSHSession? {
        return sessions[sessionId]
    }
    
    /**
     * Removes a session by ID.
     */
    fun removeSession(sessionId: String): SSHSession? {
        return sessions.remove(sessionId)
    }
    
    /**
     * Removes all sessions.
     */
    suspend fun clearAll() {
        sessions.values.forEach { session ->
            try {
                session.disconnect()
            } catch (e: Exception) {
                // Ignore errors on cleanup
            }
        }
        sessions.clear()
    }
    
    /**
     * Gets all active session IDs.
     */
    fun getActiveSessionIds(): Set<String> {
        return sessions.keys.toSet()
    }
}
