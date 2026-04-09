package com.example.healthsync.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.healthsync.data.model.HealthLog
import com.example.healthsync.data.remote.AuthRepository
import com.example.healthsync.data.remote.HealthLogRepository
import com.example.healthsync.data.remote.UserRepository
import com.example.healthsync.utils.HealthAIProcessor
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepo = AuthRepository()
    private val healthRepo = HealthLogRepository()
    private val userRepo = UserRepository()
    private val aiProcessor = HealthAIProcessor(application)

    private val _todayLog = MutableLiveData<HealthLog?>()
    val todayLog: LiveData<HealthLog?> = _todayLog

    private val _weeklyLogs = MutableLiveData<List<HealthLog>>()
    val weeklyLogs: LiveData<List<HealthLog>> = _weeklyLogs

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _aiInsight = MutableLiveData<String?>()
    val aiInsight: LiveData<String?> = _aiInsight

    fun loadData() {
        val userId = authRepo.currentUser?.uid

        if (userId == null) {
            _errorMessage.value = "User not logged in"
            return
        }

        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // Load User Name from Profile instead of Email
                val userResult = userRepo.getProfile(userId)
                if (userResult.isSuccess) {
                    _userName.value = userResult.getOrNull()?.name ?: "User"
                } else {
                    _userName.value = authRepo.currentUser?.email?.substringBefore("@") ?: "User"
                }

                val result = healthRepo.getLogs(userId)
                if (result.isSuccess) {
                    val allLogs = result.getOrNull() ?: emptyList()
                    val today = com.example.healthsync.utils.DateUtils.getTodayString()
                    val todayEntry = allLogs.find { it.date == today }
                    _todayLog.value = todayEntry
                    val last7Days = allLogs.sortedByDescending { it.timestamp }.take(7)
                    val logs = last7Days.reversed()
                    _weeklyLogs.value = logs

                    // Generate AI Insight
                    todayEntry?.let { log ->
                        val input = floatArrayOf(
                            log.heartRate.toFloat(),
                            log.sleepHours,
                            log.steps.toFloat(),
                            log.waterIntake
                        )
                        _aiInsight.value = aiProcessor.analyzeTrend(input)
                    }
                } else {
                    _errorMessage.value = "Failed to load health data"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        aiProcessor.close()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun hasAnomaly(log: HealthLog?): Boolean {
        if (log == null) return false
        return log.heartRate !in 50..100
                || log.sleepHours < 4f
                || log.steps < 1000
    }
}