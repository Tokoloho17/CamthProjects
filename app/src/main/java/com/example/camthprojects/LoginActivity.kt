package com.example.camthprojects

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.camthprojects.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var spRole: Spinner
    private lateinit var btnLogin: Button
    private lateinit var btnGoToSignup: TextView
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = AppDatabase.getDatabase(this)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        spRole = findViewById(R.id.spRole)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoToSignup = findViewById(R.id.btnGoToSignup)

        val roles = arrayOf("Select Role", "user", "admin")
        spRole.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)

        btnLogin.setOnClickListener { loginUser() }
        btnGoToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val role = spRole.selectedItem.toString()

        if (email.isEmpty() || password.isEmpty() || role == "Select Role") {
            Toast.makeText(this, "Please fill all fields and select a role", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val user = db.userDao().getAll().find { it.email.equals(email, ignoreCase = true) }

            withContext(Dispatchers.Main) {
                if (user != null && user.password == password && user.role == role) {
                    val intent = if (user.role == "admin") {
                        Intent(this@LoginActivity, AdminDashboardActivity::class.java)
                    } else {
                        Intent(this@LoginActivity, UserDashboardActivity::class.java).apply {
                            putExtra("USER_ID", user.uid)
                        }
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid credentials or role", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}