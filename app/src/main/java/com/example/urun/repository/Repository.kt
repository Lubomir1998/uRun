package com.example.urun.repository

import androidx.lifecycle.LiveData
import androidx.room.Query
import com.example.urun.data.Run
import com.example.urun.data.RunDao
import javax.inject.Inject

class Repository
@Inject constructor(private val dao: RunDao){

    suspend fun insert(run: Run) = dao.insert(run)

    suspend fun delete(run: Run) = dao.delete(run)

    fun getAllRuns() = dao.getAllRuns()

    fun getRunsByDate() = dao.getRunsByDate()

    fun getRunsByDuration() = dao.getRunsByDuration()

    fun getRunsByDistance() = dao.getRunsByDistance()

    fun getRunsByCalories() = dao.getRunsByCalories()

    fun getRunsByAvgPace() = dao.getRunsByAvgPace()

    fun getTotalDistance() = dao.getTotalDistance()

    fun getTotalCalories() = dao.getTotalCalories()

    fun getTotalAvgPace() = dao.getTotalAvgPace()

    fun getTotalDuration() = dao.getTotalDuration()


}