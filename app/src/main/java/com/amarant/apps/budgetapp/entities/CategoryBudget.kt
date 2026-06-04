package com.amarant.apps.budgetapp.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "category_budgets")
@Parcelize
data class CategoryBudget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: Category,
    val amountLimit: Double,
    val period: String, // "Monthly" or "Weekly"
    val startDate: Long,
    val isRecursive: Boolean = true
): Parcelable
