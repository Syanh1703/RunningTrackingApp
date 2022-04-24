package com.example.runningtrackingapp.other

import com.example.runningtrackingapp.R

object Constants {
    const val RUNNING_DATABASE_NAME = "running_db"
    const val REQUEST_LOCATION_CODE = 1804

    //Status of the tracking service
    const val TRACKING_SERVICE_START_OR_RESUME = "Start or Resume Tracking Service"
    const val TRACKING_SERVICE_PAUSE = "Pause Tracking Service"
    const val TRACKING_SERVICE_STOP = "Stop Tracking Service"

    //Show the Tracking Fragment when the user presses the notification
    const val ACTION_SHOW_TRACKING_FRAGMENT = "Show Tracking Fragment"

    //For Notification
    const val NOTIFICATION_CHANNEL_ID = "Tracking Channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1 //Must not be 0

    //Set average interval
    const val LOCATION_UPDATE_INTERVAL = 3000L
    const val FASTEST_LOCATION_UPDATE_INTERVAL = 1500L

    //Polyline Colors and Width
    const val POLYLINE_COLOR = R.color.errorColor
    const val POLYLINE_WIDTH = 8f

    //Camera zoom
    const val MAP_CAMERA_ZOOM = 20f
}