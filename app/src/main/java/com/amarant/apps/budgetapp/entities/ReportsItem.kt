package com.amarant.apps.budgetapp.entities

sealed class ReportsItem {
    data class DateHeader(val date: String) : ReportsItem()
    data class Entry(val entry: BudgetUI) : ReportsItem()
}
