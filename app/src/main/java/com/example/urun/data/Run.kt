package com.example.urun.data

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.urun.ui.fragments.LoginFragmentDirections

@Entity
data class Run (
        @PrimaryKey
        var timestamp: Long = 0L,
        var distanceInMetres: Int = 0,
        var duration: Long = 0L,
        var calories: Float = 0f,
        var avgPace: Float = 0f,
        val image: Bitmap? = null
)