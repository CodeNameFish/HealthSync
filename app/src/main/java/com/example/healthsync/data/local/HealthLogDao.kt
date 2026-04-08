package com.example.healthsync.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.healthsync.data.model.HealthLog

@Dao
interface HealthLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: HealthLog): Long

    @Update
    suspend fun update(log: HealthLog)

    @Delete
    suspend fun delete(log: HealthLog)

    @Query("SELECT * FROM health_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllLogs(userId: String): LiveData<List<HealthLog>>

    @Query("SELECT * FROM health_logs WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getLogByDate(userId: String, date: String): HealthLog?

    @Query("SELECT * FROM health_logs WHERE userId = :userId ORDER BY timestamp DESC LIMIT 7")
    fun getRecentLogs(userId: String): LiveData<List<HealthLog>>

    @Query("DELETE FROM health_logs WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Query("SELECT * FROM health_logs WHERE userId = :userId AND syncStatus = 0")
    suspend fun getUnsyncedLogs(userId: String): List<HealthLog>

    @Query("UPDATE health_logs SET syncStatus = 1 WHERE id = :logId")
    suspend fun markAsSynced(logId: Int)
}