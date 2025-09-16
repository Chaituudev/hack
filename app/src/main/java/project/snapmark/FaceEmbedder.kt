package project.snapmark

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

class FaceEmbedder(
    context: Context,
    modelFileName: String = "facenet.tflite",
    private val imgSize: Int = 160,
    private val embedSize: Int = 128
) {
    private val interpreter: Interpreter

    init {
        val assetFile = context.assets.openFd(modelFileName)
        val inputStream = assetFile.createInputStream()
        val bytes = ByteArray(assetFile.length.toInt())
        inputStream.read(bytes)
        inputStream.close()
        val bb = ByteBuffer.allocateDirect(bytes.size).apply {
            order(ByteOrder.nativeOrder())
            put(bytes)
            position(0)
        }
        interpreter = Interpreter(bb)
    }

    fun getEmbedding(bitmap: Bitmap): FloatArray {
        val resized = Bitmap.createScaledBitmap(bitmap, imgSize, imgSize, true)
        val inputBuffer = convertBitmapToFloatBuffer(resized)
        val output = Array(1) { FloatArray(embedSize) }
        interpreter.run(inputBuffer, output)
        return l2Normalize(output[0])
    }

    private fun convertBitmapToFloatBuffer(bmp: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(1 * imgSize * imgSize * 3 * 4)
        buffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(imgSize * imgSize)
        bmp.getPixels(intValues, 0, imgSize, 0, 0, imgSize, imgSize)
        for (pixel in intValues) {
            val r = (pixel shr 16 and 0xFF)
            val g = (pixel shr 8 and 0xFF)
            val b = (pixel and 0xFF)
            buffer.putFloat((r / 255f - 0.5f) * 2f)
            buffer.putFloat((g / 255f - 0.5f) * 2f)
            buffer.putFloat((b / 255f - 0.5f) * 2f)
        }
        buffer.rewind()
        return buffer
    }

    private fun l2Normalize(embedding: FloatArray): FloatArray {
        // Convert to Double inside sumOf to remove overload ambiguity
        val sum = embedding.sumOf { (it * it).toDouble() }
        val norm = sqrt(sum).toFloat()
        return if (norm == 0f) embedding else embedding.map { it / norm }.toFloatArray()
    }

    companion object {
        fun euclideanDistance(a: FloatArray, b: FloatArray): Float {
            var sum = 0f
            for (i in a.indices) sum += (a[i] - b[i]) * (a[i] - b[i])
            return sqrt(sum)
        }

        fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
            var dot = 0f
            var na = 0f
            var nb = 0f
            for (i in a.indices) {
                dot += a[i] * b[i]
                na += a[i] * a[i]
                nb += b[i] * b[i]
            }
            val denom = sqrt(na) * sqrt(nb)
            return if (denom == 0f) 0f else dot / denom
        }
    }
}
