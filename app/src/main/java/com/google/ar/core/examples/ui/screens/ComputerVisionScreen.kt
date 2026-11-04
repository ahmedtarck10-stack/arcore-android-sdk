package com.google.ar.core.examples.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ar.core.examples.MainUiState
import com.google.ar.core.examples.MainViewModel
import com.google.ar.core.examples.model.EdgeFilterMode
import com.google.ar.core.examples.ui.theme.*

@Composable
fun ComputerVisionScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Computer Vision Real-Time Processed Viewport
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(Color(0xFF0F172A))
                .testTag("computer_vision_surface")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                when (uiState.edgeFilterMode) {
                    EdgeFilterMode.SOBEL -> {
                        // Dark background with neon glowing Sobel edge contours
                        drawRect(Color(0xFF0A0F1D))

                        // Draw detected high-gradient edge contours
                        val edgeColor = ArCyan
                        val step = 14f
                        // Simulated edge lines and contours of physical objects in room
                        for (y in 60 until h.toInt() - 60 step 32) {
                            val p = Path()
                            var started = false
                            for (x in 40 until w.toInt() - 40 step 18) {
                                val dX = (x - w / 2f) / (w / 2f)
                                val dY = (y - h / 2f) / (h / 2f)
                                val edgeStrength = (kotlin.math.sin(x * 0.05f) * kotlin.math.cos(y * 0.04f) + 0.6f).coerceIn(0f, 1f)
                                if (edgeStrength > 0.45f) {
                                    val py = y + (edgeStrength * 16f)
                                    if (!started) {
                                        p.moveTo(x.toFloat(), py)
                                        started = true
                                    } else {
                                        p.lineTo(x.toFloat(), py)
                                    }
                                } else {
                                    started = false
                                }
                            }
                            drawPath(p, color = edgeColor.copy(alpha = 0.8f), style = Stroke(width = 1.6.dp.toPx()))
                        }

                        // Outline of desk and monitor
                        drawRoundRect(
                            color = ArCyan,
                            topLeft = Offset(w * 0.2f, h * 0.35f),
                            size = androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.35f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    EdgeFilterMode.GRAYSCALE -> {
                        // Single-channel Luminance preview
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF2B2B2B), Color(0xFF6E6E6E), Color(0xFF1E1E1E))
                            )
                        )
                        // Grayscale object highlights
                        drawCircle(
                            color = Color(0xFFAAAAAA),
                            radius = minOf(w, h) * 0.22f,
                            center = Offset(w * 0.5f, h * 0.5f)
                        )
                    }

                    EdgeFilterMode.TURBO_DEPTH -> {
                        // Simulated Turbo distance map
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFF1744), // Near (Red)
                                    Color(0xFFFFEA00), // Mid-near (Yellow)
                                    Color(0xFF00E676), // Mid (Green)
                                    Color(0xFF00B0FF), // Far (Cyan)
                                    Color(0xFF2979FF)  // Deep (Blue)
                                ),
                                center = Offset(w * 0.5f, h * 0.55f),
                                radius = w * 0.75f
                            )
                        )
                    }

                    EdgeFilterMode.INVERTED -> {
                        // White blueprint background with dark edges
                        drawRect(Color(0xFFF8FAFC))
                        drawRoundRect(
                            color = Color(0xFF0F172A),
                            topLeft = Offset(w * 0.2f, h * 0.35f),
                            size = androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.35f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        for (y in 60 until h.toInt() - 60 step 40) {
                            drawLine(
                                color = Color(0xFF334155),
                                start = Offset(w * 0.15f, y.toFloat()),
                                end = Offset(w * 0.85f, y.toFloat()),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
                }
            }

            // Top Telemetry Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkNavySurface.copy(alpha = 0.9f),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(ArEmerald, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${uiState.fps} FPS | ${uiState.frameTimeMs} ms",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ArEmerald
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkNavySurface.copy(alpha = 0.9f),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Text(
                        text = "${uiState.resolutionWidth}x${uiState.resolutionHeight} YUV_420_888",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArCyan,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Controls and Filters
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Computer Vision Filter Pipelines",
                style = MaterialTheme.typography.titleMedium,
                color = DarkOnSurface
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(EdgeFilterMode.values()) { mode ->
                    val isSelected = uiState.edgeFilterMode == mode
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) DarkPrimaryContainer else DarkNavySurface,
                        border = if (isSelected) BorderStroke(1.5.dp, ArCyan) else CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .clickable { viewModel.setEdgeFilterMode(mode) }
                            .testTag("cv_filter_${mode.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                when (mode) {
                                    EdgeFilterMode.SOBEL -> Icons.Default.FilterBAndW
                                    EdgeFilterMode.GRAYSCALE -> Icons.Default.Tonality
                                    EdgeFilterMode.TURBO_DEPTH -> Icons.Default.ColorLens
                                    EdgeFilterMode.INVERTED -> Icons.Default.InvertColors
                                },
                                contentDescription = null,
                                tint = if (isSelected) ArCyan else DarkOnSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = mode.displayName,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) DarkOnPrimaryContainer else DarkOnSurface
                                )
                                Text(
                                    text = mode.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = DarkOnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Resolution Switcher & Zero-Copy Texture Sharing Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = uiState.resolutionWidth == 1280,
                    onClick = { viewModel.setResolution(1280, 720) },
                    label = { Text("HD (1280x720)") },
                    modifier = Modifier.weight(1f).testTag("resolution_hd")
                )
                FilterChip(
                    selected = uiState.resolutionWidth == 640,
                    onClick = { viewModel.setResolution(640, 480) },
                    label = { Text("VGA (640x480)") },
                    modifier = Modifier.weight(1f).testTag("resolution_vga")
                )
            }
        }
    }
}
