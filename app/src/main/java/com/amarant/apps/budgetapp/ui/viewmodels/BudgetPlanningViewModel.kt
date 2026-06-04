package com.amarant.apps.budgetapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.amarant.apps.budgetapp.entities.BudgetSummary
import com.amarant.apps.budgetapp.entities.BudgetWithProgress
import com.amarant.apps.budgetapp.entities.CategoryBudget
import com.amarant.apps.budgetapp.entities.BudgetHistory
import com.amarant.apps.budgetapp.repository.BudgetPlanningRepository
import com.amarant.apps.budgetapp.util.UtilityFunctions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max

@HiltViewModel
class BudgetPlanningViewModel @Inject constructor(
    private val repository: BudgetPlanningRepository
) : ViewModel() {

    private val budgets = repository.getAllBudgets()
    
    // Check and Archive logic on initialization
    init {
        checkAndArchiveBudgets()
    }

    private fun checkAndArchiveBudgets() = viewModelScope.launch {
        // We'll observe the budgets once to check their dates
        // In a real scenario, this might be triggered by a Worker or on App Launch
        // For simplicity, we'll do it here
    }

    suspend fun archiveAndReset(budgetWithProgress: BudgetWithProgress) {
        val budget = budgetWithProgress.budget
        
        // 1. Save to History
        val periodName = if (budget.period == "Monthly") {
            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(budget.startDate))
        } else {
            "Week " + SimpleDateFormat("ww, yyyy", Locale.getDefault()).format(Date(budget.startDate))
        }

        val history = BudgetHistory(
            category = budget.category,
            amountLimit = budget.amountLimit,
            spentAmount = budgetWithProgress.spent,
            periodType = budget.period,
            periodName = periodName,
            dateStamp = budget.startDate
        )
        repository.archiveBudget(history)

        // 2. Reset or Delete
        if (budget.isRecursive) {
            val newStartDate = if (budget.period == "Monthly") {
                UtilityFunctions.getStartOfMonth()
            } else {
                UtilityFunctions.getStartOfWeek()
            }
            repository.insertBudget(budget.copy(startDate = newStartDate))
        } else {
            repository.deleteBudget(budget)
        }
    }
    
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
            
            val now = System.currentTimeMillis()
            val startOfMonth = UtilityFunctions.getStartOfMonth()
            val startOfWeek = UtilityFunctions.getStartOfWeek()

            val processedList = budgetList.map { budget ->
                // Check if period has ended
                val periodEnded = if (budget.period == "Monthly") {
                    budget.startDate < startOfMonth
                } else {
                    budget.startDate < startOfWeek
                }

                val spent = if (budget.period == "Monthly") {
                    monthly.find { it.category == budget.category.dbName }?.amount?.toDouble() ?: 0.0
                } else {
                    weekly.find { it.category == budget.category.dbName }?.amount?.toDouble() ?: 0.0
                }
                val absoluteSpent = abs(spent)
                
                val item = BudgetWithProgress(
                    budget = budget,
                    spent = absoluteSpent,
                    remaining = max(0.0, budget.amountLimit - absoluteSpent),
                    progress = (if (budget.amountLimit > 0) (absoluteSpent / budget.amountLimit * 100) else 0.0).toFloat().coerceIn(0f, 100f)
                )

                // If period ended, we trigger archive in background
                if (periodEnded) {
                    viewModelScope.launch { archiveAndReset(item) }
                }

                item
            }
            value = processedList
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

    val budgetHistory = repository.getBudgetHistory()

    fun insertBudget(budget: CategoryBudget) = viewModelScope.launch {
        repository.insertBudget(budget)
    }

    fun deleteBudget(budget: CategoryBudget) = viewModelScope.launch {
        repository.deleteBudget(budget)
    }

    fun deleteHistoryBudget(history: BudgetHistory) = viewModelScope.launch {
        repository.deleteHistory(history)
    }

    fun insertHistoryBudget(history: BudgetHistory) = viewModelScope.launch {
        repository.archiveBudget(history)
    }
}
