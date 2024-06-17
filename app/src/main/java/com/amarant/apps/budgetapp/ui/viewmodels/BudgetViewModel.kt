package com.amarant.apps.budgetapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.repository.BudgetRepository
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
    val totalCredit: LiveData<Float> = budgetRepository.getTotalCredit()
    val totalSpending: LiveData<Float> = budgetRepository.getTotalSpending()

    var _dateRangeBudgetEntries: MutableLiveData<List<Budget>> = MutableLiveData()
    val dateRandeBudgetEntries: LiveData<List<Budget>> = _dateRangeBudgetEntries

    fun insertBudget(budget: Budget) = viewModelScope.launch {
        budgetRepository.insertBudget(budget)
    }

    fun updateBudget(amount: Float, purpose: String, id: Int) = viewModelScope.launch {
        budgetRepository.updateBudget(amount, purpose, id)
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
        Log.d("WTF", "period: $period | start: ${dateMillisToString(start)} & end: ${dateMillisToString(end)}")
        return budgetRepository.getTotalSpendingForPeriod(start, end)
    }

    fun calculateTotalCredit(period: String): LiveData<Float> {
        val start = calculateStartPeriod(period)
        val end = calculateEndPeriod()
        Log.d("WTF", "period: $period | start: ${dateMillisToString(start)} & end: ${dateMillisToString(end)}")
        return budgetRepository.getTotalCreditForPeriod(start, end)
    }

    private fun calculateStartPeriod(period: String): Long {
        val start = when(period) {
            "1 Week" -> UtilityFunctions.getStartOfWeek()
            "2 Weeks" -> UtilityFunctions.getStartOfPreviousWeek()
            "1 Month" -> UtilityFunctions.getStartOfMonth()
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