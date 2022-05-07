package com.example.runningtrackingapp.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.runningtrackingapp.db.RunningDatabase
import com.example.runningtrackingapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runningtrackingapp.other.Constants.KEY_USER_NAME
import com.example.runningtrackingapp.other.Constants.KEY_USER_WEIGHT
import com.example.runningtrackingapp.other.Constants.RUNNING_DATABASE_NAME
import com.example.runningtrackingapp.other.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) //It lives as long as the Activity lives
object AppModule {

    /**
     * Dagger calls these functions by itself
     * Create functions which provide dependencies
     */
    @Singleton //Prevent multiple instances
    @Provides
    fun provideRunningDatabase(
        @ApplicationContext app:Context
    ) = Room.databaseBuilder(
        app,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunDAO(db:RunningDatabase) = db.getRunDao()

    //Use SharePreference Object to save first time toggle
    //Show the setup on the first launch
    @Singleton
    @Provides
    fun provideSharePreference(@ApplicationContext app:Context) = app.getSharedPreferences(
        SHARED_PREFERENCES_NAME,MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideUserName(sharedPref:SharedPreferences) = sharedPref.getString(KEY_USER_NAME, "") ?: ""

    @Singleton
    @Provides
    fun provideUserWeight(sharedPref: SharedPreferences) = sharedPref.getFloat(KEY_USER_WEIGHT, 0f)

    @Singleton
    @Provides
    fun provideFirstTime(sharedPref: SharedPreferences) = sharedPref.getBoolean(
        KEY_FIRST_TIME_TOGGLE, true)
}