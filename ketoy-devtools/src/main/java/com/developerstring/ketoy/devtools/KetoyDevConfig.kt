package com.developerstring.ketoy.devtools

/**
 * Configuration for connecting to a Ketoy Dev Server.
 *
 * @param host The hostname or IP address of the dev server
 * @param port The HTTP port of the dev server (WebSocket port is port + 1)
 * @param autoConnect Whether to auto-connect on launch (skip the connection screen)
 * @param showOverlay Whether to show the connection status overlay
 * @param shakeToDisconnect Whether shaking the device disconnects from the server
 */
data class KetoyDevConfig(
    val host: String = "",
    val port: Int = 8484,
    val autoConnect: Boolean = false,
    val showOverlay: Boolean = true,
    val shakeToDisconnect: Boolean = true,
)
