package com.amarant.apps.budgetapp.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    private const val CALENDAR_DATE_PATTERN = "dd MMMM, yyyy"

    fun getFormattedDate(date: Long): String {
        val date = Date(date)
        val formatter = SimpleDateFormat(CALENDAR_DATE_PATTERN, Locale.getDefault())
        return formatter.format(date)
    }
}