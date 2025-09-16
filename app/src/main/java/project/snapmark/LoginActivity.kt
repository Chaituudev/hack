package project.snapmark

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegisterRedirect: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var pref: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmailLogin)
        etPassword = findViewById(R.id.etPasswordLogin)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegisterRedirect = findViewById(R.id.tvRegisterRedirect)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        pref = SharedPrefManager(this)

        // If already logged in
        pref.getUID()?.let { uid ->
            pref.getRole()?.let { role ->
                redirectDashboard(role)
            }
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->
                            if (!doc.exists()) {
                                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                                auth.signOut()
                                return@addOnSuccessListener
                            }

                            val role = doc.getString("role") ?: ""
                            val approved = doc.getBoolean("approved") ?: false

                            if (role == "Admin" && !approved) {
                                Toast.makeText(this, "Admin account pending Super Admin approval", Toast.LENGTH_LONG).show()
                                auth.signOut()
                                return@addOnSuccessListener
                            }

                            // Save UID, role, and teacherId (if teacher) in SharedPreferences
                            val teacherId = if (role == "Teacher") doc.getString("teacherId") ?: "" else ""
                            pref.saveUser(uid, role, teacherId)

                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                            redirectDashboard(role)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to fetch user data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Login failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        tvRegisterRedirect.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun redirectDashboard(role: String) {
        when (role) {
            "Admin" -> startActivity(Intent(this, AdminDashboardActivity::class.java))
            "Teacher" -> startActivity(Intent(this, TeacherDashboardActivity::class.java))
            "Classroom" -> startActivity(Intent(this, ClassroomDashboardActivity::class.java))
        }
        finish()
    }
}
