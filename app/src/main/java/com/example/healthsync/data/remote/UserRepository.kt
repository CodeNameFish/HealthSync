package com.example.healthsync.data.remote

import com.example.healthsync.data.model.UserProfile
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db: DatabaseReference = FirebaseDatabase.getInstance().reference

    suspend fun saveProfile(profile: UserProfile): Result<Unit> {
        return try {
            db.child("users").child(profile.uid).setValue(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(uid: String): Result<UserProfile> {
        return try {
            val snapshot = db.child("users").child(uid).get().await()
            val profile = snapshot.getValue<UserProfile>()
                ?: return Result.failure(Exception("Profile not found"))
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            db.child("users").child(uid).updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}