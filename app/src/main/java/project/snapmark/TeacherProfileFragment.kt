package project.snapmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TeacherProfileFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvTeacherId: TextView

    private lateinit var pref: SharedPrefManager
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teacher_profile, container, false)

        tvName = view.findViewById(R.id.tvTeacherName)
        tvEmail = view.findViewById(R.id.tvTeacherEmail)
        tvTeacherId = view.findViewById(R.id.tvTeacherId)

        pref = SharedPrefManager(requireContext())
        val uid = pref.getUID()

        uid?.let {
            db.collection("users").document(it).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        tvName.text = "Name: ${doc.getString("name")}"
                        tvEmail.text = "Email: ${doc.getString("email")}"
                        tvTeacherId.text = "Teacher ID: ${pref.getTeacherId()}"
                    }
                }
        }

        return view
    }
}
