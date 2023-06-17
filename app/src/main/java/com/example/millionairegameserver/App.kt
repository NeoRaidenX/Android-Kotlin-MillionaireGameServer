package com.example.millionairegameserver

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    init {
        instance = this
    }

    companion object {
        lateinit var instance: App

        fun applicationContext(): Context {
            return instance.applicationContext
        }
    }
}