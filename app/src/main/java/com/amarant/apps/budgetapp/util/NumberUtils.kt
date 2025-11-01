package com.amarant.apps.budgetapp.util

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
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

    fun formatCurrency(amount: Double, currencySymbol: String): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val currency = Currency.getInstance(currencySymbol)
        formatter.currency = currency
        return formatter.format(amount)
    }
}