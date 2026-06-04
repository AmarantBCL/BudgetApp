package com.amarant.apps.budgetapp.util

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object NumberUtils {

    var isHideDecimal: Boolean = false

    fun formatNumberWithThousandsSeparator(number: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        if (isHideDecimal) {
            formatter.maximumFractionDigits = 0
        } else {
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2
        }
        return formatter.format(number)
    }

    fun formatDecimal(value: Double): String {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.DOWN
        return df.format(value)
    }
}