package com.example.camthprojects.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String,
    val fullName: String,
    val email: String,
    val role: String, // "admin" or "user"
    val password: String // This should be a hashed password in a real app
)