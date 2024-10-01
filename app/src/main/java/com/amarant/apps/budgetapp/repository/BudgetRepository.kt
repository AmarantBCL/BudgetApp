package com.amarant.apps.budgetapp.repository

import com.amarant.apps.budgetapp.db.BudgetDao
import com.amarant.apps.budgetapp.entities.Budget
import javax.inject.Inject

class BudgetRepository @Inject constructor(
    val budgetDao: BudgetDao
) {

    suspend fun insertBudget(budget: Budget) = budgetDao.insertBudget(budget)

    fun getAllBudgetEntries() = budgetDao.getAllData()

    suspend fun updateBudget(amount: Float, purpose: String, category: String, id: Int) =
        budgetDao.updateBudget(amount, purpose, category, id)

    suspend fun deleteEntry(budget: Budget) = budgetDao.deleteEntry(budget)

    fun getTotalSpendingForPeriod(startDate: Long, endDate: Long) =
        budgetDao.getTotalSpendingForPeriod(startDate, endDate)

    fun getTotalCreditForPeriod(startDate: Long, endDate: Long) =
        budgetDao.getTotalCreditForPeriod(startDate, endDate)

    fun getBudgetEntriesBetweenDates(startDate: Long, endDate: Long, filter: String) =
        budgetDao.getReportsBetweenDates(startDate, endDate, filter)

    fun getSpendingsByCategory(startDate: Long, endDate: Long) =
        budgetDao.getSpendingsByCategory(startDate, endDate)
}