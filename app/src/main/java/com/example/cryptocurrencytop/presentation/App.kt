package com.example.cryptocurrencytop.presentation

import android.app.Application
import com.facebook.stetho.Stetho

class App: Application() {

    companion object {
        const val SHARED_PREFS_NAME = "main prefs"
        const val KEY_REFRESHING_PERIOD = "refreshing period"
    }

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }

}