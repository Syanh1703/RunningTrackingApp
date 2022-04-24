package com.example.runningtrackingapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.runningtrackingapp.R
import com.example.runningtrackingapp.other.Constants.MAP_CAMERA_ZOOM
import com.example.runningtrackingapp.other.Constants.POLYLINE_COLOR
import com.example.runningtrackingapp.other.Constants.POLYLINE_WIDTH
import com.example.runningtrackingapp.other.Constants.TRACKING_SERVICE_PAUSE
import com.example.runningtrackingapp.other.Constants.TRACKING_SERVICE_START_OR_RESUME
import com.example.runningtrackingapp.services.Polyline
import com.example.runningtrackingapp.services.TrackingServices
import com.example.runningtrackingapp.ui.viewmodels.MainViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModels by viewModels()

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    //Create a Google Map
    private var trackingMap: GoogleMap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView.onCreate(savedInstanceState)

        btnStartRun.setOnClickListener {
            toggleRun()
        }
        mapView.getMapAsync {
            //Set the loaded Map
            trackingMap = it
            connectAllPointsOfPolyline()
        }
        subscribeObserver()
    }

    //Draw a Polyline
    /**
     * When to draw the Polyline?
     * When we get the new location => Connect the last point to the second last point
     */

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            //Reference two coordinates to the last two points of the Polyline
            val secondLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            //Define the color and width of the Polyline
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(secondLastLatLng)
                .add(lastLatLng)
            trackingMap?.addPolyline(polylineOptions)
        }
    }

    //Reconnect all the points of the Polyline when the device is rotated
    private fun connectAllPointsOfPolyline() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            trackingMap?.addPolyline(polylineOptions)
        }
    }

    //Move the camera to user's current position
    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            trackingMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(), MAP_CAMERA_ZOOM
                )
            )
        }
    }

    //Observe the data and react to changes
    private fun updateTracking(isTracking: Boolean) {
        //Update the UI
        this.isTracking = isTracking
        if (!isTracking) {
            btnStartRun.text = getString(R.string.start_run_btn)
            btnFinishRun.visibility = View.VISIBLE
        } else {
            btnStartRun.text = getString(R.string.stop_run_btn)
            btnFinishRun.visibility = View.GONE
        }
    }

    //Toggle the run btn
    private fun toggleRun() {
        if (isTracking) {
            //Pause the service
            sendCommandToService(TRACKING_SERVICE_PAUSE)
        } else {
            //Resume the Service of start running
            sendCommandToService(TRACKING_SERVICE_START_OR_RESUME)
        }
    }

    //Subscribe to the observer to subscribe to the live data object in serivce
    private fun subscribeObserver() {
        TrackingServices.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it)//New tracking state
        }

        //Observe the path
        TrackingServices.pathPoints.observe(viewLifecycleOwner) {
            pathPoints = it
            addLatestPolyline()//Connect to latest points
            moveCameraToUser()
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingServices::class.java).also {
            //Action of the service
            it.action = action
            //start the service
            requireContext().startService(it)
        }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()//Saves some resources when the memory gets low
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //Cache the map => prevent reloading every time we open the app
        mapView?.onSaveInstanceState(outState)
    }
}