package project.snapmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AllocateTeacherFragment : Fragment() {
    private lateinit var etTeacherId: EditText
    private lateinit var btnAllocate: Button
    private val db = FirebaseFirestore.getInstance()
    private val adminId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_allocate_teacher, container, false)
        etTeacherId = view.findViewById(R.id.etTeacherId)
        btnAllocate = view.findViewById(R.id.btnAllocate)

        btnAllocate.setOnClickListener {
            val teacherIdInput = etTeacherId.text.toString().trim()
            if (teacherIdInput.isEmpty()) {
                Toast.makeText(context, "Enter Teacher ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (adminId == null) {
                Toast.makeText(context, "Admin not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Query the users collection for the teacher with this teacherId
            db.collection("users")
                .whereEqualTo("teacherId", teacherIdInput)
                .whereEqualTo("role", "Teacher") // ensure it's a teacher
                .get()
                .addOnSuccessListener { query ->
                    if (!query.isEmpty) {
                        val teacherDoc = query.documents[0] // assuming teacherId is unique
                        teacherDoc.reference.update("allocatedHOD", adminId)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Teacher allocated!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Teacher not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }
}
