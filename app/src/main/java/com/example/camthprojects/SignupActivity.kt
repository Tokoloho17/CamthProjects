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
import java.util.UUID

class SignupActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignup: Button
    private lateinit var btnGoToLogin: TextView
    private lateinit var db: AppDatabase

    private val masterAdminEmail = "lethabo.hlalele01@gmail.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        db = AppDatabase.getDatabase(this)

        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnSignup = findViewById(R.id.btnSignup)
        btnGoToLogin = findViewById(R.id.btnGoToLogin)

        btnSignup.setOnClickListener { registerUser() }
        btnGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Assign role based on email
        val role = if (email.equals(masterAdminEmail, ignoreCase = true)) "admin" else "user"

        lifecycleScope.launch(Dispatchers.IO) {
            val existingUser = db.userDao().getAll().find { it.email.equals(email, ignoreCase = true) }
            if (existingUser != null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignupActivity, "Signup failed: Email already exists", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val newUser = User(uid = UUID.randomUUID().toString(), fullName = fullName, email = email, role = role, password = password) // Storing password directly is not secure
            db.userDao().insert(newUser)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@SignupActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}