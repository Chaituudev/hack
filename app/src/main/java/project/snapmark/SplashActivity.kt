package project.snapmark

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import project.snapmark.AdminDashboardActivity
import project.snapmark.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            checkLogin()
        }, 2000) // 2 sec delay
    }

    private fun checkLogin() {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val role = prefs.getString("role", null)

        if (isLoggedIn && role != null) {
            when (role) {
                "Teacher" -> {
                    startActivity(Intent(this, TeacherDashboardActivity::class.java))
                }
                "admin" -> {
                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                }
                else -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
