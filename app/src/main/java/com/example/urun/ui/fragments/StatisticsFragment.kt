package com.example.urun.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.urun.R
import com.example.urun.databinding.StatisticsFragmentBinding
import com.example.urun.other.FormatStopwatchTime
import com.example.urun.viewModels.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.math.RoundingMode

@AndroidEntryPoint
class StatisticsFragment: Fragment(R.layout.statistics_fragment) {

    private lateinit var binding: StatisticsFragmentBinding
    private val model: StatisticsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = StatisticsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.totalDistanceInMetres.observe(requireActivity(), {
            it?.let {
                binding.totalDistanceTextView.text = "${it / 1000f} km"
            }
        })

        model.totalAvgPace.observe(requireActivity(), {
            it?.let {
                val avgSpeed = it.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toFloat()
                binding.totalAvgSpeedTextView.text = "$avgSpeed min/km"
            }
        })

        model.totalCalories.observe(requireActivity(), {
            it?.let {
                val calories = it.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toFloat()
                binding.totalCaloriesTextView.text = "$calories cal"
            }
        })

        model.totalDuration.observe(requireActivity(), {
            it?.let {
                val totalTime = FormatStopwatchTime.getFormattedStopwatchTimeToString(it)
                binding.totalTimeTextView.text = totalTime
            }
        })


    }

}