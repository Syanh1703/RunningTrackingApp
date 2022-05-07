package com.example.runningtrackingapp.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDAO {
    //Insert New Run
    @Insert(onConflict = OnConflictStrategy.REPLACE)//The new run will replace the old run
    suspend fun insertRunFromRunDAO(run:Run)

    //Delete the run
    @Delete
    suspend fun deleteRunFromRunDAO(run:Run)

    //Sort the run by date, the latest run on top
    @Query("SELECT * FROM running_table ORDER BY timestamp DESC")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    //Sort the run by distance, the longest on top
    @Query("SELECT * FROM running_table ORDER BY distanceInMeter DESC")
    fun getAllRunsSortedByDistance():LiveData<List<Run>>

    //Sort the run by time finished, the longest on top
    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC")
    fun getAllRunsSortedByTimeFinished():LiveData<List<Run>>

    //Sort the run by speed, the fastest on top
    @Query("SELECT * FROM running_table ORDER BY avgSpeedInKMH DESC")
    fun getAllRunsSortedBySpeed():LiveData<List<Run>>

    //Sort the run by calories burnt, the highest on top
    @Query("SELECT * FROM running_table ORDER BY caloriesBurnt DESC")
    fun getAllRunsSortedByCaloriesBurnt():LiveData<List<Run>>

    //For Statistics Fragment
    @Query("SELECT SUM(timeInMillis) FROM running_table")
    fun getTotalTimeInMillis():LiveData<Long>

    //For Statistics Fragment
    @Query("SELECT SUM(distanceInMeter) FROM running_table")
    fun getTotalDistance():LiveData<Float>

    //For Statistics Fragment
    @Query("SELECT SUM(caloriesBurnt) FROM running_table")
    fun getTotalCalories():LiveData<Float>

    //For Statistics Fragment
    @Query("SELECT AVG(avgSpeedInKMH) FROM running_table")
    fun getTotalAvgSpeed():LiveData<Float>
}