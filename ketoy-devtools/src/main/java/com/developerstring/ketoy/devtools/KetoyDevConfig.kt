package com.developerstring.ketoy.devtools

/**
 * Immutable configuration for connecting to a **Ketoy Dev Server**.
 *
 * Pass an instance to [KetoyDevWrapper] or [KetoyDevActivity] to control
 * connection behavior, overlay visibility, and gesture shortcuts.
 *
 * ## Usage
 * ```kotlin
 * // Manual connect (show connection screen first):
 * KetoyDevWrapper(config = KetoyDevConfig()) {
 *     MyApp()
 * }
 *
 * // Auto-connect to a known server (skip the connection screen):
 * KetoyDevWrapper(
 *     config = KetoyDevConfig(
 *         host = "192.168.1.5",
 *         port = 8484,
 *         autoConnect = true
 *     )
 * ) {
 *     MyApp()
 * }
 *
 * // Hide the floating status overlay:
 * KetoyDevWrapper(
 *     config = KetoyDevConfig(showOverlay = false)
 * ) { MyApp() }
 * ```
 *
 * ## Port convention
 * The HTTP server listens on [port] (default `8484`). The companion
 * WebSocket server always listens on `port + 1` (`8485` by default).
 *
 * @property host              Hostname or IP address of the dev server.
 *                              Leave empty to show the manual connection screen.
 * @property port              HTTP port of the dev server. The WebSocket port
 *                              is always `port + 1`.
 * @property autoConnect       When `true` **and** [host] is non-blank, the
 *                              wrapper connects immediately, skipping
 *                              [KetoyDevConnectScreen].
 * @property showOverlay       Whether the floating [KetoyDevOverlay] status
 *                              pill is rendered on top of the app content.
 * @property shakeToDisconnect When `true`, a device shake gesture triggers
 *                              [KetoyDevClient.disconnect] for quick toggling
 *                              during physical-device testing.
 *
 * @see KetoyDevWrapper
 * @see KetoyDevActivity
 * @see KetoyDevClient
 */
data class KetoyDevConfig(
    val host: String = "",
    val port: Int = 8484,
    val autoConnect: Boolean = false,
    val showOverlay: Boolean = true,
    val shakeToDisconnect: Boolean = true,
)
