package project.snapmark

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import await
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.*

class ClassroomDashboardActivity : AppCompatActivity() {

    private lateinit var tvResult: TextView
    private lateinit var ivStudent: ImageView
    private lateinit var btnScanQR: Button

    private val CAMERA_REQUEST_CODE = 200
    private val FACE_CAPTURE_CODE = 201

    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var currentStudentId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classroom_dashboard)

        tvResult = findViewById(R.id.tvResult)
        ivStudent = findViewById(R.id.ivStudent)
        btnScanQR = findViewById(R.id.btnScanQR)

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        btnScanQR.setOnClickListener { startQRScanner() }
    }

    private val qrLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            currentStudentId = result.contents
            fetchStudentData(currentStudentId)
        } else {
            Toast.makeText(this, "QR Scan cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startQRScanner() {
        val options = ScanOptions()
        options.setPrompt("Scan Student QR Code")
        options.setBeepEnabled(true)
        options.setOrientationLocked(false)
        qrLauncher.launch(options)
    }

    private fun fetchStudentData(studentId: String) {
        db.collection("students").document(studentId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name") ?: "Unknown"
                    val roll = doc.getString("roll") ?: "N/A"
                    tvResult.text = "Student Found:\nName: $name\nRoll: $roll"
                    askCameraPermission()
                } else {
                    Toast.makeText(this, "Student not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun askCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        } else {
            captureFace()
        }
    }

    private fun captureFace() {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, FACE_CAPTURE_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FACE_CAPTURE_CODE && resultCode == Activity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            ivStudent.setImageBitmap(bitmap)
            Toast.makeText(this, "Face captured, verifying...", Toast.LENGTH_SHORT).show()
            verifyFace(bitmap)
        }
    }

    private fun verifyFace(capturedBitmap: Bitmap) {
        CoroutineScope(Dispatchers.Main).launch {
            tvResult.text = "Verifying..."
            val result = withContext(Dispatchers.IO) { runFaceVerification(currentStudentId, capturedBitmap) }
            tvResult.text = if (result) "Present" else "Absent"
            writeAttendance(currentStudentId, result)
        }
    }

    private suspend fun runFaceVerification(studentId: String, liveBitmap: Bitmap): Boolean {
        val doc = db.collection("students").document(studentId).get().await()
        val storedFaceUrl = doc.getString("faceUrl") ?: return false
        val storedBitmap = downloadBitmapFromStorage(storedFaceUrl) ?: return false

        val croppedStored = detectAndCropFace(storedBitmap) ?: return false
        val croppedLive = detectAndCropFace(liveBitmap) ?: return false

        val embedder = FaceEmbedder(this@ClassroomDashboardActivity)
        val embStored = embedder.getEmbedding(croppedStored)
        val embLive = embedder.getEmbedding(croppedLive)
        val distance = FaceEmbedder.euclideanDistance(embStored, embLive)

        return distance <= 0.8f
    }

    private suspend fun downloadBitmapFromStorage(url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val ref = storage.getReferenceFromUrl(url)
                val MAX_BYTES: Long = 5 * 1024 * 1024
                val bytes = ref.getBytes(MAX_BYTES).await()
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
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
