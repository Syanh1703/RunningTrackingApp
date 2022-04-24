package com.example.runningtrackingapp.other

import android.Manifest
import android.content.Context
import android.os.Build
import pub.devrel.easypermissions.EasyPermissions

object TrackingUtility {
    fun hasLocationPermission(context:Context) =
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
        {
            //Do not need to request back ground location for phone using older than Android Q
            EasyPermissions.hasPermissions(context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        else
        {   //For devices older than Android Q
            EasyPermissions.hasPermissions(context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)
        }
}