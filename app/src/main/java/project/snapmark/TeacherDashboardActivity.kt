package project.snapmark

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class TeacherDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_dashboard)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavTeacher)
        // Default fragment
        loadFragment(AddStudentFragment())

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_add_student -> loadFragment(AddStudentFragment())
                R.id.nav_profile -> loadFragment(TeacherProfileFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.teacherFragmentContainer, fragment)
            .commit()
    }
}
