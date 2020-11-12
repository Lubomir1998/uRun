package com.example.urun.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(run: Run)

    @Delete
    suspend fun delete(run: Run)

    @Query("SELECT * FROM Run")
    fun getAllRuns(): LiveData<List<Run>>

    @Query("SELECT * FROM Run ORDER BY timestamp DESC")
    fun getRunsByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM Run ORDER BY duration DESC")
    fun getRunsByDuration(): LiveData<List<Run>>

    @Query("SELECT * FROM Run ORDER BY distanceInMetres DESC")
    fun getRunsByDistance(): LiveData<List<Run>>

    @Query("SELECT * FROM Run ORDER BY calories DESC")
    fun getRunsByCalories(): LiveData<List<Run>>

    @Query("SELECT * FROM Run ORDER BY avgPace DESC")
    fun getRunsByAvgPace(): LiveData<List<Run>>

    @Query("SELECT SUM(distanceInMetres) FROM Run")
    fun getTotalDistance(): LiveData<Int>

    @Query("SELECT SUM(calories) FROM Run")
    fun getTotalCalories(): LiveData<Float>

    @Query("SELECT AVG(avgPace) FROM Run")
    fun getTotalAvgPace(): LiveData<Float>

    @Query("SELECT SUM(duration) FROM Run")
    fun getTotalDuration(): LiveData<Long>


}