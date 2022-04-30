package com.example.runningtrackingapp.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings.Global.getString
import androidx.core.app.NotificationCompat
import com.example.runningtrackingapp.R
import com.example.runningtrackingapp.other.Constants
import com.example.runningtrackingapp.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)//Declare how long the dependency will live
object ServiceModules {
    @ServiceScoped//There will be only one instance during the lifetime of the service
    @Provides
    fun provideFuseLocationProviderClient(
        @ApplicationContext app:Context
    ) = FusedLocationProviderClient(app)

    //Pending Intent
    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
        @ApplicationContext app:Context
    ) = PendingIntent.getActivity(
        app,
        0,
        Intent(app, MainActivity::class.java).also {
            it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
        }, PendingIntent.FLAG_IMMUTABLE
        //Update the intent instead of recreate
    )

    //Notification Builder
    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext app:Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)
        .setOngoing(true)//Can't be swipe away
        .setSmallIcon(R.drawable.ic_baseline_directions_run_24)
        .setContentTitle("Running App")
        .setContentText("00:00:00")//Initial time, will be updated by seconds
        .setContentIntent(pendingIntent)
}