package com.google.ar.core.examples

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ar.core.examples.ar.ArCoreHelper
import com.google.ar.core.examples.gl.Obj3dModel
import com.google.ar.core.examples.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class MainUiState(
    val currentModule: ArModule = ArModule.HELLO_AR,
    val isArCoreInstalled: Boolean = false,
    val isSimulationMode: Boolean = true,
    val statusMessage: String = "ARCore Studio Ready",

    // Hello AR State
    val selectedPlacableModel: PlacableModel = PlacableModel.ANDY,
    val placedAnchors: List<ArPlacedAnchor> = emptyList(),
    val detectedPlanes: List<SimulatedPlane> = emptyList(),
    val isDepthMapVisible: Boolean = false,
    val isInstantPlacementEnabled: Boolean = true,
    val isEnvironmentalHdr: Boolean = true,

    // Augmented Faces State
    val activeFaceFilter: FaceFilterType = FaceFilterType.FOX_EARS,
    val faceTrackingConfidence: Float = 0.98f,
    val headPitch: Float = -2.4f,
    val headYaw: Float = 1.1f,
    val headRoll: Float = 0.5f,

    // Augmented Images State
    val imageTargets: List<ImageTargetInfo> = emptyList(),
    val selectedTargetId: String? = null,

    // Computer Vision State
    val edgeFilterMode: EdgeFilterMode = EdgeFilterMode.SOBEL,
    val frameTimeMs: Float = 14.8f,
    val fps: Int = 60,
    val resolutionWidth: Int = 1280,
    val resolutionHeight: Int = 720,

    // Geospatial State
    val latitude: Double = 37.4220,
    val longitude: Double = -122.0841,
    val altitude: Double = 14.2,
    val horizontalAccuracyMeters: Float = 0.85f,
    val compassHeading: Float = 188.4f,
    val vpsAvailability: String = "Available (Streetscape Enabled)",
    val cloudAnchors: List<CloudAnchorRecord> = emptyList(),

    // 3D Model Viewer State
    val viewerModel: PlacableModel = PlacableModel.ANDY,
    val viewerRotX: Float = 15f,
    val viewerRotY: Float = 45f,
    val viewerScale: Float = 1.0f,
    val viewerWireframe: Boolean = false,
    val isModelLoading: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val arCoreHelper = ArCoreHelper(application)
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // Pre-parsed 3D models cache
    private val modelsCache = mutableMapOf<PlacableModel, Obj3dModel>()

    init {
        checkArStatus()
        initializeDefaultData()
        preloadModels()
    }

    fun checkArStatus() {
        arCoreHelper.checkArCoreAvailability()
        viewModelScope.launch {
            arCoreHelper.status.collect { status ->
                _uiState.update { current ->
                    current.copy(
                        isArCoreInstalled = status.isArCoreInstalled,
                        isSimulationMode = status.isSimulationMode,
                        statusMessage = if (status.isArCoreInstalled) "Google Play Services for AR Active"
                        else "Interactive Simulation & Sensor Studio"
                    )
                }
            }
        }
    }

    private fun initializeDefaultData() {
        // Initialize default simulated planes for Hello AR
        val planes = listOf(
            SimulatedPlane("plane-floor-1", 0f, -0.6f, -1.5f, 2.8f, 2.2f, isVertical = false),
            SimulatedPlane("plane-table-2", 0.4f, -0.1f, -1.2f, 1.2f, 0.9f, isVertical = false),
            SimulatedPlane("plane-wall-3", -1.0f, 0.2f, -2.5f, 1.5f, 2.0f, isVertical = true)
        )

        // Initialize pre-seeded anchors
        val initialAnchors = listOf(
            ArPlacedAnchor(
                id = UUID.randomUUID().toString(),
                x = 0.15f,
                y = -0.1f,
                z = -1.2f,
                model = PlacableModel.ANDY,
                planeDescription = "Horizontal Table"
            )
        )

        // Preloaded Image Targets from sample assets
        val targets = listOf(
            ImageTargetInfo("target-earth", "Earth & Continents", "default.jpg", 0.25f, isTracking = true, confidenceScore = 0.96f),
            ImageTargetInfo("target-andy-card", "Andy AR Core Badge", "fit_to_scan.png", 0.15f, isTracking = false, confidenceScore = 0.88f),
            ImageTargetInfo("target-marker-qr", "Geometric Marker 002", "sample_database.imgdb", 0.20f, isTracking = false, confidenceScore = 0.92f)
        )

        // Preloaded Cloud Anchors
        val cloudAnchors = listOf(
            CloudAnchorRecord(
                id = "ua-b74a9d21c",
                code = "GEO-4982",
                label = "Googleplex Building 43 Plaza",
                latitude = 37.4220,
                longitude = -122.0841,
                altitude = 12.5
            ),
            CloudAnchorRecord(
                id = "ua-e58f01b3a",
                code = "GEO-1104",
                label = "Android Lawn Statues",
                latitude = 37.4219,
                longitude = -122.0840,
                altitude = 14.1
            )
        )

        _uiState.update {
            it.copy(
                detectedPlanes = planes,
                placedAnchors = initialAnchors,
                imageTargets = targets,
                selectedTargetId = targets.first().id,
                cloudAnchors = cloudAnchors
            )
        }
    }

    private fun preloadModels() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isModelLoading = true) }
            for (modelType in PlacableModel.values()) {
                val model = Obj3dModel.loadFromAssets(
                    getApplication(),
                    modelType.objAsset,
                    modelType.textureAsset
                )
                if (model != null) {
                    modelsCache[modelType] = model
                }
            }
            _uiState.update { it.copy(isModelLoading = false) }
        }
    }

    fun getModel(modelType: PlacableModel): Obj3dModel? = modelsCache[modelType]

    fun selectModule(module: ArModule) {
        _uiState.update { it.copy(currentModule = module) }
    }

    // Hello AR actions
    fun selectPlacableModel(model: PlacableModel) {
        _uiState.update { it.copy(selectedPlacableModel = model) }
    }

    fun placeAnchorAt(x: Float, y: Float, z: Float = -1.2f, planeDesc: String = "Detected Plane") {
        val anchor = ArPlacedAnchor(
            id = UUID.randomUUID().toString(),
            x = x,
            y = y,
            z = z,
            model = _uiState.value.selectedPlacableModel,
            planeDescription = planeDesc
        )
        _uiState.update { it.copy(placedAnchors = it.placedAnchors + anchor) }
    }

    fun removeAnchor(id: String) {
        _uiState.update { it.copy(placedAnchors = it.placedAnchors.filterNot { a -> a.id == id }) }
    }

    fun clearAllAnchors() {
        _uiState.update { it.copy(placedAnchors = emptyList()) }
    }

    fun toggleDepthMap() {
        _uiState.update { it.copy(isDepthMapVisible = !it.isDepthMapVisible) }
    }

    fun toggleInstantPlacement() {
        _uiState.update { it.copy(isInstantPlacementEnabled = !it.isInstantPlacementEnabled) }
    }

    fun toggleEnvironmentalHdr() {
        _uiState.update { it.copy(isEnvironmentalHdr = !it.isEnvironmentalHdr) }
    }

    // Augmented Faces actions
    fun setFaceFilter(filter: FaceFilterType) {
        _uiState.update { it.copy(activeFaceFilter = filter) }
    }

    fun updateHeadPose(pitchDelta: Float, yawDelta: Float) {
        _uiState.update {
            it.copy(
                headPitch = (it.headPitch + pitchDelta).coerceIn(-45f, 45f),
                headYaw = (it.headYaw + yawDelta).coerceIn(-45f, 45f)
            )
        }
    }

    // Augmented Images actions
    fun selectImageTarget(targetId: String) {
        _uiState.update {
            it.copy(
                selectedTargetId = targetId,
                imageTargets = it.imageTargets.map { t ->
                    if (t.id == targetId) t.copy(isTracking = true) else t.copy(isTracking = false)
                }
            )
        }
    }

    fun toggleTrackingForTarget(targetId: String) {
        _uiState.update {
            it.copy(
                imageTargets = it.imageTargets.map { t ->
                    if (t.id == targetId) t.copy(isTracking = !t.isTracking) else t
                }
            )
        }
    }

    // Computer Vision actions
    fun setEdgeFilterMode(mode: EdgeFilterMode) {
        _uiState.update { it.copy(edgeFilterMode = mode) }
    }

    fun setResolution(width: Int, height: Int) {
        _uiState.update { it.copy(resolutionWidth = width, resolutionHeight = height) }
    }

    // Geospatial actions
    fun hostCloudAnchor(label: String) {
        val randomSuffix = (1000..9999).random()
        val code = "GEO-$randomSuffix"
        val record = CloudAnchorRecord(
            id = "ua-${UUID.randomUUID().toString().take(9)}",
            code = code,
            label = label.ifBlank { "Cloud Anchor $code" },
            latitude = _uiState.value.latitude + (Math.random() - 0.5) * 0.0004,
            longitude = _uiState.value.longitude + (Math.random() - 0.5) * 0.0004,
            altitude = _uiState.value.altitude + (Math.random() - 0.5) * 0.5
        )
        _uiState.update { it.copy(cloudAnchors = listOf(record) + it.cloudAnchors) }
    }

    fun resolveCloudAnchor(code: String): CloudAnchorRecord? {
        val found = _uiState.value.cloudAnchors.find { it.code.equals(code.trim(), ignoreCase = true) }
        return found
    }

    fun deleteCloudAnchor(id: String) {
        _uiState.update { it.copy(cloudAnchors = it.cloudAnchors.filterNot { a -> a.id == id }) }
    }

    // 3D Model Viewer actions
    fun setViewerModel(model: PlacableModel) {
        _uiState.update { it.copy(viewerModel = model) }
    }

    fun rotateViewerModel(deltaX: Float, deltaY: Float) {
        _uiState.update {
            it.copy(
                viewerRotY = (it.viewerRotY + deltaX * 0.5f) % 360f,
                viewerRotX = (it.viewerRotX - deltaY * 0.5f).coerceIn(-85f, 85f)
            )
        }
    }

    fun setViewerScale(scale: Float) {
        _uiState.update { it.copy(viewerScale = scale.coerceIn(0.4f, 3.0f)) }
    }

    fun toggleViewerWireframe() {
        _uiState.update { it.copy(viewerWireframe = !it.viewerWireframe) }
    }
}
