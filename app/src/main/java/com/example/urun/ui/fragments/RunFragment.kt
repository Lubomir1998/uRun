package com.example.urun.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.urun.BuildConfig.API_KEY
import com.example.urun.R
import com.example.urun.data.Run
import com.example.urun.other.Constants.ACTION_START_OR_RESUME
import com.example.urun.databinding.RunFragmentBinding
import com.example.urun.other.Constants.ACTION_FINISH
import com.example.urun.other.Constants.ACTION_PAUSE
import com.example.urun.other.FormatStopwatchTime
import com.example.urun.service.Polyline
import com.example.urun.service.TrackingService
import com.example.urun.ui.dialogs.CancelRunDialog
import com.example.urun.viewModels.RunViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class RunFragment: Fragment(R.layout.run_fragment), OnMapReadyCallback {

    private lateinit var binding: RunFragmentBinding
    private val model: RunViewModel by viewModels()
    private var map: GoogleMap? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mLocationPermissionsGranted = false
    private var isTracking = false
    private var pathLines = mutableListOf<Polyline>()
    private var currentTimeInMillis = 0L

    private  val TAG = "RunFragment"

    @set:Inject
    var weight: Float = 70f

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
        const val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        const val COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = RunFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.map.onCreate(savedInstanceState)

        // when the screen rotates, the lambda field is set to null
        // which means the stopRun() fun won't get called the first time
        // ..................
        // if savedInstanceState is null it means we haven't experienced screen rotation
        if(savedInstanceState != null){
            val cancelDialog = parentFragmentManager.findFragmentByTag("CancelTag") as CancelRunDialog?
            cancelDialog?.setYesListener {
                stopRun()
            }
        }

        getLocationPermission()

        subscribeToLiveData()

        binding.closeImg.setOnClickListener {
            showDialog()
        }


        binding.startOrStopBtn.setOnClickListener {
            pressStartOrStopBtn()
        }

        binding.finishBtn.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveInDB()
        }


    }


    private fun initMap(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        binding.map.getMapAsync {
            map = it
            getDeviceLocation()
            addAllPolylines()
        }

    }


    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap ?: return

        if(mLocationPermissionsGranted) {
            //getDeviceLocation()
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            map!!.isMyLocationEnabled = true
            map!!.uiSettings.isMyLocationButtonEnabled = true
        }


    }

    private fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.Builder()

        for(polyline in pathLines){
            for(position in polyline){
                bounds.include(position)
            }
        }

        map?.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                        bounds.build(),
                        binding.map.width,
                        binding.map.height,
                        (binding.map.width * 0.05f).toInt()
                )
        )

    }

    private fun endRunAndSaveInDB(){
        map?.snapshot { bitmap ->
            var distanceInMetres = 0
            for(polyline in pathLines){
                distanceInMetres += FormatStopwatchTime.getRunDistance(polyline).toInt()
            }
            val timestamp = Calendar.getInstance().timeInMillis
            val calories = ((distanceInMetres / 1000f ) * weight).toBigDecimal().setScale(2, RoundingMode.HALF_UP).toFloat()
            val avgPace = ((currentTimeInMillis / 1000f / 60) / (distanceInMetres / 1000f)).toBigDecimal().setScale(2, RoundingMode.FLOOR).toFloat()

            val run = Run(timestamp, distanceInMetres, currentTimeInMillis, calories, avgPace, bitmap)
            model.insertRun(run)
            Snackbar.make(requireActivity().findViewById(R.id.rootView), "Run saved successfully", Snackbar.LENGTH_LONG).show()
            stopRun()

        }
    }

    private fun stopRun(){
        //binding.timeTextView.text = "00:00:00:00"
        val intent = Intent(requireContext(), TrackingService::class.java)
        intent.action = ACTION_FINISH
        requireContext().startService(intent)
        findNavController().navigate(R.id.action_runFragment_to_myRunsFragment)
    }

    private fun showDialog(){
        CancelRunDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, "CancelTag")
    }

    @SuppressLint("VisibleForTests")
    private fun getDeviceLocation(){
        mFusedLocationClient = FusedLocationProviderClient(requireContext())

        if(mLocationPermissionsGranted){
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            val location = mFusedLocationClient.lastLocation
            location.addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    if(task.result != null) {
                        val currentLocation = task.result as Location

                        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
                        val zoomLevel = 17f

                        moveCamera(latLng, zoomLevel, "My Location")
                    }
                }
            }

        }

    }


    private fun addLatestPolyline(){
        if(isTracking && pathLines.last().size > 1){
            val preLastLatLng = pathLines.last()[pathLines.last().size - 2]
            val lastLatLng = pathLines.last().last()
            val polylineOptions = PolylineOptions()
                    .color(R.color.purple)
                    .width(10f)
                    .add(preLastLatLng)
                    .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addAllPolylines(){
        for(polyline in pathLines){
            val polylineOptions = PolylineOptions()
                    .color(R.color.purple)
                    .width(10f)
                    .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun animateCamera(){
        if(pathLines.isNotEmpty() && pathLines.last().isNotEmpty()){
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(pathLines.last().last(), 17f))
        }
    }

    private fun updateTracking(isTracking: Boolean){
        this.isTracking = isTracking
        if(isTracking){
            binding.startOrStopBtn.text = "Stop"
            binding.finishBtn.visibility = View.GONE
            binding.closeImg.visibility = View.VISIBLE
        }
        else{
            binding.startOrStopBtn.text = "Start"
            binding.finishBtn.visibility = View.VISIBLE
            binding.closeImg.visibility = View.VISIBLE
        }
    }

    private fun pressStartOrStopBtn(){
        if(isTracking){
            binding.closeImg.visibility = View.VISIBLE
            val intent = Intent(requireContext(), TrackingService::class.java)
            intent.action = ACTION_PAUSE
            requireContext().startService(intent)
        }
        else{
            binding.closeImg.visibility = View.VISIBLE
            val intent = Intent(requireContext(), TrackingService::class.java)
            intent.action = ACTION_START_OR_RESUME
            requireContext().startService(intent)
        }
    }

    private fun subscribeToLiveData(){
        TrackingService.isTracking.observe(viewLifecycleOwner, {
            if(it != null) {
                updateTracking(it)
            }
            else{
                CoroutineScope(Dispatchers.Main).launch {
                    binding.finishBtn.visibility = View.GONE
                }
            }
        })

        TrackingService.pathLines.observe(viewLifecycleOwner, {
            pathLines = it
            addLatestPolyline()
            animateCamera()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, {
            if(TrackingService.isTracking.value != null) {
                currentTimeInMillis = it
                val formattedTime = FormatStopwatchTime.getFormattedStopwatchTimeToString(currentTimeInMillis, true)
                CoroutineScope(Dispatchers.Main).launch {
                    binding.timeTextView.text = formattedTime
                }
            }
        })
    }

    private fun moveCamera(latLng: LatLng, zoomLevel: Float, title: String){
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))

        // add marker on recent location
        if(title != "My Location") {
            val marker = MarkerOptions().position(latLng).title(title)
            map!!.addMarker(marker)
        }

    }

    private fun getLocationPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mLocationPermissionsGranted = true
                initMap()
            } else {
                requestPermissions(
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            requestPermissions(
                permissions,
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                mLocationPermissionsGranted = true
                initMap()
            }
        }
    }



    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val mapView: MapView? = requireActivity().findViewById(R.id.map)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        binding.map.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.map.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.map.onLowMemory()
    }


}