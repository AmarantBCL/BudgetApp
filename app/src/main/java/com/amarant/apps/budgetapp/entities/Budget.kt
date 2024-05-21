package com.amarant.apps.budgetapp.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    val date: String,
    val bankName: String,
    val amount: Float,
    val purpose: String,
    val creditOrDebit: String
)
