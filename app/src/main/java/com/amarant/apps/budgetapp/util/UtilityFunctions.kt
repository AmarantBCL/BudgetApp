package com.amarant.apps.budgetapp.util

import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_MONTH
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_SIX_MONTHS
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_THREE_MONTHS
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_TWO_DAYS
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_TWO_MONTHS
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_TWO_WEEKS
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_TWO_YEARS
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_WEEK
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_THIS_MONTH
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_THIS_WEEK
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_THIS_YEAR
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_TODAY
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_YESTERDAY
import java.text.SimpleDateFormat
import java.util.Calendar

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

    fun calculateStartPeriod(period: Int): Long {
        val start = when (period) {
            PERIOD_TODAY -> getToday()
            PERIOD_YESTERDAY -> getYesterday()
            PERIOD_LAST_TWO_DAYS -> getYesterday()
            PERIOD_THIS_WEEK -> getStartOfWeek()
            PERIOD_LAST_WEEK -> getStartOfPreviousWeek()
            PERIOD_LAST_TWO_WEEKS -> getStartOfPreviousWeek()
            PERIOD_THIS_MONTH -> getStartOfMonth()
            PERIOD_LAST_MONTH -> getStartOfLastMonth()
            PERIOD_LAST_TWO_MONTHS -> getStartOfLastMonth()
            PERIOD_LAST_THREE_MONTHS -> getStartOfLastThreeMonths()
            PERIOD_LAST_SIX_MONTHS -> getStartOfLastSixMonths()
            PERIOD_THIS_YEAR -> getStartOfYear()
            PERIOD_LAST_TWO_YEARS -> getStartOfLastTwoYears()
            else -> {
                0L
            }
        }
        return start
    }

    fun calculateEndPeriod(period: Int): Long {
        val end = when (period) {
            PERIOD_YESTERDAY -> getToday() - 1000
            PERIOD_LAST_WEEK -> getStartOfWeek() - 1000
            PERIOD_LAST_MONTH -> getStartOfMonth() - 1000
            else -> {
                // Return end of today (23:59:59)
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                calendar.timeInMillis
            }
        }
        return end
    }

    fun getEndDate(daysToCount: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysToCount)
        return dateMillisToString(cal.timeInMillis)
    }

    fun getToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getYesterday(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
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
        calendar.add(Calendar.DAY_OF_MONTH, -7)
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

    fun getStartOfLastMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getStartOfLastThreeMonths(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -2)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getStartOfLastSixMonths(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -5)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getStartOfYear(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, Calendar.JANUARY)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getStartOfLastTwoYears(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -1)
        calendar.set(Calendar.MONTH, Calendar.JANUARY)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}