package com.example.runningtrackingapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.runningtrackingapp.R
import com.example.runningtrackingapp.db.RunDAO
import com.example.runningtrackingapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigateToTrackingFragmentIfNeeded(intent)

        setSupportActionBar(toolbar)
        //Set up bottom Navigation
        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
        bottomNavigationView.setOnItemReselectedListener {
            //Prevent reloading the Fragment if we click the fragment the second time

        }

        //Add Destination change list the fragments
        navHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.settingsFragment, R.id.runFragment, R.id.statsFragment -> {
                        bottomNavigationView.visibility = View.VISIBLE
                        //Show up the navigation when those fragments chosen
                    }
                    else -> {
                        bottomNavigationView.visibility = View.GONE
                    }
                }
            }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    //Navigate to the Tracking Fragment if neededt
    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?)
    {
        if(intent?.action == ACTION_SHOW_TRACKING_FRAGMENT)
        {
            //This was launched by the notification click
            navHostFragment.findNavController().navigate(R.id.action_global_trackingFragment)//When the Main Activity was destroyed, but the service still running

        }
    }
}