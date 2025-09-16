// FaceVerifyActivity.kt (simplified)
package project.snapmark

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import await
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*

class FaceVerifyActivity : AppCompatActivity() {

    private val PICK_CAMERA = 5001
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var embedder: FaceEmbedder

    private lateinit var tvStatus: TextView
    private lateinit var btnCapture: Button

    private var currentStudentId: String = "" // set when scanned QR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_verify)

        tvStatus = findViewById(R.id.tvStatus)
        btnCapture = findViewById(R.id.btnCapture)

        embedder = FaceEmbedder(this, "facenet.tflite", imgSize = 160, embedSize = 128)

        btnCapture.setOnClickListener {
            val take = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(take, PICK_CAMERA)
        }
    }

    // Call this after QR scan to set studentId and begin flow
    fun startVerificationFor(studentId: String) {
        currentStudentId = studentId
        tvStatus.text = "Ready to capture face for $studentId"
        // optionally auto-start camera: btnCapture.performClick()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_CAMERA && resultCode == Activity.RESULT_OK) {
            val bmp = data?.extras?.get("data") as Bitmap
            // Run verification on background thread
            CoroutineScope(Dispatchers.Main).launch {
                tvStatus.text = "Verifying..."
                val result = withContext(Dispatchers.IO) { verifyAgainstStoredFace(currentStudentId, bmp) }
                tvStatus.text = if (result) "Present" else "Absent"
                // Optionally write attendance:
                writeAttendance(currentStudentId, result)
            }
        }
    }

    private suspend fun verifyAgainstStoredFace(studentId: String, liveBitmap: Bitmap): Boolean {
        // 1) get stored face URL from Firestore
        val doc = db.collection("students").document(studentId).get().await()
        val storedFaceUrl = doc.getString("faceUrl") // ensure student doc has faceUrl
            ?: return false

        // 2) download stored image as Bitmap
        val storedBitmap = downloadBitmapFromStorage(storedFaceUrl) ?: return false

        // 3) detect+crop both faces
        val croppedStored = detectAndCropFace(storedBitmap) ?: return false
        val croppedLive = detectAndCropFace(liveBitmap) ?: return false

        // 4) get embeddings
        val embStored = embedder.getEmbedding(croppedStored)
        val embLive = embedder.getEmbedding(croppedLive)

        // 5) compute distance
        val dist = FaceEmbedder.euclideanDistance(embStored, embLive)

        // 6) threshold
        val threshold = 0.8f // start point; tune on your data
        return dist <= threshold
    }

    private suspend fun downloadBitmapFromStorage(url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val ref = storage.getReferenceFromUrl(url)
                val MAX_BYTES: Long = 5 * 1024 * 1024 // 5MB
                val bytes = ref.getBytes(MAX_BYTES).await()
                val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                bmp
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun writeAttendance(studentId: String, present: Boolean) {
        val data = hashMapOf(
            "studentId" to studentId,
            "timestamp" to System.currentTimeMillis(),
            "present" to present
        )
        db.collection("attendance").add(data)
    }
}
