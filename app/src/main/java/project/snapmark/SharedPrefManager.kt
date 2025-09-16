package project.snapmark

import android.content.Context
import android.content.SharedPreferences

class SharedPrefManager(context: Context) {

    private val pref: SharedPreferences =
        context.getSharedPreferences("SnapMarkPrefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = pref.edit()

    companion object {
        private const val KEY_UID = "uid"
        private const val KEY_ROLE = "role"
        private const val KEY_TEACHER_ID = "teacherId"
    }

    // Save UID, role, and optional teacherId
    fun saveUser(uid: String, role: String, teacherId: String = "") {
        editor.putString(KEY_UID, uid)
        editor.putString(KEY_ROLE, role)
        editor.putString(KEY_TEACHER_ID, teacherId)
        editor.apply()
    }

    fun getUID(): String? {
        return pref.getString(KEY_UID, null)
    }

    fun getRole(): String? {
        return pref.getString(KEY_ROLE, null)
    }

    fun getTeacherId(): String? {
        return pref.getString(KEY_TEACHER_ID, null)
    }

    fun clear() {
        editor.clear()
        editor.apply()
    }
}
