package com.example.healthsync.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_logs")
data class HealthLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String = "",
    val steps: Int = 0,
    val heartRate: Int = 0,
    val sleepHours: Float = 0f,
    val waterIntake: Float = 0f,
    val weight: Float = 0f,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = "",
    val syncStatus: Int = 0
)