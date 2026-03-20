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
     * Sets up `adb reverse` port forwarding for every connected Android emulator.
     *
     * This allows apps running inside the emulator to reach the host-machine dev
     * server at `localhost:<port>` instead of having to use the unreliable `10.0.2.2`
     * special address.
     *
     * Physical devices are skipped — they connect via the LAN IP directly.
     *
     * @param ports The TCP ports to reverse-forward (typically the HTTP port and WebSocket port).
     * @return A list of human-readable status messages describing what was set up.
     */
    fun setupAdbReverse(ports: List<Int>, androidSdkDir: String? = null): List<String> {
        val messages = mutableListOf<String>()
        try {
            val adb = findAdb(androidSdkDir) ?: run {
                messages.add("⚠️  adb not found — emulator reverse forwarding skipped")
                return messages
            }

            val devices = listAdbDevices(adb)
            val emulators = devices.filter { it.startsWith("emulator-") }

            if (emulators.isEmpty()) {
                messages.add("ℹ️  No running emulators detected — reverse forwarding skipped")
                return messages
            }

            for (serial in emulators) {
                for (port in ports) {
                    try {
                        val process = ProcessBuilder(adb, "-s", serial, "reverse", "tcp:$port", "tcp:$port")
                            .redirectErrorStream(true)
                            .start()
                        val output = process.inputStream.bufferedReader().readText().trim()
                        val exitCode = process.waitFor()
                        if (exitCode == 0) {
                            messages.add("✅ adb reverse tcp:$port → localhost:$port on $serial")
                        } else {
                            messages.add("⚠️  adb reverse failed on $serial (port $port): $output")
                        }
                    } catch (e: Exception) {
                        messages.add("⚠️  adb reverse failed on $serial (port $port): ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            messages.add("⚠️  adb reverse setup failed: ${e.message}")
        }
        return messages
    }

    /**
     * Removes `adb reverse` port forwarding rules that were previously set up.
     *
     * @param ports The TCP ports to remove reverse-forwarding for.
     */
    fun removeAdbReverse(ports: List<Int>, androidSdkDir: String? = null) {
        try {
            val adb = findAdb(androidSdkDir) ?: return
            val emulators = listAdbDevices(adb).filter { it.startsWith("emulator-") }
            for (serial in emulators) {
                for (port in ports) {
                    try {
                        ProcessBuilder(adb, "-s", serial, "reverse", "--remove", "tcp:$port")
                            .redirectErrorStream(true)
                            .start()
                            .waitFor()
                    } catch (_: Exception) { /* best-effort cleanup */ }
                }
            }
        } catch (_: Exception) { /* best-effort cleanup */ }
    }

    private fun listAdbDevices(adb: String): List<String> {
        val process = ProcessBuilder(adb, "devices")
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()
        return output.lines()
            .drop(1) // skip "List of devices attached" header
            .filter { it.contains("\tdevice") }
            .map { it.split("\t").first().trim() }
    }

    private fun findAdb(androidSdkDir: String? = null): String? {
        // 1. Check SDK dir passed from Gradle (local.properties sdk.dir)
        if (androidSdkDir != null) {
            val adb = java.io.File(androidSdkDir, "platform-tools/adb")
            if (adb.exists() && adb.canExecute()) return adb.absolutePath
        }

        // 2. Check ANDROID_HOME / ANDROID_SDK_ROOT env vars
        val envSdkDir = System.getenv("ANDROID_HOME")
            ?: System.getenv("ANDROID_SDK_ROOT")
        if (envSdkDir != null) {
            val adb = java.io.File(envSdkDir, "platform-tools/adb")
            if (adb.exists() && adb.canExecute()) return adb.absolutePath
        }

        // 3. Check PATH
        return try {
            val os = System.getProperty("os.name")?.lowercase() ?: ""
            val cmd = if (os.contains("win")) listOf("cmd", "/c", "where", "adb")
                      else listOf("which", "adb")
            val process = ProcessBuilder(cmd)
                .redirectErrorStream(true)
                .start()
            val path = process.inputStream.bufferedReader().readText().trim().lines().firstOrNull()?.trim()
            val exitCode = process.waitFor()
            if (exitCode == 0 && !path.isNullOrEmpty()) path else null
        } catch (_: Exception) {
            null
        }
    }

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
