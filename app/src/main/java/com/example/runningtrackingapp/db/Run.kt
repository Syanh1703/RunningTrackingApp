package com.example.runningtrackingapp.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_table")
//Do not put the primary key in the constructor

data class Run (
    var img:Bitmap? = null,
    var timeStamp:Long = 0L, //Determine the date of the run
    var avgSpeedInKMH:Float = 0f,
    var distanceInMeter :Float = 0f,
    var timeInMillis:Long = 0L, //Determines the time of each run
    var caloriesBurnt :Int = 0)
{
    @PrimaryKey(autoGenerate = true)
    var id :Int? = null
}