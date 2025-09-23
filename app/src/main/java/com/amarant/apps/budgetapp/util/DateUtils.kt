package com.amarant.apps.budgetapp.util

import java.text.SimpleDateFormat
import java.util.Calendar
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

    fun toLocalStartOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}