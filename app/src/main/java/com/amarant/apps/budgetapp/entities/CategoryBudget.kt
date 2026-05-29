package com.amarant.apps.budgetapp.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_budgets")
data class CategoryBudget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: Category,
    val amountLimit: Double,
    val period: String, // "Monthly" or "Weekly"
    val startDate: Long
)
