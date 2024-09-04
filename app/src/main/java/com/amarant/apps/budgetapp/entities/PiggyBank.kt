package com.amarant.apps.budgetapp.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "piggy_bank")
data class PiggyBank(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var currencySaved: Int,
    var hryvniaSaved: Int,
    var currencyTaken: Int,
    var hryvniaTaken: Int
)
