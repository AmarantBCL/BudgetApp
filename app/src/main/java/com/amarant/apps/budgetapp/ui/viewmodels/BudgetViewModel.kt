package com.amarant.apps.budgetapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.entities.BudgetCategoryDetails
import com.amarant.apps.budgetapp.entities.BudgetUI
import com.amarant.apps.budgetapp.entities.Category
import com.amarant.apps.budgetapp.entities.CategoryExpense
import com.amarant.apps.budgetapp.entities.ReportType
import com.amarant.apps.budgetapp.entities.ReportsItem
import com.amarant.apps.budgetapp.entities.SortField
import com.amarant.apps.budgetapp.entities.SortOption
import com.amarant.apps.budgetapp.entities.SortOrder
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
import com.github.mikephil.charting.data.PieEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.absoluteValue

@HiltViewModel
class BudgetViewModel @Inject constructor(
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

    private val _appliedFilter = MutableLiveData(Category.ALL)
    val appliedFilter: LiveData<Category>
        get() = _appliedFilter

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String>
        get() = _searchQuery

    private val _sorting = MutableLiveData(SortOption(SortField.DATE, SortOrder.DESC))
    val sorting: LiveData<SortOption>
        get() = _sorting

    private val _reportType = MutableLiveData(ReportType.ALL)
    val reportType: LiveData<ReportType>
        get() = _reportType

    private val selectedIds = MutableLiveData<Set<Int>>(emptySet())

    val testCategoryData = getBudgetEntriesBetweenDates().map { budgets ->
        val list = mutableListOf<CategoryExpense>()
        val totalSum = budgets.sumOf { it.budget.amount.toInt() }
        budgets.filter { it.budget.creditOrDebit == "Debit" }
            .groupBy { it.budget.category }.forEach { (category, items) ->
            val amount = items.sumOf { it.budget.amount.toInt() }.toFloat()
                list.add(CategoryExpense(
                    category,
                    items.size,
                    amount,
                    amount / totalSum * 100.0
                ))
            }
        list.sortedBy { it.amount }
    }

    fun setCustomRangeDisplayedText(text: String) {
        _customRangeText.value = text
    }

    val groupedEntries: LiveData<List<ReportsItem>> = getBudgetEntriesBetweenDates().map { reports ->
        val groupedList = mutableListOf<ReportsItem>()
        reports.groupBy { it.budget.date }.forEach { (date, items) ->
            groupedList.add(ReportsItem.DateHeader(DateUtils.getFormattedDate(date.toLong())))
            items.reversed().forEach { budgetUI ->
                groupedList.add(ReportsItem.Entry(budgetUI))
            }
        }
        groupedList
    }

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

    fun toggleSelection(id: Int) {
        val current = selectedIds.value ?: emptySet()
        selectedIds.value = if (current.contains(id)) {
            current - id
        } else {
            current + id
        }
    }

    fun getBudgetEntriesBetweenDates(): LiveData<List<BudgetUI>> {
        val result = MediatorLiveData<List<BudgetUI>>()
        val dbSource = dateRange.switchMap { range ->
            appliedFilter.switchMap { filter ->
                searchQuery.switchMap { query ->
                    budgetRepository.getBudgetEntriesBetweenDates(range.first, range.second, filter, query)
                }
            }
        }
        fun update(budgets: List<Budget>?, selected: Set<Int>?, type: ReportType?) {
            if (budgets != null && selected != null && type != null) {
                val filtered = when (type) {
                    ReportType.ALL -> budgets
                    ReportType.INCOME -> budgets.filter { it.amount > 0 }
                    ReportType.EXPENSE -> budgets.filter { it.amount < 0 }
                }
                result.value = filtered.map { budget ->
                    BudgetUI(budget, isHidden = budget.id in selected)
                }
            }
        }
        result.addSource(dbSource) { budgets -> update(budgets, selectedIds.value, reportType.value) }
        result.addSource(selectedIds) { selected -> update(dbSource.value, selected, reportType.value) }
        result.addSource(reportType) { type -> update(dbSource.value, selectedIds.value, type) }
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

    fun getSpendingByCategory(period: Int): LiveData<List<BudgetCategoryDetails>> {
        val start = calculateStartPeriod(period)
        val end = calculateEndPeriod(period)
        return budgetRepository.getSpendingsByCategory(start, end)
    }

    fun applyFilter(filter: Category) {
        _appliedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun validateAndAddEntries(isDebit: Boolean, amount: String, purpose: String, date: Long, category: Category): Boolean {
        val bankName = ""
        val debitOrCredit = if (isDebit) Constants.DEBIT else Constants.CREDIT
        val amountAsInt = amount.toIntOrNull()
        return if (amountAsInt == null || purpose.isEmpty()) {
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
                category = category
            ))
            true
        }
    }

    fun validateAndEditEntries(id: Int, isDebit: Boolean, amount: String, purpose: String, category: Category): Boolean {
        val debitOrCredit = if (isDebit) Constants.DEBIT else Constants.CREDIT
        val amountAsInt = amount.toIntOrNull()
        return if (amountAsInt == null || purpose.isEmpty()) {
            false
        } else {
            var amountToInsert = amountAsInt.toFloat()
            if (debitOrCredit == Constants.DEBIT) {
                amountToInsert *= -1
            }
            updateBudget(debitOrCredit, amountToInsert, purpose, category.dbName, id)
            true
        }
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

    fun setSort(field: SortField, order: SortOrder) {
        _sorting.value = SortOption(field, order)
    }

    fun setType(type: ReportType) {
        _reportType.value = type
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
                val dateInMillis = Calendar.getInstance().timeInMillis
                val startDate = dateMillisToString(dateInMillis)
                UtilityFunctions.dateStringToMillis(startDate)
            }
        }
        return end
    }

    private companion object {
        private const val DEFAULT_PERIOD = PERIOD_THIS_MONTH
    }
}