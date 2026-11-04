package com.google.ar.core.examples.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.google.ar.core.examples.ui.theme.*

@Composable
fun AugmentedImagesScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_sweep"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Scanner Viewport with "Fit to Scan" HUD
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(Color(0xFF0F172A))
                .testTag("augmented_images_surface")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Dark camera background
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0A0F1D))
                    )
                )

                // Scan target bounding box
                val boxW = minOf(w, h) * 0.65f
                val boxH = boxW * 0.85f
                val left = (w - boxW) / 2f
                val top = (h - boxH) / 2f

                val isAnyTracking = uiState.imageTargets.any { it.isTracking }

                // Target box outline
                drawRoundRect(
                    color = if (isAnyTracking) ArEmerald.copy(alpha = 0.5f) else ArCyan.copy(alpha = 0.35f),
                    topLeft = Offset(left, top),
                    size = Size(boxW, boxH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Corner bracket accents
                val bracketLen = 32f
                val bracketColor = if (isAnyTracking) ArEmerald else ArCyan
                // Top-Left
                drawLine(bracketColor, Offset(left, top), Offset(left + bracketLen, top), strokeWidth = 4.dp.toPx())
                drawLine(bracketColor, Offset(left, top), Offset(left, top + bracketLen), strokeWidth = 4.dp.toPx())
                // Top-Right
                drawLine(bracketColor, Offset(left + boxW, top), Offset(left + boxW - bracketLen, top), strokeWidth = 4.dp.toPx())
                drawLine(bracketColor, Offset(left + boxW, top), Offset(left + boxW, top + bracketLen), strokeWidth = 4.dp.toPx())
                // Bottom-Left
                drawLine(bracketColor, Offset(left, top + boxH), Offset(left + bracketLen, top + boxH), strokeWidth = 4.dp.toPx())
                drawLine(bracketColor, Offset(left, top + boxH), Offset(left, top + boxH - bracketLen), strokeWidth = 4.dp.toPx())
                // Bottom-Right
                drawLine(bracketColor, Offset(left + boxW, top + boxH), Offset(left + boxW - bracketLen, top + boxH), strokeWidth = 4.dp.toPx())
                drawLine(bracketColor, Offset(left + boxW, top + boxH), Offset(left + boxW, top + boxH - bracketLen), strokeWidth = 4.dp.toPx())

                // Animated Laser Line
                val currentLaserY = top + boxH * laserY
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            if (isAnyTracking) ArEmerald else ArCyan,
                            Color.Transparent
                        )
                    ),
                    start = Offset(left, currentLaserY),
                    end = Offset(left + boxW, currentLaserY),
                    strokeWidth = 2.5.dp.toPx()
                )

                // If tracking is locked, draw virtual 3D bounding frame
                if (isAnyTracking) {
                    val p = Path().apply {
                        moveTo(left - 15f, top - 15f)
                        lineTo(left + boxW + 15f, top - 15f)
                        lineTo(left + boxW + 25f, top + boxH + 25f)
                        lineTo(left - 25f, top + boxH + 25f)
                        close()
                    }
                    drawPath(p, color = ArEmerald.copy(alpha = 0.12f))
                    drawPath(p, color = ArEmerald.copy(alpha = 0.8f), style = Stroke(width = 1.8.dp.toPx()))
                }
            }

            // Top Instructions HUD
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = DarkNavySurface.copy(alpha = 0.85f),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CropFree,
                            contentDescription = null,
                            tint = ArCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Align image target inside scanning brackets",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkOnSurface
                        )
                    }
                }
            }

            // Tracking Status Pill at Center
            val trackedItem = uiState.imageTargets.find { it.isTracking }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (trackedItem != null) ArEmerald.copy(alpha = 0.2f) else DarkNavySurface.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, if (trackedItem != null) ArEmerald else DarkOutline),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (trackedItem != null) ArEmerald else ArAmber, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (trackedItem != null) "TRACKING: ${trackedItem.name} (96% Conf)"
                        else "Searching Image Database...",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (trackedItem != null) ArEmerald else ArAmber
                    )
                }
            }
        }

        // Database Targets Catalog List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Augmented Image Database (${uiState.imageTargets.size} Targets)",
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkOnSurface
                )
                Text(
                    text = "sample_database.imgdb",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArCyan
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.imageTargets) { target ->
                    val isTracking = target.isTracking
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isTracking) DarkPrimaryContainer.copy(alpha = 0.7f) else DarkNavySurface
                        ),
                        border = if (isTracking) BorderStroke(1.5.dp, ArEmerald) else CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleTrackingForTarget(target.id) }
                            .testTag("target_card_${target.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(DarkNavyCard, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null,
                                    tint = if (isTracking) ArEmerald else ArCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = target.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 14.sp,
                                    color = DarkOnSurface
                                )
                                Text(
                                    text = "Physical Size: ${target.physicalWidthMeters}m | Asset: ${target.fileName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DarkOnSurfaceVariant
                                )
                            }
                            FilledTonalButton(
                                onClick = { viewModel.toggleTrackingForTarget(target.id) },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isTracking) ArEmerald.copy(alpha = 0.25f) else DarkSurfaceVariant,
                                    contentColor = if (isTracking) ArEmerald else DarkOnSurface
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = if (isTracking) "Tracking" else "Simulate",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
