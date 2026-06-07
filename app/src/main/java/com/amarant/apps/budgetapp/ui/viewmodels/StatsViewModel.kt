package com.amarant.apps.budgetapp.ui.viewmodels

import android.R.attr.type
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.amarant.apps.budgetapp.entities.BarChartItem
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.entities.BudgetUI
import com.amarant.apps.budgetapp.entities.Category
import com.amarant.apps.budgetapp.entities.CategoryExpense
import com.amarant.apps.budgetapp.entities.ReportType
import com.amarant.apps.budgetapp.entities.ReportsItem
import com.amarant.apps.budgetapp.repository.BudgetRepository
import com.amarant.apps.budgetapp.util.DateUtils
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_SIX_MONTHS
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_TWO_DAYS
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_TWO_MONTHS
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_TWO_WEEKS
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_TWO_YEARS
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_THIS_MONTH
import com.amarant.apps.budgetapp.util.UtilityFunctions.calculateEndPeriod
import com.amarant.apps.budgetapp.util.UtilityFunctions.calculateStartPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs
import kotlin.collections.contains

enum class ChartType {
    PIE, BAR
}

data class BarChartData(
    val labels: List<String>,
    val incomeValues: List<Float>,
    val expenseValues: List<Float>,
    val items: List<BarChartItem> = emptyList()
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _chartType = MutableLiveData(ChartType.PIE)
    val chartType: LiveData<ChartType>
        get() = _chartType

    fun setChartType(type: ChartType) {
        _chartType.value = type
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

    fun getExpensesByCategory(category: Category): LiveData<List<ReportsItem>> {
        return budgetEntries.map { budgets ->
            val groupedList = mutableListOf<ReportsItem>()
            budgets.filter { it.budget.category == category }
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

    fun setCustomRangeDisplayedText(text: String) {
        _customRangeText.value = text
    }

    fun setType(type: ReportType) {
        _reportType.value = type
    }

    fun toggleCategorySelection(category: Category) {
        val current = hiddenCategories.value ?: emptySet()
        hiddenCategories.value = if (current.contains(category)) {
            current - category
        } else {
            current + category
        }
    }

    fun toggleBudgetSelection(id: Int) {
        val current = selectedIds.value ?: emptySet()
        selectedIds.value = if (current.contains(id)) {
            current - id
        } else {
            current + id
        }
    }

    fun getBudgetEntriesBetweenDates(startDate: Long, endDate: Long, category: Category): LiveData<List<BudgetUI>> {
        return budgetRepository.getBudgetEntriesBetweenDates(startDate, endDate, category).switchMap { budgets ->
            selectedIds.map { selected ->
                budgets.map { budget ->
                    BudgetUI(budget, isHidden = budget.id in selected)
                }
            }
        }
    }


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

    private val selectedIds = MutableLiveData<Set<Int>>(emptySet())

    private val hiddenCategories = MutableLiveData<Set<Category>>(emptySet())

    val budgetEntries: LiveData<List<BudgetUI>> = dateRange.switchMap { range ->
        budgetRepository.getBudgetEntriesBetweenDates(range.first, range.second, Category.ALL).switchMap { budgets ->
            reportType.switchMap { type ->
                selectedIds.map { selected ->
                    val filtered = when (type) {
                        ReportType.ALL -> budgets
                        ReportType.INCOME -> budgets.filter { it.amount > 0 }
                        ReportType.EXPENSE -> budgets.filter { it.amount < 0 }
                    }
                    filtered.map { budget ->
                        BudgetUI(budget, isHidden = budget.id in selected)
                    }
                }
            }
        }
    }

    val pieChartEntries: LiveData<List<BudgetUI>> = hiddenCategories.switchMap { hiddenSet ->
        budgetEntries.map { budgets ->
            budgets.filter { !hiddenSet.contains(it.budget.category) }
        }
    }

    val barChartEntries: LiveData<BarChartData> = hiddenCategories.switchMap { hiddenSet ->
        budgetEntries.switchMap { budgets ->
            period.map { periodId ->
                val calendar = Calendar.getInstance()
                val groupedData = mutableMapOf<String, Float>()
                val entriesCount = mutableMapOf<String, Int>()
                val keySortMap = mutableMapOf<String, Long>()
                val dateRanges = mutableMapOf<String, Pair<Long, Long>>()

                budgets.filter { !hiddenSet.contains(it.budget.category) }.forEach { item ->
                    calendar.timeInMillis = item.budget.date.toLong()
                    val label: String
                    val sortKey: Long
                    val itemStart: Long
                    val itemEnd: Long

                    when {
                        periodId <= PERIOD_LAST_TWO_DAYS -> {
                            label = SimpleDateFormat("dd MMM", Locale.getDefault()).format(calendar.time)
                            sortKey = item.budget.date.toLong()
                            itemStart = sortKey
                            itemEnd = sortKey
                        }
                        periodId < PERIOD_LAST_TWO_WEEKS -> {
                            label = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
                            sortKey = calendar.timeInMillis

                            val dayCal = calendar.clone() as Calendar
                            dayCal.set(Calendar.HOUR_OF_DAY, 0)
                            dayCal.set(Calendar.MINUTE, 0)
                            dayCal.set(Calendar.SECOND, 0)
                            dayCal.set(Calendar.MILLISECOND, 0)
                            itemStart = dayCal.timeInMillis
                            dayCal.set(Calendar.HOUR_OF_DAY, 23)
                            dayCal.set(Calendar.MINUTE, 59)
                            dayCal.set(Calendar.SECOND, 59)
                            itemEnd = dayCal.timeInMillis
                        }
                        periodId <= PERIOD_LAST_TWO_MONTHS -> {
                            label = SimpleDateFormat("dd MMM", Locale.getDefault()).format(calendar.time)
                            sortKey = item.budget.date.toLong()
                            itemStart = sortKey
                            itemEnd = sortKey
                        }
                        periodId < PERIOD_LAST_TWO_YEARS -> {
                            label = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
                            sortKey = calendar.get(Calendar.MONTH).toLong()

                            val monthCal = calendar.clone() as Calendar
                            monthCal.set(Calendar.DAY_OF_MONTH, 1)
                            monthCal.set(Calendar.HOUR_OF_DAY, 0)
                            itemStart = monthCal.timeInMillis
                            monthCal.set(Calendar.DAY_OF_MONTH, monthCal.getActualMaximum(Calendar.DAY_OF_MONTH))
                            monthCal.set(Calendar.HOUR_OF_DAY, 23)
                            itemEnd = monthCal.timeInMillis
                        }
                        else -> {
                            label = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(calendar.time)
                            sortKey = calendar.get(Calendar.YEAR).toLong() * 100 + calendar.get(Calendar.MONTH)

                            val monthCal = calendar.clone() as Calendar
                            monthCal.set(Calendar.DAY_OF_MONTH, 1)
                            monthCal.set(Calendar.HOUR_OF_DAY, 0)
                            itemStart = monthCal.timeInMillis
                            monthCal.set(Calendar.DAY_OF_MONTH, monthCal.getActualMaximum(Calendar.DAY_OF_MONTH))
                            monthCal.set(Calendar.HOUR_OF_DAY, 23)
                            itemEnd = monthCal.timeInMillis
                        }
                    }

                    val current = groupedData.getOrDefault(label, 0f)
                    groupedData[label] = current + abs(item.budget.amount)
                    entriesCount[label] = entriesCount.getOrDefault(label, 0) + 1
                    
                    if (!keySortMap.containsKey(label) || sortKey < keySortMap[label]!!) {
                        keySortMap[label] = sortKey
                        dateRanges[label] = itemStart to itemEnd
                    }
                }

                val sortedLabels = groupedData.keys.sortedBy { keySortMap[it] }
                val items = sortedLabels.map { label ->
                    BarChartItem(
                        label = label,
                        amount = groupedData[label] ?: 0f,
                        entries = entriesCount[label] ?: 0,
                        startDate = dateRanges[label]?.first ?: 0L,
                        endDate = dateRanges[label]?.second ?: 0L
                    )
                }.reversed() // Most recent first for the list

                BarChartData(
                    sortedLabels,
                    sortedLabels.map { groupedData[it] ?: 0f },
                    emptyList(),
                    items
                )
            }
        }
    }


    val isCurrentChartEmpty = MediatorLiveData<Boolean>().apply {
        fun update() {
            val type = _chartType.value ?: ChartType.PIE
            val pieEmpty = pieChartEntries.value.isNullOrEmpty()
            val barEmpty = barChartEntries.value?.labels.isNullOrEmpty()
            value = if (type == ChartType.PIE) pieEmpty else barEmpty
        }
        addSource(_chartType) { update() }
        addSource(pieChartEntries) { update() }
        addSource(barChartEntries) { update() }
    }

    val categoryExpenses = hiddenCategories.switchMap { hiddenSet ->
        budgetEntries.map { budgets ->
            val list = mutableListOf<CategoryExpense>()
            val totalSum = budgets.sumOf { it.budget.amount.toInt() }
            budgets.groupBy { it.budget.category }.forEach { (category, items) ->
                val amount = items.sumOf { it.budget.amount.toInt() }.toFloat()
                val isHidden = hiddenSet.contains(category)
                list.add(CategoryExpense(category, items.size, amount, amount / totalSum * 100.0, isHidden))
            }
            list.sortedBy { it.amount }
        }
    }


    private companion object {
        private const val DEFAULT_PERIOD = PERIOD_THIS_MONTH
    }
}