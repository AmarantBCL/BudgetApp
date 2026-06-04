package com.amarant.apps.budgetapp.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class Profile(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    val name: String,
    val email: String,
    val profileImageFilePath: String,
    val bankName: String,
    val currentBalance: Float,
    val initialBalance: Float,
    val primaryBank: Boolean,
    val currency: String = "$ USD",
    val monthlyIncome: Double = 0.0,
    val monthlyGoal: Double = 0.0,
    val hideDecimal: Boolean = false
)
