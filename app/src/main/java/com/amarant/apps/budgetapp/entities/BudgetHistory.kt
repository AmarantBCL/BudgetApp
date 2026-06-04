package com.amarant.apps.budgetapp.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_history")
data class BudgetHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: Category,
    val amountLimit: Double,
    val spentAmount: Double,
    val periodType: String, // "Monthly" or "Weekly"
    val periodName: String, // e.g., "May 2024" or "Week 22, 2024"
    val dateStamp: Long
)
