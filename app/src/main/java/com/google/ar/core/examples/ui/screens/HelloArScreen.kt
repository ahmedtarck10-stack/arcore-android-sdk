package com.google.ar.core.examples.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ar.core.examples.MainUiState
import com.google.ar.core.examples.MainViewModel
import com.google.ar.core.examples.model.ArPlacedAnchor
import com.google.ar.core.examples.model.PlacableModel
import com.google.ar.core.examples.ui.theme.*

@Composable
fun HelloArScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // AR Viewport with detected planes and placed 3D anchors
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(Color(0xFF0F172A))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Calculate normalized plane position
                        val normX = (offset.x / size.width - 0.5f) * 2f
                        val normY = (offset.y / size.height - 0.5f) * 2f
                        viewModel.placeAnchorAt(normX, normY, planeDesc = "Detected Floor Plane")
                    }
                }
                .testTag("ar_viewport_surface")
        ) {
            // AR Camera preview & plane simulation canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw background camera feed gradient simulation
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D1B2A),
                            Color(0xFF1E293B),
                            Color(0xFF0F172A)
                        )
                    )
                )

                // Depth map turbo colormap overlay if enabled
                if (uiState.isDepthMapVisible) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF0055).copy(alpha = 0.35f),
                                Color(0xFFFFCC00).copy(alpha = 0.25f),
                                Color(0xFF00FFCC).copy(alpha = 0.20f),
                                Color(0xFF0066FF).copy(alpha = 0.35f)
                            ),
                            center = Offset(w * 0.5f, h * 0.6f),
                            radius = w * 0.7f
                        )
                    )
                }

                // Draw Detected AR Planes
                val horizonY = h * 0.55f
                // Plane 1: Floor
                val floorPath = Path().apply {
                    moveTo(w * 0.1f, h * 0.95f)
                    lineTo(w * 0.9f, h * 0.95f)
                    lineTo(w * 0.75f, horizonY + 80f)
                    lineTo(w * 0.25f, horizonY + 80f)
                    close()
                }
                drawPath(
                    floorPath,
                    color = ArCyan.copy(alpha = 0.12f)
                )
                drawPath(
                    floorPath,
                    color = ArCyan.copy(alpha = 0.6f),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Plane 2: Table
                val tablePath = Path().apply {
                    moveTo(w * 0.45f, horizonY + 40f)
                    lineTo(w * 0.92f, horizonY + 40f)
                    lineTo(w * 0.82f, horizonY - 40f)
                    lineTo(w * 0.55f, horizonY - 40f)
                    close()
                }
                drawPath(tablePath, color = ArAmber.copy(alpha = 0.15f))
                drawPath(tablePath, color = ArAmber.copy(alpha = 0.7f), style = Stroke(width = 1.8.dp.toPx()))

                // Grid dots on detected plane
                for (gx in 1..7) {
                    for (gy in 1..5) {
                        val px = w * 0.2f + gx * (w * 0.085f)
                        val py = horizonY + 90f + gy * (h * 0.06f)
                        if (py < h * 0.92f) {
                            drawCircle(
                                color = ArCyan.copy(alpha = 0.75f),
                                radius = 2.5.dp.toPx(),
                                center = Offset(px, py)
                            )
                        }
                    }
                }
            }

            // Overlay HUD: AR Reticle and tracking instructions
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
                            Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = ArCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tap detected plane to place 3D ${uiState.selectedPlacableModel.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkOnSurface
                        )
                    }
                }
            }

            // Center AR Reticle
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .border(1.5.dp, ArCyan.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(ArCyan, CircleShape)
                )
            }

            // Render Placed 3D Model Anchors
            uiState.placedAnchors.forEach { anchor ->
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val anchorPixelX = (anchor.x / 2f + 0.5f) * maxWidth.value
                    val anchorPixelY = (anchor.y / 2f + 0.5f) * maxHeight.value

                    PlacedAnchorMarker(
                        anchor = anchor,
                        modifier = Modifier.offset(
                            x = anchorPixelX.coerceIn(20f, maxWidth.value - 90f).dp,
                            y = anchorPixelY.coerceIn(60f, maxHeight.value - 90f).dp
                        ),
                        onDelete = { viewModel.removeAnchor(anchor.id) }
                    )
                }
            }

            // Top Status badges
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (uiState.isDepthMapVisible) {
                    BadgeChip(text = "Depth Turbo", color = ArMagenta)
                }
                if (uiState.isInstantPlacementEnabled) {
                    BadgeChip(text = "Instant Placement", color = ArCyan)
                }
                BadgeChip(
                    text = "${uiState.placedAnchors.size} Anchors",
                    color = if (uiState.placedAnchors.isNotEmpty()) ArEmerald else DarkOnSurfaceVariant
                )
            }
        }

        // Bottom Controls: Model Selector, Settings, and Clear Actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Model Selector Carousel
            Text(
                text = "Select 3D Object to Place",
                style = MaterialTheme.typography.titleMedium,
                color = DarkOnSurface
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PlacableModel.values()) { model ->
                    val isSelected = uiState.selectedPlacableModel == model
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) DarkPrimaryContainer else DarkNavySurface,
                        border = if (isSelected) BorderStroke(1.5.dp, ArCyan) else CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .clickable { viewModel.selectPlacableModel(model) }
                            .testTag("model_chip_${model.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                when (model) {
                                    PlacableModel.ANDY -> Icons.Default.Android
                                    PlacableModel.PAWN -> Icons.Default.Extension
                                    PlacableModel.ANCHOR -> Icons.Default.Anchor
                                    PlacableModel.GEOSPATIAL_MARKER -> Icons.Default.Place
                                },
                                contentDescription = null,
                                tint = if (isSelected) ArCyan else DarkOnSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = model.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) DarkOnPrimaryContainer else DarkOnSurface
                            )
                        }
                    }
                }
            }

            // Quick AR Settings Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.isDepthMapVisible,
                    onClick = { viewModel.toggleDepthMap() },
                    label = { Text("Depth Map") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Layers,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.weight(1f).testTag("toggle_depth_map")
                )

                FilterChip(
                    selected = uiState.isInstantPlacementEnabled,
                    onClick = { viewModel.toggleInstantPlacement() },
                    label = { Text("Instant Placement") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.FlashOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.weight(1f).testTag("toggle_instant_placement")
                )

                IconButton(
                    onClick = { viewModel.clearAllAnchors() },
                    modifier = Modifier
                        .background(DarkNavySurface, RoundedCornerShape(8.dp))
                        .testTag("clear_anchors_button")
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Clear all anchors",
                        tint = if (uiState.placedAnchors.isNotEmpty()) ArMagenta else DarkOnSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PlacedAnchorMarker(
    anchor: ArPlacedAnchor,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavyCard.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, ArCyan),
        modifier = modifier.testTag("anchor_item_${anchor.id.take(6)}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(DarkPrimaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (anchor.model) {
                        PlacableModel.ANDY -> Icons.Default.Android
                        PlacableModel.PAWN -> Icons.Default.Extension
                        PlacableModel.ANCHOR -> Icons.Default.Anchor
                        PlacableModel.GEOSPATIAL_MARKER -> Icons.Default.Place
                    },
                    contentDescription = null,
                    tint = ArCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = anchor.model.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = DarkOnSurface
                )
                Text(
                    text = "X:${"%.1f".format(anchor.x)} Y:${"%.1f".format(anchor.y)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = DarkOnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(22.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove anchor",
                    tint = DarkOnSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun BadgeChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.6f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
