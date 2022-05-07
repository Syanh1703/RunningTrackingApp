package com.example.runningtrackingapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.runningtrackingapp.R
import com.example.runningtrackingapp.other.Constants.KEY_USER_NAME
import com.example.runningtrackingapp.other.Constants.KEY_USER_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment:Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFieldsFromSharedPref()
        btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()
            if(success)
            {
                Snackbar.make(view, getString(R.string.saved_change), Snackbar.LENGTH_SHORT).show()
            }
            else
            {
                Snackbar.make(view, getString(R.string.fill_all), Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    //Load the name from SharePref
    private fun loadFieldsFromSharedPref()
    {
        val editedUserName = sharedPreferences.getString(KEY_USER_NAME,"")?: ""
        val editedUserWeight = sharedPreferences.getFloat(KEY_USER_WEIGHT, 0f)

        etChangeName.setText(editedUserName)
        etChangeWeight.setText(editedUserWeight.toString())

    }
    private fun applyChangesToSharedPref() :Boolean
    {
        val changeName = etChangeName.text.toString()
        val changeWeight = etChangeWeight.text.toString()

        if(changeName.isEmpty() || changeWeight.isEmpty())
        {
            return false
        }
        sharedPreferences.edit()
            .putString(KEY_USER_NAME,changeName)
            .putFloat(KEY_USER_WEIGHT, changeWeight.toFloat())
            .apply()
        return true
    }
}