package com.amarant.apps.budgetapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.entities.BudgetCategoryDetails
import com.amarant.apps.budgetapp.repository.BudgetRepository
import com.amarant.apps.budgetapp.util.Constants.PERIOD_LAST_MONTH
import com.amarant.apps.budgetapp.util.Constants.PERIOD_ONE_MONTH
import com.amarant.apps.budgetapp.util.Constants.PERIOD_ONE_WEEK
import com.amarant.apps.budgetapp.util.Constants.PERIOD_TODAY
import com.amarant.apps.budgetapp.util.Constants.PERIOD_TWO_WEEKS
import com.amarant.apps.budgetapp.util.Constants.PERIOD_YESTERDAY
import com.amarant.apps.budgetapp.util.UtilityFunctions
import com.amarant.apps.budgetapp.util.UtilityFunctions.dateMillisToString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    val budgetRepository: BudgetRepository
) : ViewModel() {

    val allBudgetEntries: LiveData<List<Budget>> = budgetRepository.getAllBudgetEntries()

    var _dateRangeBudgetEntries: MutableLiveData<List<Budget>> = MutableLiveData()
    val dateRandeBudgetEntries: LiveData<List<Budget>> = _dateRangeBudgetEntries

    fun insertBudget(budget: Budget) = viewModelScope.launch {
        budgetRepository.insertBudget(budget)
    }

    fun updateBudget(amount: Float, purpose: String, category: String, id: Int) = viewModelScope.launch {
        budgetRepository.updateBudget(amount, purpose, category, id)
    }

    fun deleteEntry(budget: Budget) = viewModelScope.launch {
        budgetRepository.deleteEntry(budget)
    }

    fun getReportsBetweenDates(startDate: Long, endDate: Long) = viewModelScope.launch {
        val response = budgetRepository.getBudgetEntriesBetweenDates(startDate, endDate)
        _dateRangeBudgetEntries.postValue(response)
    }

    fun calculateTotalSpending(period: String): LiveData<Float> {
        val start = calculateStartPeriod(period)
        val end = calculateEndPeriod()
        return budgetRepository.getTotalSpendingForPeriod(start, end)
    }

    fun calculateTotalCredit(period: String): LiveData<Float> {
        val start = calculateStartPeriod(period)
        val end = calculateEndPeriod()
        return budgetRepository.getTotalCreditForPeriod(start, end)
    }

    fun getSpendingsByCategory(period: String): LiveData<List<BudgetCategoryDetails>> {
        val start = calculateStartPeriod(period)
        val end = calculateEndPeriod()
        return budgetRepository.getSpendingsByCategory(start, end)
    }

    private fun calculateStartPeriod(period: String): Long {
        val start = when(period) {
            PERIOD_TODAY -> UtilityFunctions.getToday()
            PERIOD_YESTERDAY -> UtilityFunctions.getYesterday()
            PERIOD_ONE_WEEK -> UtilityFunctions.getStartOfWeek()
            PERIOD_TWO_WEEKS -> UtilityFunctions.getStartOfPreviousWeek()
            PERIOD_ONE_MONTH -> UtilityFunctions.getStartOfMonth()
            PERIOD_LAST_MONTH -> UtilityFunctions.getStartOfLastMonth()
            else -> {
                0L
            }
        }
        return start
    }

    private fun calculateEndPeriod(): Long {
        val dateInMillies = Calendar.getInstance().timeInMillis
        val startDate = dateMillisToString(dateInMillies)
        return UtilityFunctions.dateStringToMillis(startDate)
    }
}