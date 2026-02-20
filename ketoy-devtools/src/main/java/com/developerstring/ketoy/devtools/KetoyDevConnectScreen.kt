package com.developerstring.ketoy.devtools

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Detects whether the app is running on an Android emulator.
 *
 * Uses a combination of [android.os.Build] fingerprint, model, manufacturer,
 * brand, device, product, and hardware fields to cover the most common
 * emulators (Google AVD, Genymotion, etc.).
 *
 * This is used internally by [KetoyDevConnectScreen] to pre-fill the
 * server URL with `10.0.2.2` (the emulator-to-host loopback address).
 *
 * @return `true` if the current device is likely an emulator.
 */
private fun isEmulator(): Boolean {
    return (android.os.Build.FINGERPRINT.startsWith("generic")
            || android.os.Build.FINGERPRINT.startsWith("unknown")
            || android.os.Build.MODEL.contains("google_sdk")
            || android.os.Build.MODEL.contains("Emulator")
            || android.os.Build.MODEL.contains("Android SDK built for x86")
            || android.os.Build.MANUFACTURER.contains("Genymotion")
            || android.os.Build.BRAND.startsWith("generic")
            || android.os.Build.DEVICE.startsWith("generic")
            || android.os.Build.PRODUCT.contains("sdk")
            || android.os.Build.HARDWARE.contains("goldfish")
            || android.os.Build.HARDWARE.contains("ranchu"))
}

