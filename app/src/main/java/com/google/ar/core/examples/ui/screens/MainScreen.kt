package com.google.ar.core.examples.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.ar.core.examples.MainUiState
import com.google.ar.core.examples.MainViewModel
import com.google.ar.core.examples.model.ArModule
import com.google.ar.core.examples.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var showInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ARCore Studio",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = DarkOnSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (uiState.isArCoreInstalled) ArEmerald.copy(alpha = 0.2f) else ArCyan.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, if (uiState.isArCoreInstalled) ArEmerald else ArCyan)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                if (uiState.isArCoreInstalled) ArEmerald else ArCyan,
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (uiState.isArCoreInstalled) "ARCore Ready" else "Simulation Mode",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (uiState.isArCoreInstalled) ArEmerald else ArCyan
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${uiState.currentModule.title} • ${uiState.currentModule.subtitle}",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkOnSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (!hasCameraPermission) {
                        FilledTonalButton(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = ArCyan.copy(alpha = 0.2f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp).testTag("grant_camera_button")
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = ArCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Camera", style = MaterialTheme.typography.labelSmall, color = ArCyan)
                        }
                    }
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.testTag("info_button")
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "About ARCore Studio",
                            tint = DarkOnSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavySurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkNavySurface,
                contentColor = DarkOnSurface
            ) {
                ArModule.values().forEach { module ->
                    val isSelected = uiState.currentModule == module
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectModule(module) },
                        icon = {
                            Icon(
                                when (module) {
                                    ArModule.HELLO_AR -> Icons.Default.ViewInAr
                                    ArModule.AUGMENTED_FACES -> Icons.Default.Face
                                    ArModule.AUGMENTED_IMAGES -> Icons.Default.CropFree
                                    ArModule.COMPUTER_VISION -> Icons.Default.FilterBAndW
                                    ArModule.GEOSPATIAL -> Icons.Default.Place
                                    ArModule.MODEL_VIEWER -> Icons.Default.Layers
                                },
                                contentDescription = module.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = module.title,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkOnPrimary,
                            selectedTextColor = ArCyan,
                            indicatorColor = ArCyan,
                            unselectedIconColor = DarkOnSurfaceVariant,
                            unselectedTextColor = DarkOnSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_${module.name.lowercase()}")
                    )
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.currentModule) {
                ArModule.HELLO_AR -> HelloArScreen(uiState, viewModel)
                ArModule.AUGMENTED_FACES -> AugmentedFacesScreen(uiState, viewModel)
                ArModule.AUGMENTED_IMAGES -> AugmentedImagesScreen(uiState, viewModel)
                ArModule.COMPUTER_VISION -> ComputerVisionScreen(uiState, viewModel)
                ArModule.GEOSPATIAL -> GeospatialScreen(uiState, viewModel)
                ArModule.MODEL_VIEWER -> ModelViewerScreen(uiState, viewModel)
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ViewInAr, contentDescription = null, tint = ArCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("About ARCore Studio", style = MaterialTheme.typography.titleMedium, color = DarkOnSurface)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Unified Google ARCore Android SDK showcase written in Jetpack Compose & Kotlin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkOnSurface
                    )
                    Text(
                        "• Hello AR: Plane detection, instant placement, depth sensing with Turbo colormap, and 3D anchor positioning.\n" +
                        "• Augmented Faces: 468 canonical vertices face mesh tracking, Fox ears & nose, freckles blend, and head pose orientation.\n" +
                        "• Augmented Images: Offline database target recognition (sample_database.imgdb) with tracking HUD.\n" +
                        "• Vision & Edges: CPU camera stream Sobel gradient edge filtering and 60 FPS performance monitoring.\n" +
                        "• Geospatial: VPS localization, WGS84 coordinates, live compass, and Cloud Anchor hosting/resolving.\n" +
                        "• 3D Model Viewer: Interactive 360° inspector for Wavefront OBJ assets (Andy, Pawn, Anchor).",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkOnSurfaceVariant
                    )
                    Text(
                        "Engine: Google Play Services for AR (ARCore 1.47+) + Wavefront OBJ Engine",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArCyan
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showInfoDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }
}
