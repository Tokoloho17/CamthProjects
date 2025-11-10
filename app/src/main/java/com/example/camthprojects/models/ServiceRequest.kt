package com.example.camthprojects.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_requests")
data class ServiceRequest(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String,
    var status: String = "Pending",
    var approvalStatus: String = "Pending" // New field
)