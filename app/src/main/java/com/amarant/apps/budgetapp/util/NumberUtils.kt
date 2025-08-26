package com.amarant.apps.budgetapp.util

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object NumberUtils {

    fun formatNumberWithThousandsSeparator(number: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        return formatter.format(number)
    }

    fun formatDecimal(value: Double): String {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.HALF_UP
        return df.format(value)
    }
}