package com.example.healthsync.ui.profile

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.healthsync.data.model.HealthMetric
import com.example.healthsync.data.model.UserProfile
import com.example.healthsync.data.remote.AuthRepository
import com.example.healthsync.data.remote.HealthLogRepository
import com.example.healthsync.data.remote.UserRepository
import com.example.healthsync.utils.HealthAIProcessor
import com.example.healthsync.utils.ReportGenerator
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepo = AuthRepository()
    private val userRepo = UserRepository()
    private val healthRepo = HealthLogRepository()
    private val aiProcessor = HealthAIProcessor(application)
    private val reportGenerator = ReportGenerator(application)
    private val sharedPrefs = application.getSharedPreferences("healthsync_prefs", Context.MODE_PRIVATE)

    private val _profile = MutableLiveData<UserProfile?>()
    val profile: LiveData<UserProfile?> = _profile

    private val _aiStatus = MutableLiveData<String>()
    val aiStatus: LiveData<String> = _aiStatus

    private val _updateState = MutableLiveData<Result<Unit>?>()
    val updateState: LiveData<Result<Unit>?> = _updateState

    private val _logoutState = MutableLiveData<LogoutState>()
    val logoutState: LiveData<LogoutState> = _logoutState

    private val _reportState = MutableLiveData<ReportState?>()
    val reportState: LiveData<ReportState?> = _reportState

    private val _isBiometricEnabled = MutableLiveData<Boolean>()
    val isBiometricEnabled: LiveData<Boolean> = _isBiometricEnabled

    init {
        _isBiometricEnabled.value = sharedPrefs.getBoolean("biometric_enabled", false)
    }

    fun loadProfile() {
        val uid = authRepo.currentUser?.uid ?: return
        viewModelScope.launch {
            val result = userRepo.getProfile(uid)
            if (result.isSuccess) {
                _profile.value = result.getOrNull()
                loadAIStatus(uid)
            } else {
                _profile.value = null
            }
        }
    }

    private suspend fun loadAIStatus(uid: String) {
        val logsResult = healthRepo.getLogs(uid)
        if (logsResult.isSuccess) {
            val latestLog = logsResult.getOrNull()?.maxByOrNull { it.timestamp }
            if (latestLog != null) {
                val input = floatArrayOf(
                    latestLog.heartRate.toFloat(),
                    latestLog.sleepHours,
                    latestLog.steps.toFloat(),
                    latestLog.waterIntake
                )
                _aiStatus.value = aiProcessor.analyzeTrend(input)
            } else {
                _aiStatus.value = "No health data logged yet. Start logging to see AI analysis."
            }
        } else {
            _aiStatus.value = "Unable to load health data for AI analysis."
        }
    }

    fun updateName(newName: String) {
        val uid = authRepo.currentUser?.uid ?: return
        if (newName.isBlank()) return

        viewModelScope.launch {
            val result = userRepo.updateProfile(uid, mapOf("name" to newName))
            if (result.isSuccess) {
                _profile.value = _profile.value?.copy(name = newName)
            }
            _updateState.value = result
        }
    }

    fun clearUpdateState() {
        _updateState.value = null
    }

    fun setBiometricEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("biometric_enabled", enabled).apply()
        _isBiometricEnabled.value = enabled
    }

    fun generateReport() {
        val uid = authRepo.currentUser?.uid ?: return
        viewModelScope.launch {
            _reportState.value = ReportState.Loading
            val result = healthRepo.getLogs(uid)
            if (result.isSuccess) {
                val logs = result.getOrNull()?.sortedByDescending { it.timestamp }?.take(7) ?: emptyList()
                if (logs.isNotEmpty()) {
                    val metrics = mutableListOf<HealthMetric>()
                    logs.reversed().forEach { log ->
                        metrics.add(HealthMetric("Date", log.date))
                        metrics.add(HealthMetric("Steps", log.steps.toString()))
                        metrics.add(HealthMetric("Heart Rate", "${log.heartRate} bpm"))
                        metrics.add(HealthMetric("Sleep", "${log.sleepHours} hrs"))
                        metrics.add(HealthMetric("Water", "${log.waterIntake} L"))
                        metrics.add(HealthMetric("---", "---"))
                    }
                    val reportResult = reportGenerator.generateWeeklyPDF(metrics)
                    if (reportResult != null) {
                        _reportState.value = ReportState.Success(reportResult)
                    } else {
                        _reportState.value = ReportState.Error("Failed to generate report")
                    }
                } else {
                    _reportState.value = ReportState.Error("No data available for report")
                }
            } else {
                _reportState.value = ReportState.Error("Failed to fetch logs")
            }
        }
    }

    fun resetReportState() {
        _reportState.value = null
    }

    fun logout() {
        authRepo.logout()
        _logoutState.value = LogoutState.Success
    }

    override fun onCleared() {
        super.onCleared()
        aiProcessor.close()
    }

    sealed class LogoutState {
        object Success : LogoutState()
    }

    sealed class ReportState {
        object Loading : ReportState()
        data class Success(val result: ReportGenerator.ReportResult) : ReportState()
        data class Error(val message: String) : ReportState()
    }
}