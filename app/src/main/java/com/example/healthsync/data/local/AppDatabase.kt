package com.example.healthsync.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.healthsync.data.model.HealthLog
import com.example.healthsync.data.model.UserProfile

@Database(
    entities = [HealthLog::class, UserProfile::class],
    version = 2,
    exportSchema = false
)                                          // ← removed @TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun healthLogDao(): HealthLogDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "healthsync_db"
                ).fallbackToDestructiveMigration(true)
                    .build().also { INSTANCE = it }
            }
        }
    }
}