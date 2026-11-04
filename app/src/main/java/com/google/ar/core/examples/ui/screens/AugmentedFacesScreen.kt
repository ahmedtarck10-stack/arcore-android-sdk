package com.google.ar.core.examples.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import com.google.ar.core.examples.model.FaceFilterType
import com.google.ar.core.examples.ui.theme.*

@Composable
fun AugmentedFacesScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Face tracking camera / 3D face mesh canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(Color(0xFF0F172A))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        viewModel.updateHeadPose(-dragAmount.y * 0.2f, dragAmount.x * 0.2f)
                    }
                }
                .testTag("face_mesh_surface")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w / 2f + uiState.headYaw * 3.5f
                val cy = h * 0.46f + uiState.headPitch * 3.5f
                val faceW = minOf(w, h) * 0.38f
                val faceH = faceW * 1.35f

                // Camera selfie background
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1E293B), Color(0xFF0D1B2A)),
                        center = Offset(cx, cy),
                        radius = w * 0.8f
                    )
                )

                // Face oval boundary
                val faceRect = Path().apply {
                    addOval(androidx.compose.ui.geometry.Rect(cx - faceW, cy - faceH, cx + faceW, cy + faceH))
                }

                // If Trigrid Mesh is active, draw the 468-vertex topology wireframe
                if (uiState.activeFaceFilter == FaceFilterType.TRIGRID_MESH || uiState.activeFaceFilter == FaceFilterType.NONE) {
                    drawOval(
                        color = ArCyan.copy(alpha = 0.45f),
                        topLeft = Offset(cx - faceW, cy - faceH),
                        size = Size(faceW * 2f, faceH * 2f),
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Draw canonical facial triangulation net
                    val gridCols = 8
                    val gridRows = 10
                    for (c in 1 until gridCols) {
                        val gx = cx - faceW + c * (faceW * 2f / gridCols)
                        drawLine(
                            color = ArCyan.copy(alpha = 0.25f),
                            start = Offset(gx, cy - faceH * 0.85f),
                            end = Offset(gx, cy + faceH * 0.85f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    for (r in 1 until gridRows) {
                        val gy = cy - faceH + r * (faceH * 2f / gridRows)
                        drawLine(
                            color = ArCyan.copy(alpha = 0.25f),
                            start = Offset(cx - faceW * 0.85f, gy),
                            end = Offset(cx + faceW * 0.85f, gy),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                // Facial Landmarks (Eyes, Nose tip, Lips)
                val eyeOffsetY = cy - faceH * 0.15f
                val leftEyeX = cx - faceW * 0.42f
                val rightEyeX = cx + faceW * 0.42f
                val noseTipY = cy + faceH * 0.12f
                val mouthY = cy + faceH * 0.45f

                // Draw Eye Tracking Contours
                drawCircle(color = ArCyan, radius = 5.dp.toPx(), center = Offset(leftEyeX, eyeOffsetY))
                drawCircle(color = ArCyan, radius = 5.dp.toPx(), center = Offset(rightEyeX, eyeOffsetY))
                drawCircle(color = ArCyan, radius = 4.dp.toPx(), center = Offset(cx, noseTipY))

                // Render Active Filter
                when (uiState.activeFaceFilter) {
                    FaceFilterType.FOX_EARS -> {
                        // Left Fox Ear
                        val earBaseY = cy - faceH * 0.85f
                        val earTipLeftX = cx - faceW * 0.9f
                        val earTipLeftY = cy - faceH * 1.45f
                        val leftEarPath = Path().apply {
                            moveTo(cx - faceW * 0.7f, earBaseY)
                            lineTo(earTipLeftX, earTipLeftY)
                            lineTo(cx - faceW * 0.2f, earBaseY - 30f)
                            close()
                        }
                        drawPath(leftEarPath, color = Color(0xFFFF6D00))
                        drawPath(leftEarPath, color = Color(0xFFFFD180).copy(alpha = 0.8f), style = Stroke(width = 2.dp.toPx()))

                        // Right Fox Ear
                        val earTipRightX = cx + faceW * 0.9f
                        val earTipRightY = cy - faceH * 1.45f
                        val rightEarPath = Path().apply {
                            moveTo(cx + faceW * 0.2f, earBaseY - 30f)
                            lineTo(earTipRightX, earTipRightY)
                            lineTo(cx + faceW * 0.7f, earBaseY)
                            close()
                        }
                        drawPath(rightEarPath, color = Color(0xFFFF6D00))
                        drawPath(rightEarPath, color = Color(0xFFFFD180).copy(alpha = 0.8f), style = Stroke(width = 2.dp.toPx()))

                        // Fox Nose (Nose fur overlay)
                        drawCircle(color = Color(0xFF263238), radius = 10.dp.toPx(), center = Offset(cx, noseTipY))
                        drawCircle(color = Color(0xFFFF6D00).copy(alpha = 0.5f), radius = 22.dp.toPx(), center = Offset(cx, noseTipY))

                        // Whiskers
                        drawLine(Color.White, Offset(cx - 30f, noseTipY + 8f), Offset(cx - 100f, noseTipY - 10f), strokeWidth = 2.dp.toPx())
                        drawLine(Color.White, Offset(cx - 30f, noseTipY + 16f), Offset(cx - 95f, noseTipY + 22f), strokeWidth = 2.dp.toPx())
                        drawLine(Color.White, Offset(cx + 30f, noseTipY + 8f), Offset(cx + 100f, noseTipY - 10f), strokeWidth = 2.dp.toPx())
                        drawLine(Color.White, Offset(cx + 30f, noseTipY + 16f), Offset(cx + 95f, noseTipY + 22f), strokeWidth = 2.dp.toPx())
                    }
                    FaceFilterType.FRECKLES -> {
                        // Freckles pattern over cheeks and nose bridge
                        val freckleColor = Color(0xFF8D6E63)
                        val points = listOf(
                            Offset(cx - 50f, noseTipY - 15f), Offset(cx - 75f, noseTipY - 10f),
                            Offset(cx - 90f, noseTipY - 5f), Offset(cx - 65f, noseTipY + 5f),
                            Offset(cx - 85f, noseTipY + 12f), Offset(cx - 40f, noseTipY - 5f),
                            Offset(cx + 50f, noseTipY - 15f), Offset(cx + 75f, noseTipY - 10f),
                            Offset(cx + 90f, noseTipY - 5f), Offset(cx + 65f, noseTipY + 5f),
                            Offset(cx + 85f, noseTipY + 12f), Offset(cx + 40f, noseTipY - 5f),
                            Offset(cx - 15f, noseTipY - 20f), Offset(cx + 15f, noseTipY - 20f),
                            Offset(cx, noseTipY - 30f)
                        )
                        points.forEach { pt ->
                            drawCircle(freckleColor, radius = 2.5.dp.toPx(), center = pt)
                        }
                    }
                    FaceFilterType.TRIGRID_MESH -> {
                        // Canonical UV texture pattern
                        drawCircle(
                            color = ArMagenta.copy(alpha = 0.5f),
                            radius = 6.dp.toPx(),
                            center = Offset(cx, noseTipY)
                        )
                    }
                    FaceFilterType.NONE -> {
                        // Clean preview with landmark hints
                    }
                }
            }

            // Top Status & Drag hint
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
                            Icons.Default.Face,
                            contentDescription = null,
                            tint = ArCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Drag viewport to simulate head tilt & pose rotation",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkOnSurface
                        )
                    }
                }
            }

            // Head Pose Telemetry Overlay
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkNavyCard.copy(alpha = 0.9f)),
                border = BorderStroke(1.dp, ArCyan.copy(alpha = 0.5f)),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Face Mesh Telemetry",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ArCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pitch: ${"%.1f".format(uiState.headPitch)}°  Yaw: ${"%.1f".format(uiState.headYaw)}°",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = DarkOnSurface
                    )
                    Text(
                        text = "Landmarks: 468 Vertices  |  Confidence: 98%",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = ArEmerald
                    )
                }
            }
        }

        // Filter Switcher Carousel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Augmented Face Filters",
                style = MaterialTheme.typography.titleMedium,
                color = DarkOnSurface
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(FaceFilterType.values()) { filter ->
                    val isSelected = uiState.activeFaceFilter == filter
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) DarkPrimaryContainer else DarkNavySurface,
                        border = if (isSelected) BorderStroke(1.5.dp, ArCyan) else CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .clickable { viewModel.setFaceFilter(filter) }
                            .testTag("filter_chip_${filter.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                when (filter) {
                                    FaceFilterType.FOX_EARS -> Icons.Default.Pets
                                    FaceFilterType.FRECKLES -> Icons.Default.AutoFixHigh
                                    FaceFilterType.TRIGRID_MESH -> Icons.Default.Grid4x4
                                    FaceFilterType.NONE -> Icons.Default.VisibilityOff
                                },
                                contentDescription = null,
                                tint = if (isSelected) ArCyan else DarkOnSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = filter.displayName,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) DarkOnPrimaryContainer else DarkOnSurface
                                )
                                Text(
                                    text = filter.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = DarkOnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
