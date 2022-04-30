package com.example.runningtrackingapp.other

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

object TrackingUtility {
    fun hasLocationPermission(context: Context) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            //Do not need to request back ground location for phone using older than Android Q
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {   //For devices older than Android Q
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }


    //Get the formatted Stopwatch Time
    fun getFormattedStopWatchTime(millis: Long, includesMillis: Boolean = false): String {
        var ms = millis
        val numHour = TimeUnit.MILLISECONDS.toHours(ms)//Convert ms to hour
        ms -= TimeUnit.HOURS.toMillis(numHour)
        val numMin = TimeUnit.MILLISECONDS.toMinutes(ms)
        ms -= TimeUnit.MINUTES.toMillis(numMin)
        val numSecond = TimeUnit.MILLISECONDS.toSeconds(ms)

        val startingTimeWithSeconds = "${if (numHour < 10) "0" else ""}$numHour:" +
                "${if (numMin < 10) "0" else ""}$numMin:" +
                "${if (numSecond < 10) "0" else ""}$numSecond"


        if (!includesMillis) {
            return startingTimeWithSeconds
        }

        ms -= TimeUnit.SECONDS.toMillis(numSecond)
        ms /= 10
        return startingTimeWithSeconds + ":" + "${if (ms < 10) "0" else ""}$ms"
    }

    fun calculatePolylineLength(polyline: com.example.runningtrackingapp.services.Polyline) :Float
    {
        var totalDistance = 0f
        for(i in 0..polyline.size-2)
        {
            val pos1 = polyline[i]
            val pos2 = polyline[i+1]
            val result = FloatArray(1)

            Location.distanceBetween(
                pos1.latitude,
                pos1.longitude,
                pos2.latitude,
                pos2.longitude,
                result
            )
            totalDistance += result[0]
        }
        return totalDistance
    }

}