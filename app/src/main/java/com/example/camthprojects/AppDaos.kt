package com.example.camthprojects

import androidx.room.*
import com.example.camthprojects.models.Project
import com.example.camthprojects.models.ServiceRequest
import com.example.camthprojects.models.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("SELECT * FROM users")
    suspend fun getAll(): List<User>

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)
}

@Dao
interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: Project)

    @Query("SELECT * FROM projects")
    suspend fun getAll(): List<Project>

    @Update
    suspend fun update(project: Project)

    @Delete
    suspend fun delete(project: Project)
}

@Dao
interface ServiceRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: ServiceRequest)

    @Query("SELECT * FROM service_requests")
    suspend fun getAll(): List<ServiceRequest>

    @Update
    suspend fun update(request: ServiceRequest)

    @Delete
    suspend fun delete(request: ServiceRequest)
}