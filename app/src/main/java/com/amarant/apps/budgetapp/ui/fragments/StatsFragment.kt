package com.amarant.apps.budgetapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentStatsBinding
import com.amarant.apps.budgetapp.entities.BudgetUI
import com.amarant.apps.budgetapp.entities.Category
import com.amarant.apps.budgetapp.entities.ReportType
import com.amarant.apps.budgetapp.ui.adapter.CategoryExpenseAdapter
import com.amarant.apps.budgetapp.ui.fragments.bottomsheet.PeriodBottomSheetFragment
import com.amarant.apps.budgetapp.ui.fragments.bottomsheet.ReportTypeBottomSheetFragment
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.StatsViewModel
import com.amarant.apps.budgetapp.util.NumberUtils
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_SHOW_ALL
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlin.math.absoluteValue

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding: FragmentStatsBinding
        get() = _binding ?: throw RuntimeException("FragmentStatsBinding == null")

//    private val budgetViewModel: BudgetViewModel by activityViewModels()
    private val statsViewModel: StatsViewModel by activityViewModels()

    private lateinit var categoryExpenseAdapter: CategoryExpenseAdapter

    private var totalSum = 0
    private var isIncome = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPieChart()
        initRecyclerView()
        observeViewModel()
        setClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initPieChart() {
        with(binding.pieChart) {
            setNoDataText("")
            isRotationEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 55f
            animateY(300)
        }
    }

    private fun observeViewModel() {
        statsViewModel.getBudgetEntriesBetweenDates().observe(viewLifecycleOwner) { reports ->
            totalSum = getTotalSum(reports)
            val formattedSum = NumberUtils.formatNumberWithThousandsSeparator(totalSum.toDouble())
            if (totalSum > 0) {
                binding.tvTotal.text = getString(R.string.plus_placeholder, formattedSum)
                binding.tvTotal.setTextColor(ContextCompat.getColor(requireContext(), R.color.positive_green))
            } else {
                binding.tvTotal.text = formattedSum
                binding.tvTotal.setTextColor(ContextCompat.getColor(requireContext(), R.color.negative_red))
            }
            initDataSet(prepareDataSet(reports))
        }
//        budgetViewModel.getBudgetEntriesBetweenDates().observe(viewLifecycleOwner) { reports ->
//            totalSum = getTotalSum(reports)
//            binding.tvTotal.text = NumberUtils.formatNumberWithThousandsSeparator(totalSum.toDouble())
//            initDataSet(prepareDataSet(reports))
//        }
        statsViewModel.categoryExpenses.observe(viewLifecycleOwner) {
            categoryExpenseAdapter.submitList(it)
        }
//        budgetViewModel.categoryExpenses.observe(viewLifecycleOwner) {
//            categoryExpenseAdapter.submitList(it)
//        }
        statsViewModel.period.observe(viewLifecycleOwner) {
            binding.chipPeriod.text = resources.getStringArray(R.array.periods)[it]
            if (it != PERIOD_SHOW_ALL) {
                binding.chipPeriod.setChipBackgroundColorResource(R.color.background_dark_purple)
            } else {
                binding.chipPeriod.setChipBackgroundColorResource(R.color.card_background)
            }
        }
        statsViewModel.customRangeText.observe(viewLifecycleOwner) {
            if (it != "") {
                binding.chipPeriod.text = it
            }
        }
        statsViewModel.reportType.observe(viewLifecycleOwner) {
            val allReportTypes = resources.getStringArray(R.array.report_types)
            binding.chipType.text = allReportTypes[it.ordinal]
            val iconRes = when(it) {
                ReportType.INCOME -> R.drawable.ic_trend
                ReportType.EXPENSE -> R.drawable.ic_expenses
                else -> R.drawable.ic_loop
            }
            val drawable = ContextCompat.getDrawable(requireContext(), iconRes)
            binding.chipType.chipIcon = drawable
            if (it != ReportType.ALL) {
                binding.chipType.setChipBackgroundColorResource(R.color.background_dark_purple)
            } else {
                binding.chipType.setChipBackgroundColorResource(R.color.card_background)
            }
            isIncome = it == ReportType.INCOME
        }
    }

    private fun initDataSet(entries: List<PieEntry>) {
        val dataSet = PieDataSet(entries, "")
        dataSet.sliceSpace = 2f
        dataSet.setDrawValues(false)
        val colors = mutableListOf<Int>()
        for (entry in entries) {
            val color = (entry.data as Category).color
            colors.add(ContextCompat.getColor(requireContext(), color))
        }
        dataSet.colors = colors
        val pieData = PieData(dataSet)
        binding.pieChart.data = pieData
        if (entries.isNotEmpty()) {
            binding.pieChart.highlightValue(0f, 0)
            initSelectedSection(entries.first())
        }
        initVisibility(entries.isEmpty())
    }

    private fun initRecyclerView() {
        categoryExpenseAdapter = CategoryExpenseAdapter()
        val divider = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.recyclerCategoryExpenses.addItemDecoration(divider)
        binding.recyclerCategoryExpenses.adapter = categoryExpenseAdapter
        categoryExpenseAdapter.onCategoryExpenseClickListener = {
            val action = StatsFragmentDirections.actionStatsFragmentToExpensesFragment(it)
            findNavController().navigate(action)
        }
    }

    private fun setClickListeners() {
        binding.pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                initSelectedSection(e as PieEntry)
            }

            override fun onNothingSelected() {
            }
        })
        binding.chipPeriod.setOnClickListener {
            val bottomSheet = PeriodBottomSheetFragment.newInstance(isStats = true)
            bottomSheet.periodSelectionListener = object : PeriodBottomSheetFragment.PeriodSelectionListener {
                override fun onPeriodSelected(periodId: Int, customStart: Long, customEnd: Long, customText: String) {
                    if (periodId == PeriodBottomSheetFragment.CUSTOM_DATE_RANGE_ID) {
                        statsViewModel.changeDateRange(periodId, isPeriodOnly = true)
                        statsViewModel.setReportsBetweenDates(customStart, customEnd)
                        statsViewModel.setCustomRangeDisplayedText(customText)
                    } else {
                        statsViewModel.changeDateRange(periodId)
                        statsViewModel.setCustomRangeDisplayedText("")
                    }
                }
            }
            bottomSheet.show(requireActivity().supportFragmentManager, PeriodBottomSheetFragment.TAG)
        }
        binding.chipType.setOnClickListener {
            val bottomSheet = ReportTypeBottomSheetFragment.newInstance(isStats = true)
            bottomSheet.reportTypeSelectionListener = object : ReportTypeBottomSheetFragment.ReportTypeSelectionListener {
                override fun onTypeSelected(type: ReportType) {
                    statsViewModel.setType(type)
                }
            }
            bottomSheet.show(requireActivity().supportFragmentManager, ReportTypeBottomSheetFragment.TAG)
        }
    }

    private fun initVisibility(isEntriesEmpty: Boolean) {
        binding.lblTotal.visibility = if (isEntriesEmpty) View.GONE else View.VISIBLE
        binding.tvTotal.visibility = if (isEntriesEmpty) View.GONE else View.VISIBLE
        binding.llNoEntries.visibility = if (isEntriesEmpty) View.VISIBLE else View.GONE
        binding.lblCategoryExpenses.visibility = if (isEntriesEmpty) View.GONE else View.VISIBLE
        binding.recyclerCategoryExpenses.visibility = if (isEntriesEmpty) View.GONE else View.VISIBLE
//        binding.chipGroupFilters.visibility = if (isEntriesEmpty) View.GONE else View.VISIBLE
        binding.pieChart.visibility = if (isEntriesEmpty) View.GONE else View.VISIBLE
        binding.imgIcon.visibility = if (isEntriesEmpty) View.GONE else View.VISIBLE
        binding.tvCategory.visibility = if (isEntriesEmpty) View.GONE else View.VISIBLE
        binding.tvSum.visibility = if (isEntriesEmpty) View.GONE else View.VISIBLE
        binding.tvPercent.visibility = if (isEntriesEmpty) View.GONE else View.VISIBLE
    }

    private fun getTotalSum(reports: List<BudgetUI>): Int {
        return reports//.filter { it.budget.creditOrDebit == "Debit" }
            .sumOf { it.budget.amount.toInt() }
    }

    private fun prepareDataSet(reports: List<BudgetUI>): List<PieEntry> {
        val list = mutableListOf<PieEntry>()
        reports//.filter { it.budget.creditOrDebit == "Debit" }
            .groupBy { it.budget.category }
            .forEach { (category, items) ->
                list.add(PieEntry(
                    items.sumOf { it.budget.amount.toInt() }.toFloat().absoluteValue,
                    category
                ))
            }
        return list.sortedByDescending { it.value }
    }

    private fun initSelectedSection(entry: PieEntry) {
        val category = entry.data as Category
        val value = entry.value
        val percent = NumberUtils.formatDecimal((value / totalSum.absoluteValue) * 100.0)
        binding.imgIcon.setImageResource(category.iconRes)
        binding.tvCategory.text = category.getLocalizedName(requireContext())
        if (isIncome) {
            binding.tvSum.text = getString(R.string.plus_placeholder, NumberUtils.formatNumberWithThousandsSeparator(value * 1.0))
            binding.tvSum.setTextColor(ContextCompat.getColor(requireContext(), R.color.positive_green))
        } else {
            binding.tvSum.text = NumberUtils.formatNumberWithThousandsSeparator(value * -1.0)
            binding.tvSum.setTextColor(ContextCompat.getColor(requireContext(), R.color.negative_red))
        }
        binding.tvPercent.text = getString(R.string.percent_placeholder, percent)
    }
}