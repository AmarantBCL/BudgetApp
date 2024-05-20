package com.amarant.apps.budgetapp.repository

import com.amarant.apps.budgetapp.db.BudgetDao
import javax.inject.Inject

class BudgetRepository @Inject constructor(
    val budgetDao: BudgetDao
) {
}