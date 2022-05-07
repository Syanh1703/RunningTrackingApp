package com.example.runningtrackingapp.other

import android.content.Context
import com.example.runningtrackingapp.db.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.marker_view.view.*
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkView(
    val runList: List<Run>,
    context: Context,
    layoutId: Int
) : MarkerView(context, layoutId) {

    override fun getOffset(): MPPointF {
        return MPPointF(width/2f, -height.toFloat())//specify the position where to show
    }
    override fun refreshContent(entry: Entry?, highlight: Highlight?) {
        super.refreshContent(entry, highlight)
        if(entry == null){
            return
        }
        //Display the run id
        val currentRunID = entry.x.toInt()
        val run = runList[currentRunID]

        //Handle the date
        val inDateDisplay = Calendar.getInstance().apply {
            timeInMillis = run.timeStamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        tvCustomViewDate.text = dateFormat.format(inDateDisplay.time)

        //The avg speed
        val avgSpeedDisplay = "${run.avgSpeedInKMH}km/h"
        tvCustomViewAvgSpeed.text = avgSpeedDisplay

        //The total distance
        val distanceDisplay = "${run.distanceInMeter/1000f}km"
        tvCustomViewDistance.text = distanceDisplay

        //The calories
        val caloriesDisplay = "${run.caloriesBurnt/1000f}kcal"
        tvCustomViewCaloriesBurned.text = caloriesDisplay

        //Run time
        tvCustomViewDuration.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)
    }
}