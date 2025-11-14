package com.example.camthprojects

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.camthprojects.models.User
import com.example.camthprojects.InMemoryDataStore
import java.util.UUID

class SignupActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignup: Button
    private lateinit var btnGoToLogin: TextView

    private val masterAdminEmail = "lethabo.hlalele01@gmail.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

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

        val existingUser = InMemoryDataStore.users.find { it.email.equals(email, ignoreCase = true) }
        if (existingUser != null) {
            Toast.makeText(this, "User with this email already exists", Toast.LENGTH_SHORT).show()
            return
        }

        val role = if (email.equals(masterAdminEmail, ignoreCase = true)) "admin" else "user"
        val newUser = User(uid = UUID.randomUUID().toString(), fullName = fullName, email = email, role = role, password = password)

        InMemoryDataStore.users.add(newUser)

        Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}