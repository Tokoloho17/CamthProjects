package com.example.camthprojects.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Project(
    val id: String,
    val name: String,
    val description: String,
    val status: String,
    val startDate: String,
    val endDate: String,
    val assignedUserId: String
) : Parcelable