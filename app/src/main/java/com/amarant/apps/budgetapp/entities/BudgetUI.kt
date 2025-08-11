package com.amarant.apps.budgetapp.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BudgetUI(
    val budget: Budget,
    var isHidden: Boolean = false
) : Parcelable
