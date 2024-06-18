package com.amarant.apps.budgetapp.util

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

object UtilityFunctions {

    fun dateStringToMillis(dateInString: String): Long {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        val date = dateFormat.parse(dateInString)
        return date.time
    }

    fun dateMillisToString(dateInMillis: Long): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateInMillis
        return dateFormat.format(cal.time)
    }

    fun getEndDate(daysToCount: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysToCount)
        return dateMillisToString(cal.timeInMillis)
    }

    fun getStartOfWeek(): Long {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val diff = (currentDayOfWeek - calendar.firstDayOfWeek + 7) % 7
        if (diff != 0) {
            calendar.add(Calendar.DAY_OF_MONTH, -diff)
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getStartOfPreviousWeek(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getStartOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}