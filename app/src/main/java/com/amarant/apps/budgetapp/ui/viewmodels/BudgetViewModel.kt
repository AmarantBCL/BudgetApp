package com.amarant.apps.budgetapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.entities.BudgetCategoryDetails
import com.amarant.apps.budgetapp.entities.BudgetUI
import com.amarant.apps.budgetapp.entities.CategoryStat
import com.amarant.apps.budgetapp.entities.ReportsItem
import com.amarant.apps.budgetapp.repository.BudgetRepository
import com.amarant.apps.budgetapp.util.Constants
import com.amarant.apps.budgetapp.util.DateUtils
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_MONTH
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_TWO_DAYS
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_TWO_MONTHS
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_TWO_WEEKS
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_WEEK
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_THIS_MONTH
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_THIS_WEEK
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_TODAY
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_YESTERDAY
import com.amarant.apps.budgetapp.util.UtilityFunctions
import com.amarant.apps.budgetapp.util.UtilityFunctions.dateMillisToString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _dateRange = MutableLiveData(Pair(0L, System.currentTimeMillis()))
    private val dateRange: LiveData<Pair<Long, Long>>
        get() = _dateRange

    private val _appliedFilter = MutableLiveData("")
    val appliedFilter: LiveData<String>
        get() = _appliedFilter

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String>
        get() = _searchQuery

    private val selectedIds = MutableLiveData<Set<Int>>(emptySet())

    fun insertBudget(budget: Budget) = viewModelScope.launch {
        budgetRepository.insertBudget(budget)
    }

    fun updateBudget(creditOrDebit: String, amount: Float, purpose: String, category: String, id: Int) =
        viewModelScope.launch {
            budgetRepository.updateBudget(creditOrDebit, amount, purpose, category, id)
        }

    fun deleteEntry(budget: Budget) = viewModelScope.launch {
        budgetRepository.deleteEntry(budget)
    }

    fun setReportsBetweenDates(startDate: Long, endDate: Long) {
        _dateRange.value = Pair(startDate, endDate)
    }

//    fun getReportsBetweenDates(): LiveData<List<Budget>> {
//        return dateRange.switchMap { pair ->
//            appliedFilter.switchMap { filter ->
//                budgetRepository.getBudgetEntriesBetweenDates(pair.first, pair.second, filter)
//            }
//        }
//    }

    fun toggleSelection(id: Int) {
        val current = selectedIds.value ?: emptySet()
        selectedIds.value = if (current.contains(id)) {
            current - id
        } else {
            current + id
        }
    }

    fun getBudgetUIEntriesBetweenDates(): LiveData<List<BudgetUI>> {
        val result = MediatorLiveData<List<BudgetUI>>()
        val dbSource = dateRange.switchMap { dateRange ->
            appliedFilter.switchMap { filter ->
                searchQuery.switchMap { query ->
                    budgetRepository.getBudgetEntriesBetweenDates(dateRange.first, dateRange.second, filter, query)
                }
            }
        }
        fun update(budgets: List<Budget>?, selected: Set<Int>?) {
            if (budgets != null && selected != null) {
                result.value = budgets.map { budget ->
                    BudgetUI(budget, isHidden = budget.id in selected)
                }
            }
        }
        result.addSource(dbSource) { budgets -> update(budgets, selectedIds.value) }
        result.addSource(selectedIds) { selected -> update(dbSource.value, selected) }
        return result
    }

    fun calculateTotalSpending(period: Int): LiveData<Float> {
        val start = calculateStartPeriod(period)
        val end = calculateEndPeriod(period)
        return budgetRepository.getTotalSpendingForPeriod(start, end)
    }

    fun calculateTotalCredit(period: Int): LiveData<Float> {
        val start = calculateStartPeriod(period)
        val end = calculateEndPeriod(period)
        return budgetRepository.getTotalCreditForPeriod(start, end)
    }

    fun getSpendingsByCategory(period: Int): LiveData<List<BudgetCategoryDetails>> {
        val start = calculateStartPeriod(period)
        val end = calculateEndPeriod(period)
        return budgetRepository.getSpendingsByCategory(start, end)
    }

    fun applyFilter(filter: String) {
        _appliedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getCategoryStats(): LiveData<List<CategoryStat>> {
        return budgetRepository.getCategoryStats()
    }

    fun validateAndAddEntries(isDebit: Boolean, amount: String, purpose: String, date: Long, categoryName: String): Boolean {
        val bankName = ""
        val debitOrCredit = if (isDebit) Constants.DEBIT else Constants.CREDIT
        val amountAsInt = amount.toIntOrNull()
        return if (amountAsInt == null || purpose.isEmpty() || categoryName.isEmpty()) {
            false
        } else {
            var amountToInsert = amountAsInt.toFloat()
            if (debitOrCredit == Constants.DEBIT) {
                amountToInsert *= -1
            }
            insertBudget(Budget(
                date = date.toString(),
                bankName = bankName,
                amount = amountToInsert,
                purpose = purpose,
                creditOrDebit = debitOrCredit,
                category = categoryName
            ))
            true
        }
    }

    fun validateAndEditEntries(id: Int, isDebit: Boolean, amount: String, purpose: String, categoryName: String): Boolean {
        val debitOrCredit = if (isDebit) Constants.DEBIT else Constants.CREDIT
        val amountAsInt = amount.toIntOrNull()
        return if (amountAsInt == null || purpose.isEmpty() || categoryName.isEmpty()) {
            false
        } else {
            var amountToInsert = amountAsInt.toFloat()
            if (debitOrCredit == Constants.DEBIT) {
                amountToInsert *= -1
            }
            updateBudget(debitOrCredit, amountToInsert, purpose, categoryName, id)
            true
        }
    }

    private fun calculateStartPeriod(period: Int): Long {
        val start = when (period) {
            PERIOD_TODAY -> UtilityFunctions.getToday()
            PERIOD_YESTERDAY -> UtilityFunctions.getYesterday()
            PERIOD_LAST_TWO_DAYS -> UtilityFunctions.getYesterday()
            PERIOD_THIS_WEEK -> UtilityFunctions.getStartOfWeek()
            PERIOD_LAST_WEEK -> UtilityFunctions.getStartOfPreviousWeek()
            PERIOD_LAST_TWO_WEEKS -> UtilityFunctions.getStartOfPreviousWeek()
            PERIOD_THIS_MONTH -> UtilityFunctions.getStartOfMonth()
            PERIOD_LAST_MONTH -> UtilityFunctions.getStartOfLastMonth()
            PERIOD_LAST_TWO_MONTHS -> UtilityFunctions.getStartOfLastMonth()
            else -> {
                0L
            }
        }
        return start
    }

    private fun calculateEndPeriod(period: Int): Long {
        val end = when (period) {
            PERIOD_YESTERDAY -> UtilityFunctions.getToday() - 1000
            PERIOD_LAST_WEEK -> UtilityFunctions.getStartOfWeek() - 1000
            PERIOD_LAST_MONTH -> UtilityFunctions.getStartOfMonth() - 1000
            else -> {
                val dateInMillies = Calendar.getInstance().timeInMillis
                val startDate = dateMillisToString(dateInMillies)
                UtilityFunctions.dateStringToMillis(startDate)
            }
        }
        return end
    }
}