package com.amarant.apps.budgetapp.entities

data class TempProfile(
    val fullName: String,
    val email: String,
    val currency: String,
    val monthlyIncome: String?,
    val savingGoal: String?,
    val categories: List<QuickCategoryItem>
//    val categories: List<CategoryItem>
)
