package com.example.camthprojects.models

// Simple data class, no database annotations
data class Document(
    val id: String,
    val projectId: String,
    val fileName: String,
    val fileUrl: String 
)