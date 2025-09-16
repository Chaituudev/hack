package project.snapmark

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var btnRegister: Button
    private lateinit var tvLoginRedirect: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        spinnerRole = findViewById(R.id.spinnerRole)
        btnRegister = findViewById(R.id.btnRegister)
        tvLoginRedirect = findViewById(R.id.tvLoginRedirect)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val roles = arrayOf("Teacher", "Admin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val role = spinnerRole.selectedItem.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener

                    val userMap = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "role" to role,
                        "approved" to (role != "Admin")
                    )

                    // If Teacher, assign a unique teacher ID
                    if (role == "Teacher") {
                        val teacherId = "TCH" + System.currentTimeMillis().toString()
                        userMap["teacherId"] = teacherId
                        userMap["allocatedHOD"] = "" // initially no HOD
                    }

                    db.collection("users").document(uid)
                        .set(userMap)
                        .addOnSuccessListener {
                            val msg = if (role == "Teacher") {
                                "Registered Successfully! Your Teacher ID: ${userMap["teacherId"]}"
                            } else {
                                "Registration Successful! Wait for Admin approval."
                            }
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to save user: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Registration failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        tvLoginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
