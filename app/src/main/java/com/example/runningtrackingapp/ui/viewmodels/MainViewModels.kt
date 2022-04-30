package com.example.runningtrackingapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningtrackingapp.db.Run
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

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRunFromMainRepository(run)
    }
}