package com.amarant.apps.budgetapp.util

import java.text.NumberFormat
import java.util.Locale

object NumberUtils {

    fun formatNumberWithThousandsSeparator(number: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        return formatter.format(number)
    }
}