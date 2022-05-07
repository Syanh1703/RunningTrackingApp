package com.example.runningtrackingapp.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.runningtrackingapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelTrackingDialog:DialogFragment() {

    //Create a Listener from the tracking Fragment
    private var yesCancelListener:  (() -> Unit)? = null
    fun setYesCancelListener(listener:() -> Unit)
    {
        yesCancelListener = listener
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle(getString(R.string.cancel_tracking))
            .setMessage(getString(R.string.confirm_cancel))
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton(R.string.yes){
                    _, _, -> yesCancelListener?.let {
                        yes -> yes()
            }
            }
            .setNegativeButton(getString(R.string.no))
            {
                    dialogInterface, _ -> dialogInterface.cancel()//Ignore the message and do not cancel the run
            }
            .create()

    }
}