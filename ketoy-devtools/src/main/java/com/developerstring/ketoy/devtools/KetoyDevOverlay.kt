package com.developerstring.ketoy.devtools

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

/**
 * Floating overlay that shows the dev server connection status.
 * Sits on top of the app content and can be expanded/collapsed.
 */
@Composable
fun KetoyDevOverlay(
    client: KetoyDevClient,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val connectionState by client.connectionState
    val dataVersion by client.dataVersion
    val screenCount = client.screens.size
    val connectedUrl by client.connectedUrl

    var expanded by remember { mutableStateOf(false) }

    // Status color
    val statusColor = when (connectionState) {
        is ConnectionState.Connected -> Color(0xFF3FB950)
        is ConnectionState.Connecting, is ConnectionState.Reconnecting -> Color(0xFFD29922)
        is ConnectionState.Error -> Color(0xFFF85149)
        is ConnectionState.Disconnected -> Color(0xFF484F58)
    }

    // Pulse animation for connecting state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val isConnecting = connectionState is ConnectionState.Connecting ||
            connectionState is ConnectionState.Reconnecting

    Box(
        modifier = modifier
            .padding(16.dp)
            .zIndex(Float.MAX_VALUE)
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopEnd),
            horizontalAlignment = Alignment.End
        ) {
            // Collapsed: Just a small status dot
            AnimatedVisibility(
                visible = !expanded,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF161B22))
                        .clickable { expanded = true },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                if (isConnecting) statusColor.copy(alpha = pulse)
                                else statusColor
                            )
                    )
                }
            }

            // Expanded: Full status card
            AnimatedVisibility(
                visible = expanded,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF161B22)
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isConnecting) statusColor.copy(alpha = pulse)
                                            else statusColor
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ketoy Dev",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }

                            IconButton(
                                onClick = { expanded = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Collapse",
                                    tint = Color(0xFF484F58),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFF30363D))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Status info
                        StatusRow(
                            label = "Status",
                            value = when (connectionState) {
                                is ConnectionState.Connected -> "Connected"
                                is ConnectionState.Connecting -> "Connecting..."
                                is ConnectionState.Reconnecting -> "Reconnecting (#${(connectionState as ConnectionState.Reconnecting).attempt})"
                                is ConnectionState.Error -> "Error"
                                is ConnectionState.Disconnected -> "Disconnected"
                            },
                            valueColor = statusColor
                        )

                        if (connectedUrl.isNotEmpty()) {
                            StatusRow(label = "Server", value = connectedUrl)
                        }

                        StatusRow(label = "Screens", value = "$screenCount")
                        StatusRow(label = "Version", value = "v$dataVersion")

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFF30363D))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(
                                onClick = onDisconnect,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color(0xFFF85149)
                                )
                            ) {
                                Icon(
                                    Icons.Default.LinkOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Disconnect", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFFC9D1D9)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF8B949E)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}
