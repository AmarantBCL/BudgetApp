package com.amarant.apps.budgetapp.util

import android.view.View
import com.google.android.material.snackbar.Snackbar

object MessageUtils {

    fun showSnackbarMessage(view: View, text: String, actionText: String) {
        val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
        if (actionText.isNotEmpty()) {
            snackbar.setAction(actionText) {
                snackbar.dismiss()
            }
        }
        snackbar.show()
    }
}