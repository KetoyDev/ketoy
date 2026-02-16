package com.developerstring.ketoy.devtools

import android.content.Context
import android.content.SharedPreferences

/**
 * Stores recent dev server connections for quick reconnection.
 * Uses SharedPreferences for persistence.
 */
class KetoyDevStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "ketoy_devtools", Context.MODE_PRIVATE
    )

    /**
     * Save a recent connection URL.
     */
    fun saveRecentConnection(url: String) {
        val recent = getRecentConnections().toMutableList()
        recent.remove(url)
        recent.add(0, url)
        // Keep only last 5
        val trimmed = recent.take(5)
        prefs.edit()
            .putStringSet("recent_connections", trimmed.toSet())
            .putString("last_connection", url)
            .apply()
    }

    /**
     * Get the last successful connection URL.
     */
    fun getLastConnection(): String? {
        return prefs.getString("last_connection", null)
    }

    /**
     * Get all recent connection URLs.
     */
    fun getRecentConnections(): List<String> {
        return prefs.getStringSet("recent_connections", emptySet())?.toList() ?: emptyList()
    }

    /**
     * Clear all stored connections.
     */
    fun clear() {
        prefs.edit().clear().apply()
    }
}
