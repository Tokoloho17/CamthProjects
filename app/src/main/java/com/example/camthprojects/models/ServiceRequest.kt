package com.example.camthprojects.models

// Simple data class, no database annotations
data class ServiceRequest(
    val id: String,
    val userId: String,
    val userFullName: String,
    val title: String,
    val description: String,
    val status: String,
    val approvalStatus: String
)