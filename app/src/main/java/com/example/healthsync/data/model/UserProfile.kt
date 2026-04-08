package com.example.healthsync.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val age: Int = 0,
    val height: Float = 0f,
    val weight: Float = 0f,
    val gender: String = "",
    val profileImageUrl: String = ""
)