package com.example.camthprojects

import com.example.camthprojects.models.Document
import com.example.camthprojects.models.Project
import com.example.camthprojects.models.ServiceRequest
import com.example.camthprojects.models.User

/**
 * A simple in-memory data store for the application.
 * This acts as a replacement for a database.
 * IMPORTANT: All data will be lost when the app is closed.
 */
object InMemoryDataStore {
    val users = mutableListOf<User>()
    val projects = mutableListOf<Project>()
    val serviceRequests = mutableListOf<ServiceRequest>()
    val documents = mutableListOf<Document>()

    /**
     * Clears all data. This is typically called on logout.
     */
    fun clearAll() {
        users.clear()
        projects.clear()
        serviceRequests.clear()
        documents.clear()
    }
}