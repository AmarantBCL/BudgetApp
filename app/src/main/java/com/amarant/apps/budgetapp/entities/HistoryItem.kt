package com.amarant.apps.budgetapp.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val entry: String,
    val category: Int
)
