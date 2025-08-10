package com.amarant.apps.budgetapp.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    private const val CALENDAR_DATE_PATTERN = "d MMMM, yyyy"
    private const val PARSE_TIMESTAMP_FROM_DATE_PATTERN = "dd/MM/yyyy"

    fun getFormattedDate(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat(CALENDAR_DATE_PATTERN, Locale.getDefault())
        return formatter.format(date)
    }

    fun getTimestampFromDate(date: Date): Long {
        val formatter = SimpleDateFormat(PARSE_TIMESTAMP_FROM_DATE_PATTERN, Locale.getDefault())
        val formattedDate = formatter.format(date)
        return UtilityFunctions.dateStringToMillis(formattedDate)
    }
}