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
 * Returns true if running on an Android emulator.
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
 * Material 3 themed connection screen for Ketoy Dev Tools.
 * Respects system dark/light theme automatically.
 *
 * Provides:
 * - Server URL input with emulator auto-detection
 * - Connect button for live-reload
 * - **Skip button** to load the app directly without dev tools
 * - Advanced port configuration
 * - Connection status and error display
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
