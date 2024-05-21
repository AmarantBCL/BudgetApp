package com.amarant.apps.budgetapp.repository

import com.amarant.apps.budgetapp.db.BudgetDao
import com.amarant.apps.budgetapp.entities.Budget
import javax.inject.Inject

class BudgetRepository @Inject constructor(
    val budgetDao: BudgetDao
) {

    suspend fun insertBudget(budget: Budget) = budgetDao.insertBudget(budget)

    fun getAllBudgetEntries() = budgetDao.getAllData()
}