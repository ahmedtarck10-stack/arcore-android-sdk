package com.google.ar.core.examples.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ar.core.examples.MainUiState
import com.google.ar.core.examples.MainViewModel
import com.google.ar.core.examples.gl.Model3dCanvas
import com.google.ar.core.examples.model.PlacableModel
import com.google.ar.core.examples.ui.theme.*

@Composable
fun ModelViewerScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val model = remember(uiState.viewerModel, uiState.isModelLoading) {
        viewModel.getModel(uiState.viewerModel)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // 3D Canvas Viewport
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(Color(0xFF0F172A))
                .testTag("model_viewer_surface")
        ) {
            Model3dCanvas(
                model = model,
                rotX = uiState.viewerRotX,
                rotY = uiState.viewerRotY,
                scale = uiState.viewerScale,
                isWireframe = uiState.viewerWireframe,
                baseColor = when (uiState.viewerModel) {
                    PlacableModel.ANDY -> Color(0xFF76FF03)
                    PlacableModel.PAWN -> Color(0xFF00E5FF)
                    PlacableModel.ANCHOR -> Color(0xFFFFB300)
                    PlacableModel.GEOSPATIAL_MARKER -> Color(0xFFFF1744)
                },
                onRotate = { dx, dy ->
                    viewModel.rotateViewerModel(dx, dy)
                }
            )

            // Top Instructions HUD
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkNavySurface.copy(alpha = 0.85f),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.RotateRight,
                        contentDescription = null,
                        tint = ArCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Drag to orbit 360° | Use slider to zoom",
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkOnSurface
                    )
                }
            }

            // Mesh Stats Overlay
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
                        text = uiState.viewerModel.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ArCyan
                    )
                    Text(
                        text = "Asset: ${uiState.viewerModel.objAsset}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = DarkOnSurfaceVariant
                    )
                    if (model != null) {
                        Text(
                            text = "Vertices: ${model.vertexCount}  |  Faces: ${model.faceCount}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = ArEmerald
                        )
                    }
                }
            }
        }

        // Controls: Model Selection, Wireframe toggle, Zoom slider
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Select 3D Mesh to Inspect",
                style = MaterialTheme.typography.titleMedium,
                color = DarkOnSurface
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PlacableModel.values()) { m ->
                    val isSelected = uiState.viewerModel == m
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) DarkPrimaryContainer else DarkNavySurface,
                        border = if (isSelected) BorderStroke(1.5.dp, ArCyan) else CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .clickable { viewModel.setViewerModel(m) }
                            .testTag("viewer_model_chip_${m.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                when (m) {
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
                                text = m.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) DarkOnPrimaryContainer else DarkOnSurface
                            )
                        }
                    }
                }
            }

            // Zoom slider & Wireframe Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.ZoomOut, contentDescription = null, tint = DarkOnSurfaceVariant, modifier = Modifier.size(18.dp))
                Slider(
                    value = uiState.viewerScale,
                    onValueChange = { viewModel.setViewerScale(it) },
                    valueRange = 0.5f..2.5f,
                    modifier = Modifier.weight(1f).testTag("zoom_slider")
                )
                Icon(Icons.Default.ZoomIn, contentDescription = null, tint = DarkOnSurfaceVariant, modifier = Modifier.size(18.dp))

                FilterChip(
                    selected = uiState.viewerWireframe,
                    onClick = { viewModel.toggleViewerWireframe() },
                    label = { Text("Wireframe") },
                    leadingIcon = {
                        Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    modifier = Modifier.testTag("wireframe_toggle")
                )
            }
        }
    }
}
