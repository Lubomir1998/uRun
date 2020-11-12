package com.example.urun.ui.fragments

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.urun.R
import com.example.urun.adapter.RunAdapter
import com.example.urun.data.Run
import com.example.urun.databinding.MyRunsFragmentBinding
import com.example.urun.viewModels.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class MyRunsFragment: Fragment(R.layout.my_runs_fragment) {

    private var runList: List<Run> = mutableListOf()
    private var mAdapter = RunAdapter(runList)
    private lateinit var binding: MyRunsFragmentBinding
    private val model: StatisticsViewModel by viewModels()

    private val TAG = "MyRunsFragment"

    private lateinit var sharedPreferences: SharedPreferences
    var positionSpinner = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = MyRunsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.floatingBtn.setOnClickListener {
            findNavController().navigate(R.id.action_myRunsFragment_to_runActivity)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
            setHasFixedSize(true)
        }

        sharedPreferences = requireContext().getSharedPreferences("spinnersharedPrefs", MODE_PRIVATE)
        positionSpinner = sharedPreferences.getInt("spinner", 0)
        binding.spinner.setSelection(positionSpinner)

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                when(parent?.getItemAtPosition(position).toString()){

                    "Date" -> {
                        model.listOfRunsByDate.observe(requireActivity(), {
                            displayData(it)
                        })
                        sharedPreferences.edit().putInt("spinner", position).apply()
                    }

                    "Calories burned" -> {
                        model.listOfRunsByCalories.observe(requireActivity(), {
                            displayData(it)
                        })
                        sharedPreferences.edit().putInt("spinner", position).apply()
                    }

                    "Distance" -> {
                        model.listOfRunsByDistance.observe(requireActivity(), {
                            displayData(it)
                        })
                        sharedPreferences.edit().putInt("spinner", position).apply()
                    }

                    "Avg speed" -> {
                        model.listOfRunsByAvgPace.observe(requireActivity(), {
                            displayData(it)
                        })
                        sharedPreferences.edit().putInt("spinner", position).apply()
                    }

                    "Duration" -> {
                        model.listOfRunsByDuration.observe(requireActivity(), {
                            displayData(it)
                        })
                        sharedPreferences.edit().putInt("spinner", position).apply()
                    }


                }
            }


            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


    }


    private fun displayData(list: List<Run>){
        mAdapter.listOfRuns = list
        mAdapter.notifyDataSetChanged()
    }

}