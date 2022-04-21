package com.example.runningtrackingapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp //Inject dependencies by using Dagger Hilt
class BaseApplication:Application() {

}