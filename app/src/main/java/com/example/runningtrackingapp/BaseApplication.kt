package com.example.runningtrackingapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp //Inject dependencies by using Dagger Hilt
class BaseApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        //Create Timber for Logging
        Timber.plant(Timber.DebugTree())
    }
}