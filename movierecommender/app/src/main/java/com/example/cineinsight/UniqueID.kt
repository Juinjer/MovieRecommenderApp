package com.example.cineinsight

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class UniqueID : Application() {
    var uniqueId: String = ""
        private set
    var initialized: Boolean = false

    fun setUniqueId(id: String) {
        uniqueId = id
        initialized = true
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}