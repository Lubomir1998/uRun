package com.example.urun.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.urun.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelRunDialog: DialogFragment() {

    // create this lambda and lambda fun so we can access the stopRun() fun from RunFragment()
    private var yesListener: (() -> Unit)? = null

    fun setYesListener(listener: (() -> Unit)){
        yesListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cancel run")
                .setMessage("Are you sure you want to delete the current run and all its data?")
                .setIcon(R.drawable.delete_img)
                .setPositiveButton("Yes"){_, _ ->
                    yesListener?.let {
                        it()
                    }
                }
                .setNegativeButton("No"){dialogInterface, _ ->
                    dialogInterface.cancel()
                }
                .create()
    }
}