package com.example.camthprojects.models

// Simple data class, no database annotations
data class User(
    val uid: String,
    val fullName: String,
    val email: String,
    val role: String,
    val password: String
)