package com.amarant.apps.budgetapp.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "savings")
data class Saving(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title: String,
    val target: Float,
    val saved: Float,
    val currency: String,
    val dueTo: Long,
    val circleColor: Int
) : Parcelable
