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
import com.example.runningtrackingapp.other.Constants.DELAY_COROUTINE_TIME
import com.example.runningtrackingapp.other.Constants.FASTEST_LOCATION_UPDATE_INTERVAL
import com.example.runningtrackingapp.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runningtrackingapp.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runningtrackingapp.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runningtrackingapp.other.Constants.NOTIFICATION_ID
import com.example.runningtrackingapp.other.Constants.PAUSE_CURRENT_NOTIFICATION_CODE
import com.example.runningtrackingapp.other.Constants.RESUME_CURRENT_NOTIFICATION
import com.example.runningtrackingapp.other.Constants.TRACKING_SERVICE_PAUSE
import com.example.runningtrackingapp.other.Constants.TRACKING_SERVICE_START_OR_RESUME
import com.example.runningtrackingapp.other.Constants.TRACKING_SERVICE_STOP
import com.example.runningtrackingapp.other.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias multiPolyline = MutableList<Polyline>

@AndroidEntryPoint
class TrackingServices : LifecycleService() {

    var isFirstRun = true
    var isServiceKilled = false

    //Inject everything from the Service Module
    @Inject
    lateinit var fuseLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var currentNotificationBuilder: NotificationCompat.Builder

    //Variables for stopwatch
    private val timeRunInSeconds = MutableLiveData<Long>()

    companion object {
        //Observe the data from the outside
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<multiPolyline>()
    }

    private fun postInitialValue() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())

        //Initial the timer
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        currentNotificationBuilder = baseNotificationBuilder
        postInitialValue()
        fuseLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            //Get updates when states changing
            updateLocationTracking(it)
            //Get updates when the notification changing
            updateNotificationTrackingState(it)
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
            when (it.action) {
                TRACKING_SERVICE_START_OR_RESUME -> {
                    //Call out the startForeground Service function
                    if (isFirstRun) {
                        startForeGroundService()
                        isFirstRun = false
                    } else {
                        startTimer()
                        Timber.d("Resuming Service")
                    }
                }
                TRACKING_SERVICE_STOP -> {
                    Timber.d("Stop the tracking service")
                    killServiceWhenCancel()
                }
                TRACKING_SERVICE_PAUSE -> {
                    pauseService()
                    Timber.d("Pause tracking Service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    //Track the actual time and trigger the observer
    private var isTimerEnabled = false
    private var lapTime = 0L //Number of time when the start or the stop btn is pressed
    private var totalTimeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    private fun startTimer() {
        /**
         * This function starts when user presses the start or resume btn
         */
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        //User Coroutine to start/stop the current time
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                //Time difference between now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted

                //Update value laptime
                timeRunInMillis.postValue(totalTimeRun + lapTime)

                if (timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L) {
                    /**
                     * If the timeRunInMillis is 1550ms, so the lastSecondTimeStamp will be 1000ms
                     * If the timeRunInMillis is greater than lastSecondTimeStamp + 10000, we should know it is the right time to update the timeRunInSecond
                     */
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                //Delay the coroutine
                delay(DELAY_COROUTINE_TIME)
            }

            totalTimeRun += lapTime
        }
    }

    //Kill the service
    private fun killServiceWhenCancel() {
        isServiceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValue()
        stopForeground(true)
        stopSelf()
    }

    //Update the notification
    private fun updateNotificationTrackingState(isTracking: Boolean) {
        //Specify the action text
        val notificationActionText = if (isTracking) {
            getString(R.string.pause_run)
        } else {
            getString(R.string.resume_run)
        }
        //Pending Intent
        val pendingIntent = if (isTracking) {
            //Pause the service when clicking that action
            val pauseIntent = Intent(this, TrackingServices::class.java).apply {
                action = TRACKING_SERVICE_PAUSE
            }
            PendingIntent.getService(
                this,
                PAUSE_CURRENT_NOTIFICATION_CODE,
                pauseIntent,
                FLAG_IMMUTABLE
            )
        } else {
            val resumeIntent = Intent(this, TrackingServices::class.java).apply {
                action = TRACKING_SERVICE_START_OR_RESUME
            }
            PendingIntent.getService(
                this,
                RESUME_CURRENT_NOTIFICATION,
                resumeIntent,
                FLAG_IMMUTABLE
            )
        }

        //Reference to the Notification Manager
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Swipe out the action when the user click on it
        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            //Set these to an empty list
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if(!isServiceKilled)
        {
            currentNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }
    }

    //Pause the tracking
    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    //Update location tracking
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermission(this)) {
                //Request Location Updates
                val locationRequest =
                    com.google.android.gms.location.LocationRequest.create().apply {
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
        } else {
            fuseLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(addLocation: LocationResult) {
            super.onLocationResult(addLocation)
            if (isTracking.value!!) {
                addLocation.locations.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)//Add the location to the end of the Polyline
                        Timber.d("New Locations: ${location.latitude} _ ${location.longitude}")
                    }
                }
            }
        }
    }

    //Add coordinates to the present polyline
    private fun addPathPoint(location: Location?) {
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
    private fun startForeGroundService() {
        startTimer()
        isTracking.postValue(true)
        //Get a reference to the notification manager
        val notiManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Check the Android Version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notiManager)
        }

        //Build the Notification
        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this) {
            //Observe the data
            if (!isServiceKilled) {
                val notification = currentNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
                notiManager.notify(NOTIFICATION_ID, notification.build())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(noti: NotificationManager) {
        //Create a channel
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW //Higher than this will vibrate the phone come along with sound
        )
        noti.createNotificationChannel(channel)
    }
}