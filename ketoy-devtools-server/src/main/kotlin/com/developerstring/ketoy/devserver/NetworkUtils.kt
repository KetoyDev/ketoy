package com.developerstring.ketoy.devserver

import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Network utility functions for the dev server.
 */
object NetworkUtils {

    /**
     * Get the local IP address of this machine on the LAN.
     * Tries to find a non-loopback IPv4 address.
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
