package com.google.ar.core.examples.model

enum class ArModule(val title: String, val subtitle: String) {
    HELLO_AR("Hello AR", "Planes & 3D Objects"),
    AUGMENTED_FACES("Face Meshes", "Real-Time 3D Filters"),
    AUGMENTED_IMAGES("Image Targets", "Marker & Database Detection"),
    COMPUTER_VISION("Vision & Edges", "CPU Streaming & Sobel Filter"),
    GEOSPATIAL("Geospatial", "VPS & Cloud Anchors"),
    MODEL_VIEWER("3D Models", "Wavefront OBJ & Shader Inspector")
}

enum class PlacableModel(
    val displayName: String,
    val objAsset: String,
    val textureAsset: String,
    val scaleFactor: Float
) {
    ANDY("Andy the Robot", "models/andy.obj", "models/andy.png", 0.003f),
    PAWN("Chess Pawn", "models/pawn.obj", "models/pawn_albedo.png", 0.12f),
    ANCHOR("AR Anchor", "models/anchor.obj", "models/anchor.png", 0.05f),
    GEOSPATIAL_MARKER("Geo Pin", "models/geospatial_marker.obj", "models/spatial_marker_baked.png", 0.3f)
}

enum class FaceFilterType(val displayName: String, val description: String) {
    FOX_EARS("Fox Ears & Nose", "3D ear & nose models attached to facial landmarks"),
    FRECKLES("Freckles Overlay", "Face mesh texture blend over cheek & nose bridge"),
    TRIGRID_MESH("Canonical Trigrid", "Full 468-vertex canonical topology wireframe"),
    NONE("Clean Preview", "Raw camera feed without facial augmentations")
}

enum class EdgeFilterMode(val displayName: String, val description: String) {
    SOBEL("Sobel Edge Filter", "Gradient edge detection highlighting high-frequency contours"),
    GRAYSCALE("Luminance Grayscale", "Single-channel intensity map derived from camera YUV"),
    TURBO_DEPTH("Depth Colormap", "Simulated pseudo-color distance field using Turbo palette"),
    INVERTED("Inverted Edge Contours", "Dark edges on bright field for technical blueprint inspection")
}

data class ArPlacedAnchor(
    val id: String,
    val x: Float,
    val y: Float,
    val z: Float,
    val model: PlacableModel,
    val planeDescription: String = "Horizontal Table",
    val timestamp: Long = System.currentTimeMillis()
)

data class SimulatedPlane(
    val id: String,
    val x: Float,
    val y: Float,
    val z: Float,
    val width: Float,
    val height: Float,
    val isVertical: Boolean = false
)

data class ImageTargetInfo(
    val id: String,
    val name: String,
    val fileName: String,
    val physicalWidthMeters: Float,
    var isTracking: Boolean = false,
    var confidenceScore: Float = 0.95f
)

data class CloudAnchorRecord(
    val id: String,
    val code: String,
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val hostedTimestamp: Long = System.currentTimeMillis()
)
