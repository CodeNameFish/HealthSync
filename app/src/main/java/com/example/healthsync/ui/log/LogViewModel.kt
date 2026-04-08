package com.example.healthsync.ui.log

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthsync.data.local.AppDatabase
import com.example.healthsync.data.model.HealthLog
import com.example.healthsync.data.remote.AuthRepository
import com.example.healthsync.data.remote.HealthLogRepository
import com.example.healthsync.utils.DateUtils
import kotlinx.coroutines.launch

class LogViewModel(database: AppDatabase) : ViewModel() {
    private val authRepo = AuthRepository()
    private val healthRepo = HealthLogRepository()
    private val healthLogDao = database.healthLogDao()

    private val _saveState = MutableLiveData<SaveState>()
    val saveState: LiveData<SaveState> = _saveState

    fun saveHealthLog(
        steps: Int,
        heartRate: Int,
        sleepHours: Float,
        waterIntake: Float,
        notes: String
    ) {
        val userId = authRepo.currentUser?.uid
        if (userId == null) {
            _saveState.value = SaveState.Error("User not logged in")
            return
        }

        _saveState.value = SaveState.Loading

        val log = HealthLog(
            userId = userId,
            steps = steps,
            heartRate = heartRate,
            sleepHours = sleepHours,
            waterIntake = waterIntake,
            notes = notes,
            date = DateUtils.getTodayString(),
            syncStatus = 0
        )

        viewModelScope.launch {
            val insertedId = healthLogDao.insert(log)
            val result = healthRepo.saveLog(log.copy(id = insertedId.toInt()))
            if (result.isSuccess) {
                healthLogDao.markAsSynced(insertedId.toInt())
            }
            _saveState.value = SaveState.Success
        }
    }

    sealed class SaveState {
        object Loading : SaveState()
        object Success : SaveState()
        data class Error(val message: String) : SaveState()
    }
}