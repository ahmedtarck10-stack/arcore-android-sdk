# ARCore Studio

A unified, modern Android application showcasing Google ARCore capabilities using Jetpack Compose, Kotlin, and Material Design 3.

## Overview

This application rewrites and consolidates the core features of the Google ARCore Android SDK samples into an interactive augmented reality studio with real camera integration and simulation fallback.

### Included Features & Modules

1. **Hello AR**:
   - Plane detection visualization (horizontal floors, tables, and vertical walls)
   - Tap-to-place 3D Wavefront OBJ models (Andy the Robot, Chess Pawn, AR Anchor, Geospatial Pin)
   - Instant Placement mode toggle
   - Depth Sensing colormap (Turbo palette pseudo-color visualization)
   - Environmental HDR lighting estimation

2. **Augmented Faces**:
   - 468 canonical vertices face mesh tracking & landmark points
   - 3D facial filter overlays: Fox Ears & Nose, Freckles makeup, and Canonical Trigrid topology wireframe
   - Live Head Pose Telemetry (Pitch, Yaw, Roll, confidence scoring)

3. **Augmented Images**:
   - Image target scanning with "Fit to Scan" HUD and animated sweep laser
   - Reference target database catalog (`sample_database.imgdb`, Earth, Andy badge)
   - Real-time tracking state and virtual 3D bounding frame anchoring

4. **Computer Vision & Edges**:
   - Real-time CPU camera image streaming (`ImageFormat.YUV_420_888`)
   - Sobel edge filter pipeline highlighting gradient contours
   - Grayscale luminance, Inverted blueprint mode, and Turbo depth map
   - Frame processing latency (ms) and stream FPS telemetry

5. **Geospatial & Cloud Anchors**:
   - VPS (Visual Positioning System) and WGS84 GPS coordinate anchor placement
   - Live interactive compass dial and heading orientation
   - Cloud Anchor hosting (generates 16-character IDs and short codes)
   - Cloud Anchor resolving by code lookup

6. **3D Model & Shader Viewer**:
   - 360-degree interactive orbital rotation (pitch & yaw gestures)
   - Model inspection (Andy, Pawn, Anchor, Geo Marker) with polygon face and vertex metrics
   - Wireframe mode and zoom slider controls

## Technical Stack

- **Framework**: Android (minSdk 24, targetSdk 36, compileSdk 36)
- **Language**: Kotlin 2.1.0 with Jetpack Compose (BOM 2025.02.00)
- **AR Engine**: Google Play Services for AR (`com.google.ar:core:1.47.0`)
- **3D Geometry**: Wavefront OBJ 3D Loader (`de.javagl:obj:0.4.0`)
- **Design System**: Material Design 3 (M3)
