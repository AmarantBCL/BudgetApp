package com.amarant.apps.budgetapp.repository

import com.amarant.apps.budgetapp.db.BudgetDao
import com.amarant.apps.budgetapp.entities.Budget
import javax.inject.Inject

class BudgetRepository @Inject constructor(
    val budgetDao: BudgetDao
) {

    suspend fun insertBudget(budget: Budget) = budgetDao.insertBudget(budget)

    fun getAllBudgetEntries() = budgetDao.getAllData()

    suspend fun updateBudget(amount: Float, purpose: String, id: Int) =
        budgetDao.updateBudget(amount, purpose, id)

    suspend fun deleteEntry(budget: Budget) = budgetDao.deleteEntry(budget)

    fun getTotalCredit() = budgetDao.getTotalCredit()

    fun getTotalSpending() = budgetDao.getTotalSpending()

    suspend fun getBudgetEntriesBetweenDates(startDate: Long, endDate: Long) =
        budgetDao.getReportsBetweenDates(startDate, endDate)
}