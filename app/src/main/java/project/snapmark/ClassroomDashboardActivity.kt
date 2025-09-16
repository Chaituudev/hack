package project.snapmark

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class ClassroomDashboardActivity : AppCompatActivity() {

    private lateinit var tvResult: TextView
    private lateinit var ivStudent: ImageView
    private lateinit var btnScanQR: Button

    private val CAMERA_REQUEST_CODE = 200
    private val FACE_CAPTURE_CODE = 201

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classroom_dashboard)

        tvResult = findViewById(R.id.tvResult)
        ivStudent = findViewById(R.id.ivStudent)
        btnScanQR = findViewById(R.id.btnScanQR)

        db = FirebaseFirestore.getInstance()

        btnScanQR.setOnClickListener {
            startQRScanner()
        }
    }

    // QR Scanner
    private fun startQRScanner() {
        val options = ScanOptions()
        options.setPrompt("Scan Student QR Code")
        options.setBeepEnabled(true)
        options.setOrientationLocked(false)
        qrLauncher.launch(options)
    }

    private val qrLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val studentId = result.contents
            fetchStudentData(studentId)
        } else {
            Toast.makeText(this, "QR Scan cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    // Fetch student details from Firestore
    private fun fetchStudentData(studentId: String) {
        db.collection("students").document(studentId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name") ?: "Unknown"
                    val roll = doc.getString("roll") ?: "N/A"
                    val email = doc.getString("email") ?: "N/A"

                    tvResult.text = "Student Found:\nName: $name\nRoll: $roll\nEmail: $email"

                    // After showing details → capture face
                    askCameraPermission()
                } else {
                    Toast.makeText(this, "Student not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Ask Camera Permission
    private fun askCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        } else {
            captureFace()
        }
    }

    // Capture Face
    private fun captureFace() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, FACE_CAPTURE_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FACE_CAPTURE_CODE && resultCode == Activity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            ivStudent.setImageBitmap(bitmap)
            Toast.makeText(this, "Face captured successfully", Toast.LENGTH_SHORT).show()
        }
    }
}
