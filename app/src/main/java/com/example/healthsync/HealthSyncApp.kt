package com.example.healthsync

import android.app.Application
import com.google.firebase.FirebaseApp

class HealthSyncApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}