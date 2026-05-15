package com.amarant.apps.budgetapp.util

object Constants {

    const val DATABASE_NAME = "budget_db"
    const val PREFERENCE_NAME = "profile_preference"
    const val PREFERENCE_PROFILE_EXISTENCE_KEY = "PREFERENCE_PROFILE_EXISTENCE_KEY"
    const val PREFERENCE_IS_PIN_ENTERED_KEY = "IS_PIN_ENTERED"
    const val PREFERENCE_PIN_VALUE_KEY = "PREFERENCE_PIN_VALUE_KEY"
    const val PREFERENCE_LAST_ACTIVE_TIME_KEY = "PREFERENCE_LAST_ACTIVE_TIME_KEY"
    
    const val PIN_LOCK_TIMEOUT_MILLIS = 10 * 60 * 1000L // 10 minutes

    const val DEBIT = "Debit"
    const val CREDIT = "Credit"

    const val SNACKBAR_PIN_DURATION = 1000
}