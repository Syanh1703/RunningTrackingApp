package com.example.runningtrackingapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runningtrackingapp.R
import com.example.runningtrackingapp.db.Run
import com.example.runningtrackingapp.other.TrackingUtility
import kotlinx.android.synthetic.main.item_run.view.*
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter :RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(itemView:View):RecyclerView.ViewHolder(itemView)

    //Compare two runs objects
    val diffCallback = object :DiffUtil.ItemCallback<Run>()
    {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            //Use hashcode to compare the difference in items
            return oldItem.hashCode() == newItem.hashCode() //Ensure the two objects are 100% the same
        }

    }

    //Construct list difference
    val listDiffer = AsyncListDiffer(this, diffCallback)

    fun submitList(list:List<Run>) = listDiffer.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.item_run, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = listDiffer.currentList[position]
        holder.itemView.apply {
            //Load the image into the imageView
            Glide.with(this).load(run.img).into(ivRunImage)

            //Handle the date
            val inDateDisplay = Calendar.getInstance().apply {
                timeInMillis = run.timeStamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            tvDate.text = dateFormat.format(inDateDisplay.time)

            //The avg speed
            val avgSpeedDisplay = "${run.avgSpeedInKMH}km/h"
            tvAvgSpeed.text = avgSpeedDisplay

            //The total distance
            val distanceDisplay = "${run.distanceInMeter/1000f}km"
            tvDistance.text = distanceDisplay

            //The calories
            val caloriesDisplay = "${run.caloriesBurnt/1000f}kcal"
            tvCalories.text = caloriesDisplay

            //Run time
            tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)
        }
    }

    override fun getItemCount(): Int {
        return listDiffer.currentList.size
    }
}