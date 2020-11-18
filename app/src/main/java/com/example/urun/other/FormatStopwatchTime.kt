package com.example.urun.other

import android.location.Location
import com.example.urun.service.Polyline
import java.util.concurrent.TimeUnit

object FormatStopwatchTime {

    fun getRunDistance(polyline: Polyline): Float {

        var distance = 0f

        for(i in 0..polyline.size - 2){
            val pos1 = polyline[i]
            val pos2 = polyline[i + 1]

            val result = FloatArray(1)

            Location.distanceBetween(
                    pos1.latitude, pos1.longitude,
                    pos2.latitude, pos2.longitude,
                    result
            )

            distance += result[0]

        }

        return distance
    }

    fun getAvgPace(avgPace: Float): String {
        val avgPaceSeconds = (avgPace - avgPace.toLong()) * 10
        val seconds = ((avgPaceSeconds / 10) * 60).toLong()


        return "${avgPace.toLong()}:${if(seconds < 10) "0$seconds" else seconds}"
    }

    fun getFormattedStopwatchTimeToString(millisecond: Long, includeMillis: Boolean = false, includeHours: Boolean = true): String{
        var millis = millisecond

        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        millis -= TimeUnit.HOURS.toMillis(hours)
        var minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        millis -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        if(!includeHours){
            minutes += hours * 60

            return  "${if(minutes < 10) "0" else ""}$minutes:" +
                    "${if(seconds < 10) "0" else ""}$seconds"
        }
        if(!includeMillis){
            return "${if(hours < 10) "0" else ""}$hours:" +
                    "${if(minutes < 10) "0" else ""}$minutes:" +
                    "${if(seconds < 10) "0" else ""}$seconds"
        }

        millis -= TimeUnit.SECONDS.toMillis(seconds)
        millis /= 10

        return "${if(hours < 10) "0" else ""}$hours:" +
                "${if(minutes < 10) "0" else ""}$minutes:" +
                "${if(seconds < 10) "0" else ""}$seconds:" +
                "${if(millis < 10) "0" else ""}$millis"


    }


}