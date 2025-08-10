package com.amarant.apps.budgetapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.entities.BudgetCategoryDetails
import com.amarant.apps.budgetapp.entities.CategoryStat
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

    fun insertBudget(budget: Budget) = viewModelScope.launch {
        budgetRepository.insertBudget(budget)
    }

    fun updateBudget(amount: Float, purpose: String, category: String, id: Int) =
        viewModelScope.launch {
            budgetRepository.updateBudget(amount, purpose, category, id)
        }

    fun deleteEntry(budget: Budget) = viewModelScope.launch {
        budgetRepository.deleteEntry(budget)
    }

    fun setReportsBetweenDates(startDate: Long, endDate: Long) {
        _dateRange.value = Pair(startDate, endDate)
    }

    fun getReportsBetweenDates(): LiveData<List<Budget>> {
        return dateRange.switchMap { pair ->
            appliedFilter.switchMap { filter ->
                budgetRepository.getBudgetEntriesBetweenDates(pair.first, pair.second, filter)
            }
        }
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

    fun getCategoryStats(): LiveData<List<CategoryStat>> {
        return budgetRepository.getCategoryStats()
    }

    fun validateEntries(isDebit: Boolean, amount: String, purpose: String, date: Long, categoryName: String): Boolean {
        val bankName = ""
        val debitOrCredit = if (isDebit) Constants.DEBIT else Constants.CREDIT
        val amountAsInt = amount.toIntOrNull()
//        val dateInMillis = UtilityFunctions.dateStringToMillis(date).toString()
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