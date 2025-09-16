package project.snapmark

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder

class AddStudentFragment : Fragment() {

    private lateinit var etName: EditText
    private lateinit var etClass: EditText
    private lateinit var btnAdd: Button
    private lateinit var imgQr: ImageView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_student, container, false)

        etName = view.findViewById(R.id.etStudentName)
        etClass = view.findViewById(R.id.etStudentClass)
        btnAdd = view.findViewById(R.id.btnAddStudent)
        imgQr = view.findViewById(R.id.imgQrCode)

        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            val studentClass = etClass.text.toString().trim()
            val teacherId = FirebaseAuth.getInstance().currentUser?.uid

            if (name.isEmpty() || studentClass.isEmpty() || teacherId == null) {
                Toast.makeText(context, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val student = hashMapOf(
                "name" to name,
                "class" to studentClass,
                "teacherId" to teacherId,
                "createdAt" to System.currentTimeMillis()
            )

            // Save to lowercase "students" collection
            db.collection("students").add(student)
                .addOnSuccessListener { documentRef ->
                    val studentId = documentRef.id  // Firestore auto ID

                    // Generate QR code from studentId
                    generateQrCode(studentId)

                    Toast.makeText(context, "Student Added! QR generated.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }

    private fun generateQrCode(studentId: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(
                studentId,
                BarcodeFormat.QR_CODE,
                400,
                400
            )
            imgQr.setImageBitmap(bitmap)
            imgQr.visibility = View.VISIBLE
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
}