/**
 * Material 3 themed connection screen for the Ketoy Dev Tools suite.
 *
 * This is the first screen users see when [KetoyDevWrapper] (or
 * [KetoyDevActivity]) is launched without [KetoyDevConfig.autoConnect].
 * It handles the entire connection flow:
 *
 * - **Server URL input** with automatic emulator detection (pre-fills
 *   `10.0.2.2` when running on an AVD/Genymotion emulator).
 * - **Connect button** that initiates [KetoyDevClient.connect].
 * - **Skip button** that allows loading the app directly without
 *   connecting to a dev server — useful for QA builds that ship the
 *   devtools module but don’t always need live-reload.
 * - **Advanced section** for overriding the default port (`8484`).
 * - **Connection status & error display** driven reactively by
 *   [KetoyDevClient.connectionState] and [KetoyDevClient.lastError].
 * - Dynamic Material You color theming on Android 12+ with graceful
 *   fallback to default dark/light schemes.
 *
 * ## Usage
 * ```kotlin
 * // Typically you don’t call this directly; KetoyDevWrapper uses it.
 * // But you can embed it manually:
 * KetoyDevConnectScreen(
 *     client = remember { KetoyDevClient() },
 *     onConnected = { /* navigate to preview */ },
 *     onSkip      = { /* load app without dev tools */ }
 * )
 * ```
 *
 * ## Architecture
 * The composable is **stateless** with respect to networking — it
 * delegates all connection logic to [KetoyDevClient]. A
 * [LaunchedEffect] observes [ConnectionState.Connected] and
 * automatically invokes [onConnected] when the handshake succeeds.
 *
 * @param client      The [KetoyDevClient] instance managing the connection.
 * @param onConnected Callback invoked once the client reaches
 *                     [ConnectionState.Connected].
 * @param onSkip      Callback invoked when the user taps “Skip” to
 *                     bypass devtools and load the app directly.
 * @param modifier    Optional [Modifier] applied to the root surface.
 *
 * @see KetoyDevClient
 * @see KetoyDevWrapper
 * @see KetoyDevConfig
 */
@Composable
fun KetoyDevConnectScreen(
    client: KetoyDevClient,
    onConnected: () -> Unit,
    onSkip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isDark = isSystemInDarkTheme()

    val onEmulator = remember { isEmulator() }
    var serverUrl by remember { mutableStateOf(if (onEmulator) "10.0.2.2" else "") }
    var showAdvanced by remember { mutableStateOf(false) }
    var customPort by remember { mutableStateOf("8484") }

    val connectionState by client.connectionState
    val lastError by client.lastError

    // Auto-navigate when connected
    LaunchedEffect(connectionState) {
        if (connectionState is ConnectionState.Connected) {
            onConnected()
        }
    }

    // Material 3 color scheme (auto dark/light)
    val colorScheme = if (isDark) dynamicDarkColorScheme(androidx.compose.ui.platform.LocalContext.current)
        .takeIf { android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S }
        ?: darkColorScheme()
    else dynamicLightColorScheme(androidx.compose.ui.platform.LocalContext.current)
        .takeIf { android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S }
        ?: lightColorScheme()

    MaterialTheme(colorScheme = colorScheme) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                // Logo
                DevToolsLogo()

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = "Ketoy Dev Tools",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Connect to your dev server for live preview",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Connection Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // URL Input
                        Text(
                            text = "Server URL",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = serverUrl,
                            onValueChange = { serverUrl = it },
                            placeholder = {
                                Text(if (onEmulator) "10.0.2.2 (emulator → host)" else "192.168.1.5")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Go
                            ),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    keyboardController?.hide()
                                    attemptConnect(client, serverUrl, customPort)
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Wifi,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )

                    // Advanced options
                        AnimatedVisibility(visible = showAdvanced) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Port",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                )
                                OutlinedTextField(
                                    value = customPort,
                                    onValueChange = { customPort = it },
                                    placeholder = { Text("8484") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Advanced toggle
                        TextButton(
                            onClick = { showAdvanced = !showAdvanced },
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Icon(
                                if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showAdvanced) "Hide advanced" else "Advanced options",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Connect Button
                        val isConnecting = connectionState is ConnectionState.Connecting ||
                                connectionState is ConnectionState.Reconnecting

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                attemptConnect(client, serverUrl, customPort)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = serverUrl.isNotBlank() && !isConnecting,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isConnecting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Connecting...")
                            } else {
                                Icon(
                                    Icons.Default.Link,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connect", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Skip Button — loads app directly without dev tools
                        OutlinedButton(
                            onClick = onSkip,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Skip \u2014 Load App Directly")
                        }

                        // Error message
                        AnimatedVisibility(visible = lastError != null) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = lastError ?: "",
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Instructions
                M3InstructionsCard()

                // Emulator hint
                if (onEmulator) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Emulator detected",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Use 10.0.2.2 to reach your host machine's localhost. " +
                                            "This is pre-filled for you.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "ketoy-devtools v1.1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Branded logo composable for the Ketoy Dev Tools connection screen.
 *
 * Renders a circular badge with a sweep gradient border using the
 * current [MaterialTheme] color scheme and a monospaced “K” glyph
 * in the center.
 */
@Composable
private fun DevToolsLogo() {
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.primary
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Brush.sweepGradient(gradientColors)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "K",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Instructional card displayed on [KetoyDevConnectScreen] that walks
 * the developer through the four steps required to start live-reloading:
 *
 * 1. Start the dev server (`./gradlew ketoyDev`).
 * 2. Note the IP address from the terminal output.
 * 3. Enter the IP and tap **Connect**.
 * 4. Edit Kotlin DSL — changes appear instantly.
 */
@Composable
private fun M3InstructionsCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "How to connect",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            M3InstructionStep(
                number = 1,
                title = "Start the dev server",
                code = "./gradlew ketoyDev"
            )
            M3InstructionStep(
                number = 2,
                title = "Note the IP address from terminal output",
                code = null
            )
            M3InstructionStep(
                number = 3,
                title = "Enter the IP above and tap Connect",
                code = null
            )
            M3InstructionStep(
                number = 4,
                title = "Edit your Kotlin DSL — changes appear instantly!",
                code = null
            )
        }
    }
}

/**
 * A single numbered instruction row used inside [M3InstructionsCard].
 *
 * @param number 1-based step number rendered inside a circular badge.
 * @param title  Short human-readable instruction text.
 * @param code   Optional code snippet displayed in a monospace chip.
 */
@Composable
private fun M3InstructionStep(number: Int, title: String, code: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$number",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (code != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Parses the user-entered URL and port, then delegates to
 * [KetoyDevClient.connect].
 *
 * If the URL already contains a colon-delimited port (e.g.
 * `"192.168.1.5:9090"`), the separate [port] field is ignored.
 * Otherwise the numeric value of [port] is used (falling back to
 * `8484` when the string is not a valid integer).
 *
 * @param client The [KetoyDevClient] to connect.
 * @param url    Trimmed server URL entered by the user.
 * @param port   Port string from the advanced-options text field.
 */
private fun attemptConnect(client: KetoyDevClient, url: String, port: String) {
    val cleanUrl = url.trim()
    if (cleanUrl.isBlank()) return

    if (cleanUrl.contains(":")) {
        // URL already has port
        client.connect(cleanUrl)
    } else {
        val portNum = port.toIntOrNull() ?: 8484
        client.connect(cleanUrl, portNum)
    }
}
