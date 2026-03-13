package dev.ketoy.gradle.server

import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Network utility functions for the Ketoy Dev Server.
 *
 * Provides helpers to discover the host machine's LAN address so that the server
 * can print accurate connection URLs for physical Android devices on the same Wi-Fi
 * network.
 */
object NetworkUtils {

    /**
     * Discovers the **local IPv4 address** of this machine on the LAN.
     *
     * Wi-Fi interfaces (`en*`, `wlan*`, `wl*`) are preferred because they are
     * the most common path from a developer's laptop to a phone on the same network.
     *
     * Falls back to `127.0.0.1` if no suitable address is found.
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
