package com.amarant.apps.budgetapp.entities

data class CategoryExpense(
    val category: Category,
    val entries: Int,
    val amount: Float,
    val percent: Double,
    val isHidden: Boolean = false
)
