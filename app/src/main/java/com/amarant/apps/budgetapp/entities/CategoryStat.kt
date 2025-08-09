package com.amarant.apps.budgetapp.entities

import androidx.room.ColumnInfo

data class CategoryStat(
    val category: String,
    @ColumnInfo(name = "entries_count")
    val count: Int
)
