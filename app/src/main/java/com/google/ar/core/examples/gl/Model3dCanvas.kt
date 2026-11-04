package com.google.ar.core.examples.gl

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Model3dCanvas(
    model: Obj3dModel?,
    rotX: Float,
    rotY: Float,
    scale: Float,
    isWireframe: Boolean = false,
    baseColor: Color = Color(0xFF00E5FF),
    onRotate: (deltaX: Float, deltaY: Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val bounds = remember(model) { model?.computeBounds() ?: floatArrayOf(0f, 0f, 0f, 1f) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onRotate(dragAmount.x, dragAmount.y)
                }
            }
    ) {
        if (model == null || model.vertices.isEmpty() || model.indices.isEmpty()) {
            // Draw placeholder grid
            drawArGrid(this)
            return@Canvas
        }

        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val extent = bounds[3].takeIf { it > 0.0001f } ?: 1f
        val baseScale = (minOf(size.width, size.height) * 0.42f) / extent * scale

        val radX = Math.toRadians(rotX.toDouble())
        val radY = Math.toRadians(rotY.toDouble())
        val cosX = cos(radX).toFloat(); val sinX = sin(radX).toFloat()
        val cosY = cos(radY).toFloat(); val sinY = sin(radY).toFloat()

        // Light direction vector (normalized)
        val lx = 0.577f; val ly = -0.577f; val lz = 0.577f

        // Draw ground grid
        drawArGrid(this)

        val vCount = model.indices.size
        // Render step to maintain 60fps for heavy meshes (> 4000 triangles)
        val step = if (vCount > 15000) 6 else if (vCount > 6000) 3 else 3

        var idx = 0
        while (idx + 2 < vCount) {
            val i0 = model.indices[idx] * 3
            val i1 = model.indices[idx + 1] * 3
            val i2 = model.indices[idx + 2] * 3

            if (i0 + 2 < model.vertices.size && i1 + 2 < model.vertices.size && i2 + 2 < model.vertices.size) {
                // Vertex 0
                val x0 = (model.vertices[i0] - bounds[0]) * baseScale
                val y0 = -(model.vertices[i0 + 1] - bounds[1]) * baseScale // flip Y for screen space
                val z0 = (model.vertices[i0 + 2] - bounds[2]) * baseScale

                // Vertex 1
                val x1 = (model.vertices[i1] - bounds[0]) * baseScale
                val y1 = -(model.vertices[i1 + 1] - bounds[1]) * baseScale
                val z1 = (model.vertices[i1 + 2] - bounds[2]) * baseScale

                // Vertex 2
                val x2 = (model.vertices[i2] - bounds[0]) * baseScale
                val y2 = -(model.vertices[i2 + 1] - bounds[1]) * baseScale
                val z2 = (model.vertices[i2 + 2] - bounds[2]) * baseScale

                // Rotate around Y then X
                val px0 = x0 * cosY + z0 * sinY
                val pz0 = -x0 * sinY + z0 * cosY
                val py0 = y0 * cosX - pz0 * sinX
                val fz0 = y0 * sinX + pz0 * cosX

                val px1 = x1 * cosY + z1 * sinY
                val pz1 = -x1 * sinY + z1 * cosY
                val py1 = y1 * cosX - pz1 * sinX

                val px2 = x2 * cosY + z2 * sinY
                val pz2 = -x2 * sinY + z2 * cosY
                val py2 = y2 * cosX - pz2 * sinX

                // Cross product for face normal in screen space
                val ax = px1 - px0; val ay = py1 - py0
                val bx = px2 - px0; val by = py2 - py0
                val normalZ = ax * by - ay * bx

                // Backface culling: only draw front-facing triangles
                if (normalZ > 0) {
                    val sx0 = centerX + px0; val sy0 = centerY + py0
                    val sx1 = centerX + px1; val sy1 = centerY + py1
                    val sx2 = centerX + px2; val sy2 = centerY + py2

                    val path = Path().apply {
                        moveTo(sx0, sy0)
                        lineTo(sx1, sy1)
                        lineTo(sx2, sy2)
                        close()
                    }

                    if (isWireframe) {
                        drawPath(path, color = baseColor.copy(alpha = 0.75f), style = Stroke(width = 1.2f))
                    } else {
                        // Diffuse lighting intensity calculation
                        val intensity = (0.35f + 0.65f * (normalZ / (kotlin.math.sqrt(ax * ax + ay * ay + normalZ * normalZ) + 0.001f))).coerceIn(0.2f, 1.0f)
                        val faceColor = Color(
                            red = (baseColor.red * intensity).coerceIn(0f, 1f),
                            green = (baseColor.green * intensity).coerceIn(0f, 1f),
                            blue = (baseColor.blue * intensity).coerceIn(0f, 1f),
                            alpha = 0.92f
                        )
                        drawPath(path, color = faceColor)
                        drawPath(path, color = baseColor.copy(alpha = 0.25f), style = Stroke(width = 0.8f))
                    }
                }
            }
            idx += step
        }
    }
}

private fun drawArGrid(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height
    val gridColor = Color(0xFF1E3A8A).copy(alpha = 0.35f)
    val horizonY = h * 0.75f

    // Draw perspective plane grid
    val count = 9
    for (i in -4..4) {
        val startX = w / 2f + i * 28f
        val endX = w / 2f + i * (w * 0.16f)
        scope.drawLine(gridColor, Offset(startX, horizonY), Offset(endX, h), strokeWidth = 1.2f)
    }
    for (step in 1..5) {
        val y = horizonY + (h - horizonY) * (step / 5f)
        val inset = (1f - (step / 5f)) * (w * 0.35f)
        scope.drawLine(gridColor, Offset(inset, y), Offset(w - inset, y), strokeWidth = 1.2f)
    }
}
