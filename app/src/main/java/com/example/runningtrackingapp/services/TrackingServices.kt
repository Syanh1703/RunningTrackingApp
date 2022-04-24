package com.example.runningtrackingapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.runningtrackingapp.R
import com.example.runningtrackingapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runningtrackingapp.other.Constants.FASTEST_LOCATION_UPDATE_INTERVAL
import com.example.runningtrackingapp.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runningtrackingapp.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runningtrackingapp.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runningtrackingapp.other.Constants.NOTIFICATION_ID
import com.example.runningtrackingapp.other.Constants.TRACKING_SERVICE_PAUSE
import com.example.runningtrackingapp.other.Constants.TRACKING_SERVICE_START_OR_RESUME
import com.example.runningtrackingapp.other.Constants.TRACKING_SERVICE_STOP
import com.example.runningtrackingapp.other.TrackingUtility
import com.example.runningtrackingapp.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber

typealias Polyline = MutableList<LatLng>
typealias multiPolyline = MutableList<Polyline>

class TrackingServices :LifecycleService() {

    var isFirstRun = true
    lateinit var fuseLocationProviderClient:FusedLocationProviderClient

    companion object
    {
        val isTracking = MutableLiveData<Boolean>() //Observe the data from the outside
        val pathPoints = MutableLiveData<multiPolyline>()
    }

    private fun postInitialValue()
    {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValue()
        fuseLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            //Get updates when states changing
            updateLocationTracking(it)
        })

    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        /**
         * Three main actions for activity to communicate with the service
         * 1. Start/Resume the tracking
         * 2. Pause the tracking
         * 3. Stop the tracking
         */
        intent?.let {
            when (intent.action) {
                TRACKING_SERVICE_START_OR_RESUME -> {
                    //Call out the startForeground Service function
                    if(isFirstRun)
                    {
                        startForeGroundService()
                        isFirstRun = false
                    }
                    else
                    {
                        startForeGroundService()
                        Timber.d("Resuming Service")
                    }
                }
                TRACKING_SERVICE_STOP -> {
                    Timber.d("Stop the tracking service")
                }
                TRACKING_SERVICE_PAUSE -> {
                    pauseService()
                    Timber.d("Pause tracking Service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    //Pause the tracking
    private fun pauseService()
    {
        isTracking.postValue(false)
    }
    //Update location tracking
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking:Boolean)
    {
        if(isTracking)
        {
            if(TrackingUtility.hasLocationPermission(this))
            {
                //Request Location Updates
                val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_UPDATE_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fuseLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }
        else{
            fuseLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback()
    {
        override fun onLocationResult(addLocation: LocationResult) {
            super.onLocationResult(addLocation)
            if(isTracking.value!!)
            {
                addLocation.locations.let { locations ->
                    for(location in locations)
                    {
                        addPathPoint(location)//Add the location to the end of the Polyline
                        Timber.d("New Locations: ${location.latitude} _ ${location.longitude}")
                    }
                }
            }
        }
    }

    //Add coordinates to the present polyline
    private fun addPathPoint(location: Location?)
    {
        location?.let {
            //Convert to LatLng
            val pos = LatLng(location.latitude, location.longitude)
            //Add the pos to the present polyline
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    //Add the first Polyline
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    //Start the foreground service
    private fun startForeGroundService()
    {
        addEmptyPolyline()
        isTracking.postValue(true)
        //Get a reference to the notification manager
        val notiManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Check the Android Version
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            createNotificationChannel(notiManager)
        }

        //Build the Notification
        val notiBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)//Can't be swipe away
            .setSmallIcon(R.drawable.ic_baseline_directions_run_24)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("00:00:00")//Initial time, will be updated by seconds
            .setContentIntent(getMainActivityPendingIntent())
        startForeground(NOTIFICATION_ID,notiBuilder.build())
    }

    //Lead the user to the Tracking Fragment when the user presses the notification
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        }, FLAG_IMMUTABLE
       //Update the intent instead of recreate

    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(noti:NotificationManager){
        //Create a channel
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW //Higher than this will vibrate the phone come along with sound
        )
        noti.createNotificationChannel(channel)
    }
}