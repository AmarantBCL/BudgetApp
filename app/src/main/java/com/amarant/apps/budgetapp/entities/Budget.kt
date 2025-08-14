package com.amarant.apps.budgetapp.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "budget")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    val date: String,
    val bankName: String,
    val amount: Float,
    val purpose: String,
    val creditOrDebit: String,
    @ColumnInfo(defaultValue = "Unknown")
    val category: Category
) : Parcelable
