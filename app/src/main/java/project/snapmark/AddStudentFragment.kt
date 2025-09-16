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


class AddStudentFragment : Fragment() {
    private lateinit var etName: EditText
    private lateinit var etRollNo: EditText
    private lateinit var etClass: EditText
    private lateinit var btnAdd: Button
    private val db = FirebaseFirestore.getInstance()
    private val teacherId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_student, container, false)
        etName = view.findViewById(R.id.etStudentName)
        etRollNo = view.findViewById(R.id.etRollNo)
        etClass = view.findViewById(R.id.etClass)
        btnAdd = view.findViewById(R.id.btnAddStudent)

        btnAdd.setOnClickListener {
            val studentData = hashMapOf(
                "name" to etName.text.toString(),
                "rollNo" to etRollNo.text.toString(),
                "class" to etClass.text.toString(),
                "addedByTeacherId" to teacherId
            )

            db.collection("Students").add(studentData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Student Added!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }
}
