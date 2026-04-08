package com.example.healthsync.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthsync.data.model.HealthLog
import com.example.healthsync.data.remote.AuthRepository
import com.example.healthsync.data.remote.HealthLogRepository
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val authRepo = AuthRepository()
    private val healthRepo = HealthLogRepository()

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

    fun loadData() {
        val userId = authRepo.currentUser?.uid

        if (userId == null) {
            _errorMessage.value = "User not logged in"
            return
        }

        _isLoading.value = true
        _userName.value = authRepo.currentUser?.email ?: "User"

        viewModelScope.launch {
            try {
                val result = healthRepo.getLogs(userId)
                if (result.isSuccess) {
                    val allLogs = result.getOrNull() ?: emptyList()
                    val today = com.example.healthsync.utils.DateUtils.getTodayString()
                    val todayEntry = allLogs.find { it.date == today }
                    _todayLog.value = todayEntry
                    val last7Days = allLogs.sortedByDescending { it.timestamp }.take(7)
                    _weeklyLogs.value = last7Days.reversed()
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