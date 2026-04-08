package com.example.healthsync.data.local

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.healthsync.data.remote.HealthLogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SyncManager(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val healthLogDao = database.healthLogDao()
    private val healthRepo = HealthLogRepository()

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun syncOfflineData(userId: String) {
        if (!isNetworkAvailable()) return

        CoroutineScope(Dispatchers.IO).launch {
            val unsyncedLogs = healthLogDao.getUnsyncedLogs(userId)
            unsyncedLogs.forEach { log ->
                val result = healthRepo.saveLog(log)
                if (result.isSuccess) {
                    healthLogDao.markAsSynced(log.id)
                }
            }
        }
    }
}