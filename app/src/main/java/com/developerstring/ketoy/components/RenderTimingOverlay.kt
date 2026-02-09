package com.developerstring.ketoy.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.developerstring.ketoy.core.toJson
import com.developerstring.ketoy.model.KNode
import com.developerstring.ketoy.renderer.JSONStringToUI

// ═══════════════════════════════════════════════════════════════
//  Data class holding timing results for each pipeline phase
// ═══════════════════════════════════════════════════════════════
data class PipelineTimings(
    val dslBuildMs: Double,
    val jsonSerializationMs: Double,
    val jsonParseAndRenderMs: Double,
    val firstDrawMs: Double? = null, // filled after first draw
    val totalMs: Double,
    val jsonSizeBytes: Int
)

// ═══════════════════════════════════════════════════════════════
//  TimedKetoyScreen — measures all 3 SDUI pipeline phases
//  plus first-draw time, and displays a compact timing overlay
// ═══════════════════════════════════════════════════════════════
@Composable
fun TimedKetoyScreen(
    screenName: String,
    buildUi: () -> KNode
) {
    val compositionStartNanos = remember { System.nanoTime() }

    // Phase 1 — DSL Build
    val dslStart = remember { System.nanoTime() }
    val node = remember { buildUi() }
    val dslEnd = remember { System.nanoTime() }

    // Phase 2 — JSON Serialization
    val jsonStart = remember { System.nanoTime() }
    val json = remember { node.toJson() }
    val jsonEnd = remember { System.nanoTime() }

    // Pre-compute timings for Phase 1 & 2
    val dslMs = remember { (dslEnd - dslStart) / 1_000_000.0 }
    val jsonMs = remember { (jsonEnd - jsonStart) / 1_000_000.0 }
    val jsonSize = remember { json.toByteArray(Charsets.UTF_8).size }

    // Phase 3 — Compose rendering + first-draw tracking
    val renderStart = remember { System.nanoTime() }
    var firstDrawNanos by remember { mutableStateOf<Long?>(null) }
    var timings by remember { mutableStateOf<PipelineTimings?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {

        // Rendered SDUI content — with first-draw hook
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    if (firstDrawNanos == null) {
                        firstDrawNanos = System.nanoTime()
                    }
                }
        ) {
            JSONStringToUI(value = json)
        }

        // Compute final timings once first draw happens
        LaunchedEffect(firstDrawNanos) {
            firstDrawNanos?.let { drawNanos ->
                val renderMs = (drawNanos - renderStart) / 1_000_000.0
                val totalMs = (drawNanos - compositionStartNanos) / 1_000_000.0
                timings = PipelineTimings(
                    dslBuildMs = dslMs,
                    jsonSerializationMs = jsonMs,
                    jsonParseAndRenderMs = renderMs,
                    firstDrawMs = totalMs,
                    totalMs = totalMs,
                    jsonSizeBytes = jsonSize
                )
            }
        }

        // ── Timing overlay — collapsed FAB or expanded panel ──
        var expanded by remember { mutableStateOf(false) }

        if (timings != null) {
            // Expanded panel
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(200)) + slideInVertically(tween(300)) { -it },
                exit = fadeOut(tween(150)) + slideOutVertically(tween(250)) { -it },
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                timings?.let { t -> TimingPanel(screenName, t) { expanded = false } }
            }

            // Collapsed FAB pill
            AnimatedVisibility(
                visible = !expanded,
                enter = fadeIn(tween(200)) + scaleIn(tween(250)),
                exit = fadeOut(tween(150)) + scaleOut(tween(200)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 12.dp, top = 8.dp)
            ) {
                timings?.let { t -> TimingFab(t.totalMs) { expanded = true } }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Collapsed state — small pill showing total time + tap hint
// ═══════════════════════════════════════════════════════════════
@Composable
private fun TimingFab(totalMs: Double, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            ),
        color = Color(0xFF1D1B20),
        shadowElevation = 6.dp,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Filled.Speed,
                contentDescription = "Show timings",
                tint = Color(0xFFD0BCFF),
                modifier = Modifier.size(16.dp)
            )
            Text(
                formatMs(totalMs),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Expanded state — full timing panel with phase breakdown
// ═══════════════════════════════════════════════════════════════
@Composable
private fun TimingPanel(screenName: String, t: PipelineTimings, onClose: () -> Unit) {
    val shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
    Column(
        modifier = Modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1D1B20), Color(0xFF2D2A33))
                ),
                shape
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Title row with close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Filled.Speed,
                    contentDescription = null,
                    tint = Color(0xFFD0BCFF),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "$screenName — Pipeline Timing",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD0BCFF),
                    fontFamily = FontFamily.Monospace
                )
            }
            Icon(
                Icons.Filled.Close,
                contentDescription = "Collapse",
                tint = Color(0xFF938F99),
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onClose)
                    .padding(1.dp)
            )
        }

        // Phase breakdown with proportional bar
        val maxPhaseMs = maxOf(t.dslBuildMs, t.jsonSerializationMs, t.jsonParseAndRenderMs, 0.001)

        PhaseRow("DSL Build", t.dslBuildMs, maxPhaseMs, Color(0xFFA8F5C4))
        PhaseRow("JSON Serialize", t.jsonSerializationMs, maxPhaseMs, Color(0xFFFFD8E4))
        PhaseRow("Parse + Render", t.jsonParseAndRenderMs, maxPhaseMs, Color(0xFFD0BCFF))

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(1.dp)
                .background(Color(0xFF49454F))
        )

        // Summary row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Total: ${formatMs(t.totalMs)}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
            Text(
                "JSON: ${formatBytes(t.jsonSizeBytes)}",
                fontSize = 10.sp,
                color = Color(0xFFCAC4D0),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Phase row with label, proportional bar, and value
// ═══════════════════════════════════════════════════════════════
@Composable
private fun PhaseRow(label: String, ms: Double, maxMs: Double, color: Color) {
    val fraction by animateFloatAsState(
        targetValue = (ms / maxMs).toFloat().coerceIn(0.05f, 1f),
        animationSpec = tween(400),
        label = "bar"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Label
        Text(
            label,
            fontSize = 10.sp,
            color = Color(0xFFCAC4D0),
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(90.dp)
        )
        // Bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF49454F))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
        // Value
        Text(
            formatMs(ms),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(52.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════
//  Formatting helpers
// ═══════════════════════════════════════════════════════════════
private fun formatMs(ms: Double): String = when {
    ms < 1.0 -> "%.0fμs".format(ms * 1000)
    ms < 100 -> "%.1fms".format(ms)
    else -> "%.0fms".format(ms)
}

private fun formatBytes(bytes: Int): String = when {
    bytes < 1024 -> "${bytes}B"
    bytes < 1024 * 1024 -> "%.1fKB".format(bytes / 1024.0)
    else -> "%.1fMB".format(bytes / (1024.0 * 1024.0))
}
