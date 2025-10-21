package com.felipemacedo.androidsshterminal.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.felipemacedo.androidsshterminal.data.model.AuthMethod
import com.felipemacedo.androidsshterminal.data.model.Host
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repository for managing SSH host configurations using DataStore.
 */
class HostDataStore(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hosts")
        private val HOSTS_KEY = stringPreferencesKey("saved_hosts")
    }

    /**
     * Gets all saved hosts as a Flow.
     */
    val hostsFlow: Flow<List<Host>> = context.dataStore.data.map { preferences ->
        val hostsJson = preferences[HOSTS_KEY] ?: "[]"
        parseHosts(hostsJson)
    }

    /**
     * Saves a new host or updates an existing one.
     *
     * @param host Host configuration to save
     */
    suspend fun saveHost(host: Host) {
        context.dataStore.edit { preferences ->
            val currentHosts = preferences[HOSTS_KEY]?.let { parseHosts(it) } ?: emptyList()
            val updatedHosts = currentHosts.filterNot { it.id == host.id } + host
            preferences[HOSTS_KEY] = hostsToJson(updatedHosts)
        }
    }

    /**
     * Deletes a host by ID.
     *
     * @param hostId ID of the host to delete
     */
    suspend fun deleteHost(hostId: String) {
        context.dataStore.edit { preferences ->
            val currentHosts = preferences[HOSTS_KEY]?.let { parseHosts(it) } ?: emptyList()
            val updatedHosts = currentHosts.filterNot { it.id == hostId }
            preferences[HOSTS_KEY] = hostsToJson(updatedHosts)
        }
    }

    /**
     * Updates the last used timestamp for a host.
     *
     * @param hostId ID of the host
     */
    suspend fun updateLastUsed(hostId: String) {
        context.dataStore.edit { preferences ->
            val currentHosts = preferences[HOSTS_KEY]?.let { parseHosts(it) } ?: emptyList()
            val updatedHosts = currentHosts.map { host ->
                if (host.id == hostId) {
                    host.copy(lastUsed = System.currentTimeMillis())
                } else {
                    host
                }
            }
            preferences[HOSTS_KEY] = hostsToJson(updatedHosts)
        }
    }

    /**
     * Gets a specific host by ID.
     *
     * @param hostId ID of the host
     * @return Host or null if not found
     */
    suspend fun getHost(hostId: String): Host? {
        var result: Host? = null
        context.dataStore.data.map { preferences ->
            val hosts = preferences[HOSTS_KEY]?.let { parseHosts(it) } ?: emptyList()
            result = hosts.find { it.id == hostId }
        }
        return result
    }

    /**
     * Clears all saved hosts.
     */
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.remove(HOSTS_KEY)
        }
    }

    /**
     * Parses JSON string to list of Hosts.
     */
    private fun parseHosts(json: String): List<Host> {
        return try {
            val jsonArray = JSONArray(json)
            List(jsonArray.length()) { index ->
                val obj = jsonArray.getJSONObject(index)
                Host(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    host = obj.getString("host"),
                    port = obj.getInt("port"),
                    username = obj.getString("username"),
                    authMethod = AuthMethod.valueOf(obj.getString("authMethod")),
                    password = obj.optString("password").ifEmpty { null },
                    privateKey = obj.optString("privateKey").ifEmpty { null },
                    createdAt = obj.getLong("createdAt"),
                    lastUsed = if (obj.has("lastUsed")) obj.getLong("lastUsed") else null
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Converts list of Hosts to JSON string.
     */
    private fun hostsToJson(hosts: List<Host>): String {
        val jsonArray = JSONArray()
        hosts.forEach { host ->
            val obj = JSONObject().apply {
                put("id", host.id)
                put("name", host.name)
                put("host", host.host)
                put("port", host.port)
                put("username", host.username)
                put("authMethod", host.authMethod.name)
                put("password", host.password ?: "")
                put("privateKey", host.privateKey ?: "")
                put("createdAt", host.createdAt)
                host.lastUsed?.let { put("lastUsed", it) }
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }
}
