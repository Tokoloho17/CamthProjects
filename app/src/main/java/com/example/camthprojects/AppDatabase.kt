package com.example.camthprojects

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.camthprojects.models.Project
import com.example.camthprojects.models.ServiceRequest
import com.example.camthprojects.models.User

@Database(entities = [User::class, Project::class, ServiceRequest::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun projectDao(): ProjectDao
    abstract fun serviceRequestDao(): ServiceRequestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).allowMainThreadQueries().build() // For simplicity, but not recommended for production
                INSTANCE = instance
                instance
            }
        }
    }
}