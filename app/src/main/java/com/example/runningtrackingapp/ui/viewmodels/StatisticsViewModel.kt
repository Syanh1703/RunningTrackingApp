package com.example.runningtrackingapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.runningtrackingapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Collect the data from the repository and provide to the related Fragment
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val mainRepository: MainRepository
): ViewModel(){
}