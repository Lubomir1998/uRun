package com.example.urun.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urun.data.Run
import com.example.urun.repository.Repository
import kotlinx.coroutines.launch

class StatisticsViewModel
@ViewModelInject constructor(private val repository: Repository): ViewModel(){

    val listOfRunsByDate: LiveData<List<Run>> = repository.getRunsByDate()
    val listOfRunsByDuration: LiveData<List<Run>> = repository.getRunsByDuration()
    val listOfRunsByDistance: LiveData<List<Run>> = repository.getRunsByDistance()
    val listOfRunsByCalories: LiveData<List<Run>> = repository.getRunsByCalories()
    val listOfRunsByAvgPace: LiveData<List<Run>> = repository.getRunsByAvgPace()
    val totalDistanceInMetres: LiveData<Int> = repository.getTotalDistance()
    val totalCalories: LiveData<Float> = repository.getTotalCalories()
    val totalAvgPace: LiveData<Float> = repository.getTotalAvgPace()
    val totalDuration: LiveData<Long> = repository.getTotalDuration()


}