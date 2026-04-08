package com.example.healthsync.utils

object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidName(name: String): Boolean {
        return name.trim().length >= 2
    }

    fun isValidHeartRate(hr: Int): Boolean {
        return hr in 30..220
    }

    fun isValidSteps(steps: Int): Boolean {
        return steps in 0..100000
    }

    fun isValidSleep(hours: Float): Boolean {
        return hours in 0f..24f
    }

    fun isValidWeight(weight: Float): Boolean {
        return weight in 1f..500f
    }

    fun isValidWater(water: Float): Boolean {
        return water in 0f..10f
    }
}