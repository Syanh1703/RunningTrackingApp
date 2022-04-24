package com.example.runningtrackingapp.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.runningtrackingapp.R
import kotlinx.android.synthetic.main.fragment_setup.*

class SetUpFragment :Fragment(R.layout.fragment_setup) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvContinue.setOnClickListener {
            findNavController().navigate(R.id.action_setUpFragment_to_runFragments)//Perform the navigation
        }
    }
}