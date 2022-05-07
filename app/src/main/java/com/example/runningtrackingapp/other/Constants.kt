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
    const val POLYLINE_COLOR = R.color.md_blue_500
    const val POLYLINE_WIDTH = 8f

    //Camera zoom
    const val MAP_CAMERA_ZOOM = 20f

    //Delay time for coroutine
    const val DELAY_COROUTINE_TIME = 50L

    //Code for current notification
    const val PAUSE_CURRENT_NOTIFICATION_CODE = 1
    const val RESUME_CURRENT_NOTIFICATION = 2

    //Shared Preferences
    const val SHARED_PREFERENCES_NAME = "sharePref"
    const val KEY_FIRST_TIME_TOGGLE = "KEY_FIRST_TIME_TOGGLE"
    const val KEY_USER_NAME = "KEY_USER_NAME"
    const val KEY_USER_WEIGHT = "KEY_USER_WEIGHT"

    //Cancel Tracking Dialog Tag
    const val CANCEL_TRACKING_DIALOG_TAG = "Cancel Dialog"
}