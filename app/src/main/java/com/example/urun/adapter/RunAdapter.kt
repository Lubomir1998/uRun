package com.example.urun.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.urun.data.Run
import com.example.urun.databinding.RunItemBinding
import com.example.urun.other.FormatStopwatchTime
import java.math.RoundingMode
import java.text.DateFormatSymbols
import java.util.*

class RunAdapter(var listOfRuns: List<Run>): RecyclerView.Adapter<RunAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = RunItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val run = listOfRuns[position]

        holder.runImage.setImageBitmap(run.image)
        holder.caloriesTextView.text = run.calories.toString()
        holder.avgSpeedTextView.text = run.avgPace.toString()
        holder.distanceTextView.text = "${run.distanceInMetres / 1000f}"
        holder.durationTextView.text = FormatStopwatchTime.getFormattedStopwatchTimeToString(run.duration, false, false)

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = run.timestamp

        val year = calendar.get(Calendar.YEAR)
        val month_ = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val month = getMonth(month_)

        holder.dateTextView.text = "$day $month $year"

    }

    override fun getItemCount(): Int = listOfRuns.size

    private fun getMonth(month: Int): String? {
        return DateFormatSymbols().months[month]
    }

    class MyViewHolder(itemView: RunItemBinding): RecyclerView.ViewHolder(itemView.root){

        val runImage = itemView.runImageView
        val dateTextView = itemView.dateTextView
        val durationTextView = itemView.durationTextView
        val avgSpeedTextView = itemView.avgSpeedTextView
        val caloriesTextView = itemView.caloriesTextView
        val distanceTextView = itemView.distanceTextView

    }
}