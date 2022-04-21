package com.example.runningtrackingapp.di

import android.content.Context
import androidx.room.Room
import com.example.runningtrackingapp.db.RunningDatabase
import com.example.runningtrackingapp.other.Constants.RUNNING_DATABASE_NAME
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
}