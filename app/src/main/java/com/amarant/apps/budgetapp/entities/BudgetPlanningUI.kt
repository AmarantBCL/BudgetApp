package com.amarant.apps.budgetapp.entities

data class BudgetWithProgress(
    val budget: CategoryBudget,
    val spent: Double,
    val remaining: Double,
    val progress: Float
)

data class BudgetSummary(
    val totalBudgeted: Double,
    val totalSpent: Double,
    val overallProgress: Float
)
