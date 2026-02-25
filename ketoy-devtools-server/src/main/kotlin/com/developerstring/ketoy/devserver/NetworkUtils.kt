package com.developerstring.ketoy.devserver

import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Network utility functions for the Ketoy Dev Server.
 *
 * Provides helpers to discover the host machine’s LAN address so that the server
 * can print accurate connection URLs for physical Android devices on the same Wi-Fi
 * network. This is critical for the developer workflow: the app running on a phone
 * needs to reach the dev server by IP, not `localhost`.
 *
 * ### Usage
 *
 * ```kotlin
 * val ip = NetworkUtils.getLocalIpAddress()
 * println("Connect your device to http://$ip:8484")
 * ```
 *
 * @see KetoyDevServer
 */
object NetworkUtils {

    /**
     * Discovers the **local IPv4 address** of this machine on the LAN.
     *
     * The algorithm iterates over all non-loopback, up [NetworkInterface]s and collects
     * [Inet4Address] candidates. Wi-Fi interfaces (`en*`, `wlan*`, `wl*`) are preferred
     * because they are the most common path from a developer’s laptop to a phone on
     * the same network.
     *
     * Falls back to `127.0.0.1` if no suitable address is found or an exception occurs.
     *
     * ### Example
     *
     * ```kotlin
     * val ip = NetworkUtils.getLocalIpAddress() // e.g. "192.168.1.42"
     * ```
     *
     * @return A non-loopback IPv4 address string, or `"127.0.0.1"` as a last resort.
     */
    fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            val candidates = mutableListOf<String>()

            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue

                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (address is Inet4Address && !address.isLoopbackAddress) {
                        val ip = address.hostAddress ?: continue
                        // Prefer Wi-Fi interfaces (en0, wlan0, etc.)
                        val name = networkInterface.name.lowercase()
                        if (name.startsWith("en") || name.startsWith("wlan") || name.startsWith("wl")) {
                            return ip
                        }
                        candidates.add(ip)
                    }
                }
            }

            return candidates.firstOrNull() ?: "127.0.0.1"
        } catch (e: Exception) {
            return "127.0.0.1"
        }
    }
}
