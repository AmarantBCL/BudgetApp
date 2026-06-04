package com.amarant.apps.budgetapp.repository

import androidx.lifecycle.LiveData
import com.amarant.apps.budgetapp.db.BudgetDao
import com.amarant.apps.budgetapp.db.CategoryBudgetDao
import com.amarant.apps.budgetapp.db.BudgetHistoryDao
import com.amarant.apps.budgetapp.entities.CategoryBudget
import com.amarant.apps.budgetapp.entities.BudgetHistory
import com.amarant.apps.budgetapp.entities.BudgetCategoryDetails
import javax.inject.Inject

class BudgetPlanningRepository @Inject constructor(
    private val categoryBudgetDao: CategoryBudgetDao,
    private val budgetHistoryDao: BudgetHistoryDao,
    private val expenseDao: BudgetDao
) {
    suspend fun insertBudget(budget: CategoryBudget) = categoryBudgetDao.insertBudget(budget)

    fun getAllBudgets(): LiveData<List<CategoryBudget>> = categoryBudgetDao.getAllBudgets()

    suspend fun deleteBudget(budget: CategoryBudget) = categoryBudgetDao.deleteBudget(budget)

    fun getSpendingsByCategory(startDate: Long, endDate: Long): LiveData<List<BudgetCategoryDetails>> =
        expenseDao.getSpendingsByCategory(startDate, endDate)

    suspend fun archiveBudget(history: BudgetHistory) = budgetHistoryDao.insertHistory(history)

    fun getBudgetHistory(): LiveData<List<BudgetHistory>> = budgetHistoryDao.getAllHistory()
}
