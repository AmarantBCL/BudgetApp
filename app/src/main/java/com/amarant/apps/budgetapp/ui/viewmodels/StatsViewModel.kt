package com.amarant.apps.budgetapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.entities.BudgetUI
import com.amarant.apps.budgetapp.entities.Category
import com.amarant.apps.budgetapp.entities.CategoryExpense
import com.amarant.apps.budgetapp.entities.ReportType
import com.amarant.apps.budgetapp.entities.ReportsItem
import com.amarant.apps.budgetapp.repository.BudgetRepository
import com.amarant.apps.budgetapp.util.DateUtils
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_THIS_MONTH
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_THIS_WEEK
import com.amarant.apps.budgetapp.util.UtilityFunctions.calculateEndPeriod
import com.amarant.apps.budgetapp.util.UtilityFunctions.calculateStartPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _dateRange = MutableLiveData(Pair(
        calculateStartPeriod(DEFAULT_PERIOD),
        calculateEndPeriod(DEFAULT_PERIOD))
    )
    val dateRange: LiveData<Pair<Long, Long>>
        get() = _dateRange

    private val _period = MutableLiveData(DEFAULT_PERIOD)
    val period: LiveData<Int>
        get() = _period

    private val _customRangeText = MutableLiveData<String>()
    val customRangeText: LiveData<String>
        get() = _customRangeText

    private val _reportType = MutableLiveData(ReportType.EXPENSE)
    val reportType: LiveData<ReportType>
        get() = _reportType

    val categoryExpenses = getBudgetEntriesBetweenDates().map { budgets ->
        val list = mutableListOf<CategoryExpense>()
        val totalSum = budgets//.filter { it.budget.creditOrDebit == "Debit" }
            .sumOf { it.budget.amount.toInt() }
        budgets//.filter { it.budget.creditOrDebit == "Debit" }
            .groupBy { it.budget.category }.forEach { (category, items) ->
                val amount = items.sumOf { it.budget.amount.toInt() }.toFloat()
                list.add(
                    CategoryExpense(
                    category,
                    items.size,
                    amount,
                    amount / totalSum * 100.0
                )
                )
            }
        list.sortedBy { it.amount }
    }

    fun getBudgetEntriesBetweenDates(): LiveData<List<BudgetUI>> {
        val result = MediatorLiveData<List<BudgetUI>>()
        val dbSource = dateRange.switchMap { range ->
//            appliedFilter.switchMap { filter ->
//                searchQuery.switchMap { query ->
                    budgetRepository.getBudgetEntriesBetweenDates(range.first, range.second, Category.ALL)
//                }
//            }
        }
        fun update(budgets: List<Budget>?, type: ReportType?) {
            if (budgets != null && type != null) {
                val filtered = when (type) {
                    ReportType.ALL -> budgets
                    ReportType.INCOME -> budgets.filter { it.amount > 0 }
                    ReportType.EXPENSE -> budgets.filter { it.amount < 0 }
                }
                result.value = filtered.map { budget ->
                    BudgetUI(budget, isHidden = false)
                }
            }
        }
        result.addSource(dbSource) { budgets -> update(budgets, reportType.value) }
//        result.addSource(selectedIds) { selected -> update(dbSource.value, selected, reportType.value) }
        result.addSource(reportType) { type -> update(dbSource.value, type) }
        return result
    }

    fun getExpensesByCategory(category: Category): LiveData<List<ReportsItem>> {
        return getBudgetEntriesBetweenDates().map { reports ->
            val groupedList = mutableListOf<ReportsItem>()
            reports.filter { it.budget.category == category }// && it.budget.creditOrDebit == "Debit" }
                .groupBy { it.budget.date }
                .forEach { (date, items) ->
                    groupedList.add(ReportsItem.DateHeader(DateUtils.getFormattedDate(date.toLong())))
                    items.reversed().forEach { budgetUI ->
                        groupedList.add(ReportsItem.Entry(budgetUI))
                    }
                }
            groupedList
        }
    }

    fun setReportsBetweenDates(startDate: Long, endDate: Long) {
        _dateRange.value = Pair(startDate, endDate)
    }

    fun changeDateRange(position: Int, isPeriodOnly: Boolean = false) {
        if (isPeriodOnly) {
            _period.value = position
        } else {
            val start = calculateStartPeriod(position)
            val end = calculateEndPeriod(position)
            setReportsBetweenDates(start, end)
            _period.value = position
        }
    }

    fun setCustomRangeDisplayedText(text: String) {
        _customRangeText.value = text
    }

    fun setType(type: ReportType) {
        _reportType.value = type
    }

    private companion object {
        private const val DEFAULT_PERIOD = PERIOD_THIS_MONTH
    }
}