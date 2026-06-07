package com.amarant.apps.budgetapp.entities

data class BarChartItem(
    val label: String,
    val amount: Float,
    val entries: Int,
    val startDate: Long,
    val endDate: Long
)
