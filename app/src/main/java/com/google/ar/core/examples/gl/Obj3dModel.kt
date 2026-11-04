package com.google.ar.core.examples.gl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import de.javagl.obj.Obj
import de.javagl.obj.ObjData
import de.javagl.obj.ObjReader
import de.javagl.obj.ObjUtils
import java.io.InputStream
import java.nio.FloatBuffer
import java.nio.IntBuffer
import kotlin.math.cos
import kotlin.math.sin

/**
 * Data container and geometry processor for 3D Wavefront OBJ models.
 */
class Obj3dModel(
    val name: String,
    val vertexCount: Int,
    val faceCount: Int,
    val vertices: FloatArray, // x, y, z triplets
    val normals: FloatArray?,
    val indices: IntArray,
    var textureBitmap: Bitmap? = null
) {
    companion object {
        private const val TAG = "Obj3dModel"

        fun loadFromAssets(context: Context, objAssetPath: String, textureAssetPath: String? = null): Obj3dModel? {
            return try {
                val inputStream: InputStream = context.assets.open(objAssetPath)
                val rawObj: Obj = ObjReader.read(inputStream)
                val obj: Obj = ObjUtils.convertToRenderable(rawObj)

                val vertexBuffer: FloatBuffer = ObjData.getVertices(obj)
                val vertices = FloatArray(vertexBuffer.limit())
                vertexBuffer.get(vertices)

                val normalBuffer: FloatBuffer = ObjData.getNormals(obj)
                val normals = if (normalBuffer.limit() > 0) {
                    val n = FloatArray(normalBuffer.limit())
                    normalBuffer.get(n)
                    n
                } else null

                val indexBuffer: IntBuffer = ObjData.getFaceVertexIndices(obj)
                val indices = IntArray(indexBuffer.limit())
                indexBuffer.get(indices)

                var textureBitmap: Bitmap? = null
                if (textureAssetPath != null) {
                    try {
                        val texStream = context.assets.open(textureAssetPath)
                        textureBitmap = BitmapFactory.decodeStream(texStream)
                        texStream.close()
                    } catch (e: Exception) {
                        Log.w(TAG, "Texture not loaded for $objAssetPath: ${e.message}")
                    }
                }

                inputStream.close()
                Obj3dModel(
                    name = objAssetPath.substringAfterLast('/'),
                    vertexCount = vertices.size / 3,
                    faceCount = indices.size / 3,
                    vertices = vertices,
                    normals = normals,
                    indices = indices,
                    textureBitmap = textureBitmap
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load OBJ model $objAssetPath", e)
                null
            }
        }
    }

    /**
     * Computes bounding box and center offset for normalization.
     */
    fun computeBounds(): FloatArray {
        if (vertices.isEmpty()) return floatArrayOf(0f, 0f, 0f, 1f)
        var minX = Float.MAX_VALUE; var maxX = -Float.MAX_VALUE
        var minY = Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
        var minZ = Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE

        var i = 0
        while (i < vertices.size) {
            val x = vertices[i]
            val y = vertices[i + 1]
            val z = vertices[i + 2]
            if (x < minX) minX = x
            if (x > maxX) maxX = x
            if (y < minY) minY = y
            if (y > maxY) maxY = y
            if (z < minZ) minZ = z
            if (z > maxZ) maxZ = z
            i += 3
        }

        val centerX = (minX + maxX) / 2f
        val centerY = (minY + maxY) / 2f
        val centerZ = (minZ + maxZ) / 2f
        val extent = maxOf(maxX - minX, maxY - minY, maxZ - minZ)
        return floatArrayOf(centerX, centerY, centerZ, extent)
    }
}
