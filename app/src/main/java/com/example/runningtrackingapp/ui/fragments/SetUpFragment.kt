package com.example.runningtrackingapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.runningtrackingapp.R
import com.example.runningtrackingapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runningtrackingapp.other.Constants.KEY_USER_NAME
import com.example.runningtrackingapp.other.Constants.KEY_USER_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject

@AndroidEntryPoint
class SetUpFragment :Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharePref :SharedPreferences

    @set:Inject
    var isFirstAppOpen = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(!isFirstAppOpen)//user has entered the values
        {
            //Remove the SetUp Fragment from the background
            val navOption = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()
            findNavController().navigate(R.id.action_setupFragment_to_runFragments,
            savedInstanceState,
            navOption)
        }

        tvContinue.setOnClickListener {
            val success = writeDataSharedPref()
            if(success)
            {
                findNavController().navigate(R.id.action_setupFragment_to_runFragments)//Perform the navigation
            }
            else
            {
                Snackbar.make(requireView(), getString(R.string.fill_all), Snackbar.LENGTH_SHORT).show()
            }

        }
    }

    private fun writeDataSharedPref():Boolean
    {
        //Avoid saving empty string
        val userName = etName.text.toString()
        val userWeight = etWeight.text.toString()
        if(userName.isEmpty() || userWeight.isEmpty())
        {
            return false
        }

        //Save to the shared preference
        sharePref.edit()
            .putString(KEY_USER_NAME, userName)
            .putFloat(KEY_USER_WEIGHT, userWeight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()
        return true
    }
}