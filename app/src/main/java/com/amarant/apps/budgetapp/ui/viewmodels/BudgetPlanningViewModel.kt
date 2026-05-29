package com.amarant.apps.budgetapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.amarant.apps.budgetapp.entities.BudgetSummary
import com.amarant.apps.budgetapp.entities.BudgetWithProgress
import com.amarant.apps.budgetapp.entities.CategoryBudget
import com.amarant.apps.budgetapp.repository.BudgetPlanningRepository
import com.amarant.apps.budgetapp.util.UtilityFunctions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max

@HiltViewModel
class BudgetPlanningViewModel @Inject constructor(
    private val repository: BudgetPlanningRepository
) : ViewModel() {

    private val budgets = repository.getAllBudgets()
    
    // We need expenses for both current month and current week
    private val startOfMonth = UtilityFunctions.getStartOfMonth()
    private val startOfWeek = UtilityFunctions.getStartOfWeek()
    private val now = System.currentTimeMillis()

    private val monthlyExpenses = repository.getSpendingsByCategory(startOfMonth, now)
    private val weeklyExpenses = repository.getSpendingsByCategory(startOfWeek, now)

    val budgetListWithProgress: LiveData<List<BudgetWithProgress>> = MediatorLiveData<List<BudgetWithProgress>>().apply {
        fun update() {
            val budgetList = budgets.value ?: return
            val monthly = monthlyExpenses.value ?: emptyList()
            val weekly = weeklyExpenses.value ?: emptyList()

            value = budgetList.map { budget ->
                val spent = if (budget.period == "Monthly") {
                    monthly.find { it.category == budget.category.dbName }?.amount?.toDouble() ?: 0.0
                } else {
                    weekly.find { it.category == budget.category.dbName }?.amount?.toDouble() ?: 0.0
                }
                // Convert spent to positive as it's saved as negative in DB for Debit
                val absoluteSpent = abs(spent)
                
                BudgetWithProgress(
                    budget = budget,
                    spent = absoluteSpent,
                    remaining = max(0.0, budget.amountLimit - absoluteSpent),
                    progress = (if (budget.amountLimit > 0) (absoluteSpent / budget.amountLimit * 100) else 0.0).toFloat().coerceIn(0f, 100f)
                )
            }
        }
        addSource(budgets) { update() }
        addSource(monthlyExpenses) { update() }
        addSource(weeklyExpenses) { update() }
    }

    val budgetSummary: LiveData<BudgetSummary> = budgetListWithProgress.map { list ->
        val totalBudgeted = list.sumOf { it.budget.amountLimit }
        val totalSpent = list.sumOf { it.spent }
        val overallProgress = if (totalBudgeted > 0) {
            (totalSpent / totalBudgeted * 100).toFloat().coerceIn(0f, 100f)
        } else 0f
        
        BudgetSummary(totalBudgeted, totalSpent, overallProgress)
    }

    fun insertBudget(budget: CategoryBudget) = viewModelScope.launch {
        repository.insertBudget(budget)
    }

    fun deleteBudget(budget: CategoryBudget) = viewModelScope.launch {
        repository.deleteBudget(budget)
    }
}
