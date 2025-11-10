package com.example.camthprojects.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    var status: String = "Not Started",
    var startDate: String = "",
    var endDate: String = "",
    var assignedUserId: String = ""
)