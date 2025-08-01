package com.amarant.apps.budgetapp.util

import android.util.Patterns

object EmailUtils {

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}