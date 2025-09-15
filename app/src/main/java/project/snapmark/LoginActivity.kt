package project.snapmark

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import project.snapmark.AdminDashboardActivity
import project.snapmark.R
import project.snapmark.StudentDashboardActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Example button for student login
        findViewById<Button>(R.id.btnLoginStudent).setOnClickListener {
            saveLogin("student")
            startActivity(Intent(this, StudentDashboardActivity::class.java))
            finish()
        }

        // Example button for admin login
        findViewById<Button>(R.id.btnLoginAdmin).setOnClickListener {
            saveLogin("admin")
            startActivity(Intent(this, AdminDashboardActivity::class.java))
            finish()
        }
    }

    private fun saveLogin(role: String) {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE).edit()
        prefs.putBoolean("is_logged_in", true)
        prefs.putString("role", role)
        prefs.apply()
    }
}
