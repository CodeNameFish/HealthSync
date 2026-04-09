package com.example.healthsync.data.remote

import com.example.healthsync.data.model.HealthLog
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import kotlinx.coroutines.tasks.await

class HealthLogRepository {

    private val db: DatabaseReference = FirebaseDatabase.getInstance("https://healthsync-b1894-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    suspend fun saveLog(log: HealthLog): Result<Unit> {
        return try {
            db.child("healthLogs")
                .child(log.userId)
                .child(log.id.toString())
                .setValue(log)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLogs(userId: String): Result<List<HealthLog>> {
        return try {
            val snapshot = db.child("healthLogs").child(userId).get().await()
            val logs = snapshot.children.mapNotNull { it.getValue<HealthLog>() }
            Result.success(logs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteLog(userId: String, logId: Int): Result<Unit> {
        return try {
            db.child("healthLogs").child(userId).child(logId.toString()).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}