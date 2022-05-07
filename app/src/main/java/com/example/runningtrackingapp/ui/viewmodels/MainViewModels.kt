package com.example.runningtrackingapp.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningtrackingapp.db.Run
import com.example.runningtrackingapp.di.SortType
import com.example.runningtrackingapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Collect the data from the repository and provide to the related Fragment
 */
@HiltViewModel
class MainViewModels @Inject constructor(
    val mainRepository: MainRepository
): ViewModel(){

    private val runsSortedByDate = mainRepository.getAllRunSortedByDate()
    private val runsSortedByTimeFinished = mainRepository.getAllRunSortedByTimeFinished()
    private val runsSortedBySpeed = mainRepository.getALlRunSortedBySpeed()
    private val runsSortedByDistance = mainRepository.getAllRunSortedByDistance()
    private val runsSortedByCalories = mainRepository.getAllRunSortedByCaloriesBurnt()

    //Create a new LiveData
    val runMediatorLiveData = MediatorLiveData<List<Run>>()

     var sortType :SortType = SortType.DATE //Set the default value to date when opening the app

    init {
        //Merge all LiveData together
        runMediatorLiveData.addSource(runsSortedByDate){ result ->
            if(sortType == SortType.DATE)
            {
                result!!.let {
                    runMediatorLiveData.value = it
                }
            }
        }

        runMediatorLiveData.addSource(runsSortedByCalories){ resutlt ->
            if(sortType == SortType.CALORIES_BURNT)
            {
                resutlt!!.let {
                    runMediatorLiveData.value = it
                }
            }
        }

        runMediatorLiveData.addSource(runsSortedByDistance){ result ->
            if(sortType == SortType.DISTANCE)
            {
                result!!.let {
                    runMediatorLiveData.value = it
                }
            }
        }

        runMediatorLiveData.addSource(runsSortedBySpeed){ result ->
            if(sortType == SortType.AVG_SPEED)
            {
                result!!.let {
                    runMediatorLiveData.value = it
                }
            }
        }

        runMediatorLiveData.addSource(runsSortedByTimeFinished){ result ->
            if(sortType == SortType.RUNNING_TIME)
            {
                result!!.let {
                    runMediatorLiveData.value = it
                }
            }
        }
    }

    //Function to alert changes of the sortType
    fun sortRuns(runsSort:SortType) = when(runsSort)
    {
        SortType.DATE -> runsSortedByDate.value?.let { runMediatorLiveData.value = it }
        SortType.RUNNING_TIME -> runsSortedByTimeFinished.value?.let { runMediatorLiveData.value = it }
        SortType.AVG_SPEED -> runsSortedBySpeed.value?.let { runMediatorLiveData.value = it }
        SortType.DISTANCE -> runsSortedByDistance.value?.let { runMediatorLiveData.value = it }
        SortType.CALORIES_BURNT -> runsSortedByCalories.value?.let { runMediatorLiveData.value = it }
    }.also {
        this.sortType = runsSort
    }

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRunFromMainRepository(run)
    }
}