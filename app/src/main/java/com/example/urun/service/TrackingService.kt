package com.example.urun.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.urun.other.Constants.ACTION_FINISH
import com.example.urun.other.Constants.ACTION_PAUSE
import com.example.urun.other.Constants.ACTION_START_OR_RESUME
import com.example.urun.other.Constants.LOCATION_FASTEST_INTERVAL
import com.example.urun.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.urun.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.urun.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.urun.other.Constants.NOTIFICATION_ID
import com.example.urun.other.FormatStopwatchTime
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService: LifecycleService() {

    // Chronometer vars
    var lapRun = 0L
    var totalRun = 0L
    var timeStarted = 0L
    var lastSecondTimestamp = 0L
    var isChronometerEnabled = false

    var isServiceKilled = false
    var isFirstRun = true
    private val TAG = "TrackingService"
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var timeRunInSeconds = MutableLiveData<Long>()

    @Inject
    lateinit var baseNotification: NotificationCompat.Builder

    lateinit var currentNotification: NotificationCompat.Builder

    // vars used in the RunFragment
    companion object {
        var isTracking = MutableLiveData<Boolean>()
        var pathLines = MutableLiveData<Polylines>()
        var timeRunInMillis = MutableLiveData<Long>()
    }

    private fun initLiveDataValues(){
        isTracking.postValue(null)
        pathLines.postValue(mutableListOf())
        timeRunInMillis.postValue(0L)
        timeRunInSeconds.postValue(0L)
    }

    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()

        currentNotification = baseNotification
        initLiveDataValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, {
            it?.let {
            updateTracking(it)
            }
        })
    }


    private fun addEmptyLine() = pathLines.value?.apply {
        add(mutableListOf())
        pathLines.postValue(this)
    } ?: pathLines.postValue(mutableListOf(mutableListOf()))


    private fun addPathPoints(location: Location?){
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathLines.value?.apply {
                last().add(pos)
                pathLines.postValue(this)
            }
        }
    }


    private val locationCallback = object: LocationCallback(){
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)

            if(isTracking.value != null && isTracking.value!!){
                result?.locations?.let { locations ->
                    CoroutineScope(Dispatchers.Main).launch {
                        Log.d(TAG, "*******onLocationResult: ${locations.size}")
                    }
                    for(location in locations){
                        addPathPoints(location)
                        CoroutineScope(Dispatchers.Main).launch {
                            Log.d(
                                TAG,
                                "*****onLocationResult: ${location.latitude} and ${location.longitude}"
                            )
                        }
                    }
                }
            }

        }
    }

    private fun killService(){
        isServiceKilled = true
        isFirstRun = true
        isChronometerEnabled = false
        initLiveDataValues()
        stopForeground(true)
        stopSelf()
    }

    private fun updateTracking(isTracking: Boolean){
        if(isTracking){
            val request = LocationRequest().apply {
                interval = LOCATION_UPDATE_INTERVAL
                fastestInterval = LOCATION_FASTEST_INTERVAL
                priority = PRIORITY_HIGH_ACCURACY
            }

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationProviderClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when(it.action){

                ACTION_START_OR_RESUME -> {
                    // starting the run
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    }
                    // resuming the run (after pause)
                    else {
                        startChronometer()
                    }
                }

                ACTION_PAUSE -> {
                    isTracking.postValue(false)
                    isChronometerEnabled = false
                }

                ACTION_FINISH -> {
                    killService()
                }

                else -> Log.d(TAG, "**: else")

            }
        }


        return super.onStartCommand(intent, flags, startId)
    }


    private fun startChronometer(){
        addEmptyLine()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isChronometerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value != null && isTracking.value!!){
                lapRun = System.currentTimeMillis() - timeStarted
                timeRunInMillis.postValue(totalRun + lapRun)
                if(timeRunInMillis.value!! >= lastSecondTimestamp + 1000L){
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(50L)
            }

            totalRun += lapRun
        }
    }


    private fun startForegroundService(){
        startChronometer()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotification.build())


        timeRunInSeconds.observe(this, {
            if(!isServiceKilled) {
                val notification = currentNotification
                        .setContentText(FormatStopwatchTime.getFormattedStopwatchTimeToString(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(channel)
    }




}