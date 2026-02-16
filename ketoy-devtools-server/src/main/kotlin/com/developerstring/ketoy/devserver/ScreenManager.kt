package com.developerstring.ketoy.devserver

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Manages screen JSON files — reads from disk, caches, and tracks versions.
 */
class ScreenManager(private val watchDir: File) {
    private val screens = ConcurrentHashMap<String, String>()
    private val version = AtomicLong(0)

    init {
        loadAllScreens()
    }

    /**
     * Load all JSON files from the watch directory.
     */
    fun loadAllScreens() {
        watchDir.listFiles { file -> file.extension == "json" }?.forEach { file ->
            loadScreen(file)
        }
    }

    /**
     * Load or reload a single screen JSON file.
     */
    fun loadScreen(file: File): String? {
        return try {
            val json = file.readText().trim()
            val name = file.nameWithoutExtension
            screens[name] = json
            version.incrementAndGet()
            json
        } catch (e: Exception) {
            System.err.println("⚠️  Failed to load ${file.name}: ${e.message}")
            null
        }
    }

    /**
     * Remove a screen (when its JSON file is deleted).
     */
    fun removeScreen(name: String) {
        screens.remove(name)
        version.incrementAndGet()
    }

    /**
     * Get the JSON for a specific screen.
     */
    fun getScreen(name: String): String? = screens[name]

    /**
     * Get all screens as a map.
     */
    fun getAllScreens(): Map<String, String> = screens.toMap()

    /**
     * List all available screen names.
     */
    fun listScreens(): List<String> = screens.keys().toList().sorted()

    /**
     * Get the current data version (increments on every change).
     */
    fun getVersion(): Long = version.get()
}
