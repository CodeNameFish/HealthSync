package com.example.healthsync.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.healthsync.data.model.UserProfile

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfile)

    @Update
    suspend fun update(profile: UserProfile)

    @Query("SELECT * FROM user_profile WHERE uid = :uid LIMIT 1")
    suspend fun getProfile(uid: String): UserProfile?

    @Query("SELECT * FROM user_profile WHERE uid = :uid LIMIT 1")
    fun observeProfile(uid: String): LiveData<UserProfile?>
}