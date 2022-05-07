package com.example.runningtrackingapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.runningtrackingapp.R
import com.example.runningtrackingapp.db.Run
import com.example.runningtrackingapp.other.Constants.CANCEL_TRACKING_DIALOG_TAG
import com.example.runningtrackingapp.other.Constants.MAP_CAMERA_ZOOM
import com.example.runningtrackingapp.other.Constants.POLYLINE_COLOR
import com.example.runningtrackingapp.other.Constants.POLYLINE_WIDTH
import com.example.runningtrackingapp.other.Constants.TRACKING_SERVICE_PAUSE
import com.example.runningtrackingapp.other.Constants.TRACKING_SERVICE_START_OR_RESUME
import com.example.runningtrackingapp.other.Constants.TRACKING_SERVICE_STOP
import com.example.runningtrackingapp.other.TrackingUtility
import com.example.runningtrackingapp.services.Polyline
import com.example.runningtrackingapp.services.TrackingServices
import com.example.runningtrackingapp.ui.viewmodels.MainViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModels by viewModels()

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    //Create a Google Map
    private var trackingMap: GoogleMap? = null

    private var currentTimeInMillis = 0L

    //Create a cancel menu
    private var cancelMenu : Menu? = null

    @set:Inject
    var userWeight :Float? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)//Visible only in the Tracking Fragment
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView.onCreate(savedInstanceState)

        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        //Prevent bugs appear when rotate the screen
        if(savedInstanceState!= null)
        {
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG_TAG) as CancelTrackingDialog?
            cancelTrackingDialog?.setYesCancelListener {
                stopRun()
            }
        }

        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endAndSaveRun()
        }
        mapView.getMapAsync {
            //Set the loaded Map
            trackingMap = it
            connectAllPointsOfPolyline()
        }
        subscribeObserver()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.cancelMenu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        //Change the visibility of the menu item
        if(currentTimeInMillis>0L)
        {
            //If the time starts counting, set the cancel btn to be visible
            this.cancelMenu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.miCancelTracking -> {
                confirmCancelAlertDialog()
                //Kill the service in the Tracking Service
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun stopRun()
    {
        tvTimer.text = "00:00:00"
        sendCommandToService(TRACKING_SERVICE_STOP)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragments)
    }

    private fun confirmCancelAlertDialog()
    {
        CancelTrackingDialog().apply {
            setYesCancelListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
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
                .add(secondLastLatLng, lastLatLng)
                .geodesic(true)
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
                .geodesic(true)
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

    /**
     * To save the data, we need to zoom the map to see the whole track
     */
    private fun zoomToSeeWholeTrack()
    {
        //LatLng bound Object by Google
        val bound = LatLngBounds.Builder()
        for(polyline in pathPoints)
        {
            for(pos in polyline)
            {
                bound.include(pos)
            }
        }

        trackingMap?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bound.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endAndSaveRun()
    {
        //Get all the necessary information
        //Make the screenshot
        trackingMap?.snapshot {bmp ->
            //Calculate the distance
            var distanceInMeters = 0f
            userWeight = 80f //Assume the user is 80kg
            for(polyline in pathPoints)
            {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline)
            }
            val runTimeInMillisToHour = (currentTimeInMillis/1000f/60/60)
            val avgRunSpeed = (round((distanceInMeters/1000f) / runTimeInMillisToHour)*10)/10f
            val timeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurnt = ((distanceInMeters/1000f)*userWeight!!).toInt()


            //Construct Run Object
            val runResult = Run(bmp, timeStamp, avgRunSpeed, distanceInMeters, currentTimeInMillis, caloriesBurnt)
            //Insert run and save to the database
            viewModel.insertRun(runResult)
            Snackbar.make(requireActivity().findViewById(R.id.rootView),
            getString(R.string.run_saved),
            Snackbar.LENGTH_LONG)
                .show()
            stopRun()
        }
    }

    //Observe the data and react to changes
    private fun updateTracking(isTracking: Boolean) {
        //Update the UI
        this.isTracking = isTracking
        if (!isTracking && currentTimeInMillis > 0L) {
            btnToggleRun.text = getString(R.string.start_run_btn)
            btnFinishRun.visibility = View.VISIBLE
        } else if(isTracking){
            btnToggleRun.text = getString(R.string.stop_run_btn)
            cancelMenu?.getItem(0)?.isVisible = true
            btnFinishRun.visibility = View.GONE
        }
    }

    //Toggle the run btn
    private fun toggleRun() {
        if (isTracking) {
            //Able to cancel the run when paused
                cancelMenu?.getItem(0)?.isVisible = true
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
        TrackingServices.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()//Connect to latest points
            moveCameraToUser()
        })

        //Observe the time
        TrackingServices.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeInMillis,true)
            tvTimer.text = formattedTime
        })
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