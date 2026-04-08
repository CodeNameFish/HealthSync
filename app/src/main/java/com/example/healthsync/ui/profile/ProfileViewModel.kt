package com.example.healthsync.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthsync.data.model.UserProfile
import com.example.healthsync.data.remote.AuthRepository
import com.example.healthsync.data.remote.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val authRepo = AuthRepository()
    private val userRepo = UserRepository()

    private val _profile = MutableLiveData<UserProfile?>()
    val profile: LiveData<UserProfile?> = _profile

    private val _logoutState = MutableLiveData<LogoutState>()
    val logoutState: LiveData<LogoutState> = _logoutState

    fun loadProfile() {
        val uid = authRepo.currentUser?.uid ?: return
        viewModelScope.launch {
            val result = userRepo.getProfile(uid)
            if (result.isSuccess) {
                _profile.value = result.getOrNull()
            } else {
                _profile.value = null
            }
        }
    }

    fun logout() {
        authRepo.logout()
        _logoutState.value = LogoutState.Success
    }

    sealed class LogoutState {
        object Success : LogoutState()
    }
}