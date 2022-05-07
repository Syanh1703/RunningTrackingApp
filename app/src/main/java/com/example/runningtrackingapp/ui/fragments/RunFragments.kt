package com.example.runningtrackingapp.ui.fragments

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runningtrackingapp.R
import com.example.runningtrackingapp.adapters.RunAdapter
import com.example.runningtrackingapp.di.SortType
import com.example.runningtrackingapp.other.Constants.REQUEST_LOCATION_CODE
import com.example.runningtrackingapp.other.TrackingUtility
import com.example.runningtrackingapp.ui.viewmodels.MainViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_run.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class RunFragments : Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {

    private val runViewModel: MainViewModels by viewModels()

    private lateinit var runAdapter: RunAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestLocationPermission()
        setUpRecyclerView()

        //Set the Spinner
        when(runViewModel.sortType)
        {
            SortType.DATE -> spFilter.setSelection(0)//Index base of the string array
            SortType.RUNNING_TIME -> spFilter.setSelection(1)
            SortType.DISTANCE -> spFilter.setSelection(2)
            SortType.AVG_SPEED -> spFilter.setSelection(3)
            SortType.CALORIES_BURNT -> spFilter.setSelection(4)
        }

        spFilter.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position)
                {
                    0 -> runViewModel.sortRuns(SortType.DATE)
                    1 -> runViewModel.sortRuns(SortType.RUNNING_TIME)
                    2 -> runViewModel.sortRuns(SortType.DISTANCE)
                    3 -> runViewModel.sortRuns(SortType.AVG_SPEED)
                    4 -> runViewModel.sortRuns(SortType.CALORIES_BURNT)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

        }

        //Observe changes in the database
        runViewModel.runMediatorLiveData.observe(viewLifecycleOwner) {
            runAdapter.submitList(it)
        }

        fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragments_to_trackingFragment)
        }
    }

    private fun requestLocationPermission() {
        //Check if user has accepted the permission
        if (TrackingUtility.hasLocationPermission(requireContext())) {
            return
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                EasyPermissions.requestPermissions(
                    this,
                    "This app needs your permission to work properly",
                    REQUEST_LOCATION_CODE,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            } else {
                EasyPermissions.requestPermissions(
                    this,
                    "This app needs your permission to work properly",
                    REQUEST_LOCATION_CODE,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
        }
    }

    //Setup the recycler view
    private fun setUpRecyclerView() = rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        //If user denies the permission
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestLocationPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}