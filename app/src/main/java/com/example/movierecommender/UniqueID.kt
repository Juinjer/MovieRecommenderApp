package com.example.movierecommender

import android.app.Application

class UniqueID:Application() {
    var uniqueId: String = ""
        private set
    var initialized: Boolean = false

    fun setUniqueId(id: String) {
        uniqueId = id
        initialized = true
    }
}