package com.example.healthsync.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthsync.data.model.UserProfile
import com.example.healthsync.data.remote.AuthRepository
import com.example.healthsync.data.remote.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val authRepo = AuthRepository()
    private val userRepo = UserRepository()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepo.login(email, password)
            if (result.isSuccess) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun register(email: String, password: String, name: String, age: Int, weight: Float) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepo.register(email, password)
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                val profile = UserProfile(
                    uid = user.uid,
                    name = name,
                    email = email,
                    age = age,
                    weight = weight
                )
                userRepo.saveProfile(profile)
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun isLoggedIn(): Boolean = authRepo.isLoggedIn()
}

sealed class AuthState {
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}