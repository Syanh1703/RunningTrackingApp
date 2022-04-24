package com.example.runningtrackingapp.repositories

import com.example.runningtrackingapp.db.Run
import com.example.runningtrackingapp.db.RunDAO
import javax.inject.Inject

/**
 * Collect the data from the data sources
 */
class MainRepository  @Inject constructor(
    val runDAO: RunDAO
){
    //Call the function of the Run DAO
    suspend fun insertRun(run:Run) = runDAO.insertRun(run)

    suspend fun deleteRun(run: Run) = runDAO.deleteRun(run)

    fun getAllRunSortedByDate() = runDAO.getAllRunsSortedByDate()

    fun getAllRunSortedByTimeFinished() = runDAO.getAllRunsSortedByTimeFinished()

    fun getAllRunSortedByCaloriesBurnt() = runDAO.getAllRunsSortedByCaloriesBurnt()

    fun getALlRunSortedBySpeed() = runDAO.getAllRunsSortedBySpeed()

    fun getAllRunSortedByDistance() = runDAO.getAllRunsSortedByDistance()

    //For Statistics Fragment
    fun getTotalAvgSpeed() = runDAO.getTotalAvgSpeed()

    fun getTotalDistance() = runDAO.getTotalDistance()

    fun getTotalCaloriesBurnt() = runDAO.getTotalCalories()

    fun getTotalTimeInMillis() = runDAO.getTotalTimeInMillis()
}