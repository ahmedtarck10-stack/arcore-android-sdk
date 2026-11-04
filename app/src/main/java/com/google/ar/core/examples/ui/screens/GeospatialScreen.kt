package com.google.ar.core.examples.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ar.core.examples.MainUiState
import com.google.ar.core.examples.MainViewModel
import com.google.ar.core.examples.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GeospatialScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var showHostDialog by remember { mutableStateOf(false) }
    var showResolveDialog by remember { mutableStateOf(false) }
    var hostLabelInput by remember { mutableStateOf("") }
    var resolveCodeInput by remember { mutableStateOf("") }
    var resolveResult by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top Geospatial VPS & Compass HUD
        Card(
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Geospatial & VPS Positioning",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ArCyan
                        )
                        Text(
                            text = uiState.vpsAvailability,
                            style = MaterialTheme.typography.labelSmall,
                            color = ArEmerald
                        )
                    }
                    BadgeChip(text = "Accuracy: ${"%.2f".format(uiState.horizontalAccuracyMeters)}m", color = ArEmerald)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Interactive Compass Widget
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .testTag("compass_dial"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val r = size.minDimension / 2f
                            val center = Offset(size.width / 2f, size.height / 2f)

                            drawCircle(
                                color = DarkNavyCard,
                                radius = r
                            )
                            drawCircle(
                                color = ArCyan.copy(alpha = 0.5f),
                                radius = r,
                                style = Stroke(width = 1.5.dp.toPx())
                            )

                            // Compass needle pointing north rotated by heading
                            val rad = Math.toRadians((uiState.compassHeading - 90).toDouble())
                            val tipX = center.x + cos(rad).toFloat() * (r * 0.75f)
                            val tipY = center.y + sin(rad).toFloat() * (r * 0.75f)
                            val tailX = center.x - cos(rad).toFloat() * (r * 0.5f)
                            val tailY = center.y - sin(rad).toFloat() * (r * 0.5f)

                            drawLine(Color(0xFFFF1744), center, Offset(tipX, tipY), strokeWidth = 3.dp.toPx())
                            drawLine(Color.White, center, Offset(tailX, tailY), strokeWidth = 2.dp.toPx())
                            drawCircle(Color.White, radius = 3.dp.toPx(), center = center)
                        }
                    }

                    // WGS84 Coordinates Grid
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Latitude: ${uiState.latitude}° N",
                            style = MaterialTheme.typography.labelMedium,
                            color = DarkOnSurface
                        )
                        Text(
                            text = "Longitude: ${uiState.longitude}° W",
                            style = MaterialTheme.typography.labelMedium,
                            color = DarkOnSurface
                        )
                        Text(
                            text = "Altitude: ${uiState.altitude} m (WGS84 Ellipsoid)",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkOnSurfaceVariant
                        )
                        Text(
                            text = "Heading: ${"%.1f".format(uiState.compassHeading)}° S/SW",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArCyan
                        )
                    }
                }
            }
        }

        // Cloud Anchors Section
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cloud Anchors (${uiState.cloudAnchors.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkOnSurface
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = { showHostDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkPrimaryContainer),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("host_anchor_button")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = ArCyan)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Host Anchor", style = MaterialTheme.typography.labelSmall, color = ArCyan)
                    }

                    FilledTonalButton(
                        onClick = { showResolveDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkNavySurface),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("resolve_anchor_button")
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = ArAmber)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resolve", style = MaterialTheme.typography.labelSmall, color = ArAmber)
                    }
                }
            }

            if (resolveResult != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkNavySurface,
                    border = BorderStroke(1.dp, ArEmerald),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = resolveResult ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = ArEmerald
                        )
                        IconButton(onClick = { resolveResult = null }, modifier = Modifier.size(18.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = DarkOnSurfaceVariant, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.cloudAnchors) { record ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.fillMaxWidth().testTag("cloud_anchor_card_${record.code}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(DarkNavyCard, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CloudQueue, contentDescription = null, tint = ArCyan, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = record.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontSize = 14.sp,
                                        color = DarkOnSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    BadgeChip(text = record.code, color = ArCyan)
                                }
                                Text(
                                    text = "Lat: ${"%.4f".format(record.latitude)} | Lng: ${"%.4f".format(record.longitude)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DarkOnSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { viewModel.deleteCloudAnchor(record.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = DarkOnSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Host Anchor Dialog
    if (showHostDialog) {
        AlertDialog(
            onDismissRequest = { showHostDialog = false },
            title = { Text("Host Cloud Anchor", style = MaterialTheme.typography.titleMedium, color = DarkOnSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Provide a description for this location. It will be uploaded to the Cloud Anchor service and assigned a shareable code.", style = MaterialTheme.typography.bodyMedium, color = DarkOnSurfaceVariant)
                    OutlinedTextField(
                        value = hostLabelInput,
                        onValueChange = { hostLabelInput = it },
                        placeholder = { Text("e.g. Building 43 Lobby") },
                        modifier = Modifier.fillMaxWidth().testTag("host_label_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.hostCloudAnchor(hostLabelInput)
                        hostLabelInput = ""
                        showHostDialog = false
                    },
                    modifier = Modifier.testTag("confirm_host_anchor")
                ) {
                    Text("Host")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHostDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Resolve Anchor Dialog
    if (showResolveDialog) {
        AlertDialog(
            onDismissRequest = { showResolveDialog = false },
            title = { Text("Resolve Cloud Anchor", style = MaterialTheme.typography.titleMedium, color = DarkOnSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter the short Cloud Anchor code (e.g. GEO-4982):", style = MaterialTheme.typography.bodyMedium, color = DarkOnSurfaceVariant)
                    OutlinedTextField(
                        value = resolveCodeInput,
                        onValueChange = { resolveCodeInput = it },
                        placeholder = { Text("GEO-xxxx") },
                        modifier = Modifier.fillMaxWidth().testTag("resolve_code_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val found = viewModel.resolveCloudAnchor(resolveCodeInput)
                        resolveResult = if (found != null) {
                            "Anchor '${found.label}' resolved at (${"%.4f".format(found.latitude)}, ${"%.4f".format(found.longitude)})"
                        } else {
                            "Cloud Anchor code '$resolveCodeInput' not found"
                        }
                        resolveCodeInput = ""
                        showResolveDialog = false
                    },
                    modifier = Modifier.testTag("confirm_resolve_anchor")
                ) {
                    Text("Resolve")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResolveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
