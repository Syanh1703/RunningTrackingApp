package com.example.runningtrackingapp.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.runningtrackingapp.R
import com.example.runningtrackingapp.ui.viewmodels.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatsFragment:Fragment(R.layout.fragment_stats) {

    private val viewModel : StatisticsViewModel by viewModels()
}