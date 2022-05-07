package com.example.runningtrackingapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.runningtrackingapp.R
import com.example.runningtrackingapp.other.CustomMarkView
import com.example.runningtrackingapp.other.TrackingUtility
import com.example.runningtrackingapp.ui.viewmodels.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_stats.*
import kotlinx.android.synthetic.main.item_run.*
import kotlin.math.round

@AndroidEntryPoint
class StatsFragment : Fragment(R.layout.fragment_stats) {

    private val statsFragmentViewModel: StatisticsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToObserver()
        setUpBarChart()
    }

    //Setup the bar chart
    private fun setUpBarChart() {
        barChart.xAxis.apply {
            //Set the x Axis to the bottom of the screen
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = R.color.white
            textColor = R.color.white
            setDrawGridLines(false)
        }

        barChart.axisLeft.apply {
            axisLineColor = R.color.white
            setDrawGridLines(false)
            textColor = R.color.white
        }
        barChart.axisRight.apply {
            axisLineColor = R.color.white
            setDrawGridLines(false)
            textColor = R.color.white
        }
        barChart.apply {
            description.text = getString(R.string.avg_speed_over_time)
            legend.isEnabled = false
            //Fill the chart with entries

        }
    }

    private fun subscribeToObserver() {
        statsFragmentViewModel.totalTimeRun.observe(viewLifecycleOwner) {
            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
                tvTotalTime.text = totalTimeRun

            }
        }

        statsFragmentViewModel.totalDistance.observe(viewLifecycleOwner) {
            it?.let {
                val numOfKM = it / 1000f
                val totalDistance = round(numOfKM * 10f) / 10f
                val totalDistanceString = "${totalDistance}km"
                tvTotalDistance.text = totalDistanceString
            }
        }

        statsFragmentViewModel.avgSpeed.observe(viewLifecycleOwner) {
            it?.let {
                val avgSpeed = round(it * 10f) / 10f
                val avgSpeedString = "${avgSpeed}km/h"
                tvAverageSpeed.text = avgSpeedString
            }
        }

        statsFragmentViewModel.totalCaloriesBurnt.observe(viewLifecycleOwner) {
            it?.let {
                tvCalories.text = "${it}kcal"
            }
        }

        statsFragmentViewModel.runSortedByDate.observe(viewLifecycleOwner){
            it?.let {
                //Create a list of bar chart entries, which contains the x and y values
                //x will the index of run
                //y will the avg speed
                val allAvgSpeed = it.indices.map {
                    i -> BarEntry(i.toFloat(), it[i].avgSpeedInKMH)
                }
                val barEntriesDataSet = BarDataSet(allAvgSpeed,getString(R.string.avg_speed_over_time)).apply {
                    valueTextColor = R.color.white
                    color = ContextCompat.getColor(requireContext(),R.color.colorAccent)
                }
                barChart.data = BarData(barEntriesDataSet)
                /**
                 * Assign the marker view created in the Custom Marker to bar chart
                 */
                barChart.marker = CustomMarkView(it.reversed(), requireContext(), R.layout.marker_view)
                barChart.invalidate()
            }
        }
    }
}