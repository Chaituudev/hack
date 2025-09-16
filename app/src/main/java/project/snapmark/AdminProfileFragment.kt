package project.snapmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminProfileFragment : Fragment() {
    private lateinit var tvAdminName: TextView
    private lateinit var tvAdminEmail: TextView
    private val db = FirebaseFirestore.getInstance()
    private val adminId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_profile, container, false)
        tvAdminName = view.findViewById(R.id.tvAdminName)
        tvAdminEmail = view.findViewById(R.id.tvAdminEmail)

        db.collection("Admins").document(adminId!!).get()
            .addOnSuccessListener { doc ->
                tvAdminName.text = "Name: ${doc.getString("name")}"
                tvAdminEmail.text = "Email: ${doc.getString("email")}"
            }
        return view
    }
}
