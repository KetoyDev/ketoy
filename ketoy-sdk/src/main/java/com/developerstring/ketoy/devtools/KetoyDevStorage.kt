package com.developerstring.ketoy.devtools

import android.content.Context
import android.content.SharedPreferences

/**
 * Persistent store for recent Ketoy Dev Server connections.
 *
 * `KetoyDevStorage` persists the most recent server URLs using
 * [SharedPreferences] so that developers can quickly reconnect
 * to familiar servers without re-typing addresses.
 *
 * ## Usage
 * ```kotlin
 * val storage = KetoyDevStorage(context)
 *
 * // Save after a successful connection:
 * storage.saveRecentConnection("192.168.1.5:8484")
 *
 * // Retrieve for a "quick-connect" list:
 * val recents: List<String> = storage.getRecentConnections()
 *
 * // Pre-fill the URL field with the last-used server:
 * val last: String? = storage.getLastConnection()
 * ```
 *
 * ## Storage details
 * - Preferences file: `ketoy_devtools` (private mode).
 * - A maximum of **5** recent connections are retained (FIFO).
 * - [saveRecentConnection] moves a duplicate URL to the top of the
 *   list rather than creating a second entry.
 *
 * @param context Android [Context] used to obtain [SharedPreferences].
 *
 * @see KetoyDevConnectScreen
 * @see KetoyDevClient
 */
class KetoyDevStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "ketoy_devtools", Context.MODE_PRIVATE
    )

    /**
     * Record a connection URL as the most-recent entry.
     *
     * If the URL already exists in the list it is moved to the front
     * rather than duplicated. Only the five most recent entries are kept.
     *
     * @param url The `host:port` connection string to save.
     */
    fun saveRecentConnection(url: String) {
        val recent = getRecentConnections().toMutableList()
        recent.remove(url)
        recent.add(0, url)
        val trimmed = recent.take(5)
        prefs.edit()
            .putStringSet("recent_connections", trimmed.toSet())
            .putString("last_connection", url)
            .apply()
    }

    /**
     * Returns the URL of the last **successfully saved** connection,
     * or `null` if no connection has been recorded yet.
     */
    fun getLastConnection(): String? {
        return prefs.getString("last_connection", null)
    }

    /**
     * Returns all recently saved connection URLs, ordered newest-first.
     * The list contains at most five entries.
     */
    fun getRecentConnections(): List<String> {
        return prefs.getStringSet("recent_connections", emptySet())?.toList() ?: emptyList()
    }

    /**
     * Remove **all** stored connection data.
     */
    fun clear() {
        prefs.edit().clear().apply()
    }
}
