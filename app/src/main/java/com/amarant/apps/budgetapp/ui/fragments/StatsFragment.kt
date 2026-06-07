package com.amarant.apps.budgetapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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
import com.amarant.apps.budgetapp.ui.MainActivity
import com.amarant.apps.budgetapp.ui.adapter.CategoryExpenseAdapter
import com.amarant.apps.budgetapp.ui.fragments.bottomsheet.PeriodBottomSheetFragment
import com.amarant.apps.budgetapp.ui.fragments.bottomsheet.ReportTypeBottomSheetFragment
import com.amarant.apps.budgetapp.ui.viewmodels.ChartType
import com.amarant.apps.budgetapp.ui.viewmodels.StatsViewModel
import com.amarant.apps.budgetapp.util.NumberUtils
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_SHOW_ALL
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlin.math.absoluteValue
import androidx.core.view.get
import com.amarant.apps.budgetapp.ui.adapter.BarChartItemAdapter


class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding: FragmentStatsBinding
        get() = _binding ?: throw RuntimeException("FragmentStatsBinding == null")

//    private val budgetViewModel: BudgetViewModel by activityViewModels()
    private val statsViewModel: StatsViewModel by activityViewModels()

    private lateinit var categoryExpenseAdapter: CategoryExpenseAdapter
    private lateinit var barChartItemAdapter: BarChartItemAdapter

    private var totalSum = 0
    private var isIncome = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


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
        initBarChart()
        initRecyclerView()
        observeViewModel()
        setClickListeners()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.stats_menu, menu)
        val current = statsViewModel.chartType.value ?: ChartType.PIE
        val next = if (current == ChartType.PIE) ChartType.BAR else ChartType.PIE
        menu[0].setIcon(if (next == ChartType.PIE) R.drawable.ic_pie_chart else R.drawable.ic_bar_chart)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_chart -> {
                val current = statsViewModel.chartType.value ?: ChartType.PIE
                val next = if (current == ChartType.PIE) ChartType.BAR else ChartType.PIE
                statsViewModel.setChartType(next)
                
                // Toggle the icon
                item.setIcon(if (next == ChartType.PIE) R.drawable.ic_bar_chart else R.drawable.ic_pie_chart)
                true
            }
            R.id.action_settings -> {
                findNavController().navigate(StatsFragmentDirections.actionGlobalProfileFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

    private fun initBarChart() {
        with(binding.barChart) {
            description.isEnabled = false
            legend.isEnabled = true
            legend.textColor = ContextCompat.getColor(requireContext(), R.color.primary_white)
            legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
            
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.primary_white)
            xAxis.granularity = 1f
            xAxis.labelRotationAngle = 0f // Make it horizontal as requested
            xAxis.setDrawAxisLine(false)
            
            axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.primary_white)
            axisLeft.setDrawGridLines(true)
            axisLeft.gridColor = ContextCompat.getColor(requireContext(), R.color.divider_color)
            axisLeft.setDrawAxisLine(false)
            axisRight.isEnabled = false
            
            setNoDataText(getString(R.string.no_entries_for_this_period))
            setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.light_gray))
            
            setTouchEnabled(true)
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawGridBackground(false)
            setScaleEnabled(false)
            
            // Note: Rounded corners require custom renderer which we'll skip for now 
            // if it causes compile issues, but we'll try to apply it if successful.
        }
    }


    private fun observeViewModel() {
        statsViewModel.isCurrentChartEmpty.observe(viewLifecycleOwner) { isEmpty ->
            initVisibility(isEmpty)
        }

        statsViewModel.chartType.observe(viewLifecycleOwner) { type ->
            // Clear selections when switching charts
            binding.pieChart.highlightValue(null)
            binding.barChart.highlightValue(null)
            
            // Reset labels to overall total
            binding.lblTotal.setText(R.string.total)
            val formattedSum = NumberUtils.formatNumberWithThousandsSeparator(totalSum.toDouble())
            if (totalSum > 0) {
                binding.tvTotal.text = getString(R.string.plus_placeholder, formattedSum)
                binding.tvTotal.setTextColor(ContextCompat.getColor(requireContext(), R.color.positive_green))
            } else {
                binding.tvTotal.text = formattedSum
                binding.tvTotal.setTextColor(ContextCompat.getColor(requireContext(), R.color.negative_red))
            }

            // Update visibility of elements when chart type changes
            initVisibility(statsViewModel.isCurrentChartEmpty.value ?: false)
            
            // Restore Type chip visibility for all modes
            binding.chipType.visibility = View.VISIBLE
        }

        statsViewModel.barChartEntries.observe(viewLifecycleOwner) { data ->
            // Update the new adapter with time period items
            barChartItemAdapter.isIncome = isIncome
            barChartItemAdapter.submitList(data.items)

            // Clear selection and reset header when data changes (e.g. filter/category toggle)
            binding.barChart.highlightValue(null)
            if (statsViewModel.chartType.value == ChartType.BAR) {
                binding.lblTotal.setText(R.string.total)
            }

            if (data.labels.isEmpty()) {
                binding.barChart.data = null
                binding.barChart.invalidate()
                return@observe
            }

            // Map values to entries (ViewModel now only sends the relevant values based on filter)
            val entries = data.incomeValues.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }

            val type = statsViewModel.reportType.value ?: ReportType.EXPENSE
            val colorRes = if (type == ReportType.INCOME) R.color.positive_green else R.color.negative_red
            val color = ContextCompat.getColor(requireContext(), colorRes)
            
            val dataLabel = if (type == ReportType.INCOME) getString(R.string.income) else getString(R.string.expense)
            val dataSet = BarDataSet(entries, dataLabel)
            dataSet.color = color
            dataSet.setDrawValues(false)
            
            // Highlight styling
            dataSet.highLightColor = ContextCompat.getColor(requireContext(), R.color.primary_white)
            dataSet.highLightAlpha = 40 // Subdued white "glow"
            
            // Apple-style Gradient: Full color at top, same color with 40% alpha at bottom
            val startColor = color
            val endColor = (color and 0x00FFFFFF) or 0x66000000 // 40% alpha

            dataSet.setGradientColor(startColor, endColor)

            val barData = BarData(dataSet)
            barData.barWidth = 0.5f // Elegant bar thickness
            
            binding.barChart.data = barData
            
            // X-axis horizontal and clean
            binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(data.labels)
            binding.barChart.xAxis.axisMinimum = -0.5f
            binding.barChart.xAxis.axisMaximum = data.labels.size.toFloat() - 0.5f
            binding.barChart.xAxis.setCenterAxisLabels(false)
            binding.barChart.xAxis.granularity = 1f
            
            // Apply Rounded Tops Renderer
            try {
                binding.barChart.renderer = com.amarant.apps.budgetapp.util.RoundedBarChartRenderer(
                    binding.barChart, binding.barChart.animator, binding.barChart.viewPortHandler, 25f
                )
            } catch (e: Exception) {
                // Stay on default if renderer has issues
            }

            binding.barChart.invalidate()
            binding.barChart.animateY(500)
        }

        statsViewModel.pieChartEntries.observe(viewLifecycleOwner) { reports ->
            totalSum = getTotalSum(reports)

            // Clear selection and reset header when data changes
            binding.pieChart.highlightValue(null)
            if (statsViewModel.chartType.value == ChartType.PIE) {
                binding.lblTotal.setText(R.string.total)
            }

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
            // Update adapter state
            if (::barChartItemAdapter.isInitialized) {
                barChartItemAdapter.isIncome = isIncome
            }
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
        categoryExpenseAdapter.onCategoryExpenseClickListener = { category ->
            val action = StatsFragmentDirections.actionStatsFragmentToExpensesFragment(
                category = category,
                isIncome = isIncome
            )
            findNavController().navigate(action)
        }
        categoryExpenseAdapter.onCategoryExpenseLongClickListener = { category, isHidden ->
            val currentItems = categoryExpenseAdapter.currentList.size
            val hiddenItems = categoryExpenseAdapter.currentList.filter { it.isHidden }.size
            if (!isHidden) {
                if (currentItems - 1 > hiddenItems) {
                    statsViewModel.toggleCategorySelection(category)
                } else {
                    (requireActivity() as MainActivity).showSnackbarMessage(
                        binding.root, getString(R.string.cant_hide_more_categories)
                    )
                }
            } else {
                statsViewModel.toggleCategorySelection(category)
            }
        }

        barChartItemAdapter = com.amarant.apps.budgetapp.ui.adapter.BarChartItemAdapter()
        barChartItemAdapter.onBarChartItemClickListener = { item ->
            val action = StatsFragmentDirections.actionStatsFragmentToExpensesFragment(
                category = Category.ALL,
                isIncome = isIncome,
                startDate = item.startDate,
                endDate = item.endDate,
                title = item.label
            )
            findNavController().navigate(action)
        }
    }

    private fun setClickListeners() {
        binding.pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                initSelectedSection(e as PieEntry)
            }
            override fun onNothingSelected() {}
        })

        binding.barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                // Display the value and label for the selected bar
                val data = statsViewModel.barChartEntries.value ?: return
                val index = e.x.toInt()
                if (index >= 0 && index < data.labels.size) {
                    val label = data.labels[index]
                    val value = e.y
                    
//                    binding.tvCategory.text = label
                    binding.lblTotal.text = label
                    binding.imgIcon.visibility = View.GONE // Hide icon for bar selection
//                    binding.tvCategory.visibility = View.VISIBLE
//                    binding.lblTotal.visibility = View.VISIBLE
//                    binding.tvSum.visibility = View.VISIBLE
//                    binding.tvTotal.visibility = View.VISIBLE
                    binding.tvPercent.visibility = View.GONE // No percentage for bar selection
                    
                    if (isIncome) {
//                        binding.tvSum.text = getString(R.string.plus_placeholder, NumberUtils.formatNumberWithThousandsSeparator(value.toDouble()))
                        binding.tvTotal.text = getString(R.string.plus_placeholder, NumberUtils.formatNumberWithThousandsSeparator(value.toDouble()))
//                        binding.tvSum.setTextColor(ContextCompat.getColor(requireContext(), R.color.positive_green))
                        binding.tvTotal.setTextColor(ContextCompat.getColor(requireContext(), R.color.positive_green))
                    } else {
//                        binding.tvSum.text = NumberUtils.formatNumberWithThousandsSeparator(value.toDouble() * -1.0)
                        binding.tvTotal.text = NumberUtils.formatNumberWithThousandsSeparator(value.toDouble() * -1.0)
//                        binding.tvSum.setTextColor(ContextCompat.getColor(requireContext(), R.color.negative_red))
                        binding.tvTotal.setTextColor(ContextCompat.getColor(requireContext(), R.color.negative_red))
                    }
                }
            }

            override fun onNothingSelected() {
                // Return to showing nothing or the main total
                if (statsViewModel.chartType.value == ChartType.BAR) {
//                    binding.tvCategory.visibility = View.GONE
//                    binding.tvSum.visibility = View.GONE
                      binding.lblTotal.setText(R.string.total)
                        val formattedSum = NumberUtils.formatNumberWithThousandsSeparator(totalSum.toDouble())
                        if (totalSum > 0) {
                            binding.tvTotal.text = getString(R.string.plus_placeholder, formattedSum)
                            binding.tvTotal.setTextColor(ContextCompat.getColor(requireContext(), R.color.positive_green))
                        } else {
                            binding.tvTotal.text = formattedSum
                            binding.tvTotal.setTextColor(ContextCompat.getColor(requireContext(), R.color.negative_red))
                        }
                }
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
        val isPie = statsViewModel.chartType.value == ChartType.PIE
        binding.lblTotal.visibility = if (isEntriesEmpty) View.GONE else View.VISIBLE
        binding.tvTotal.visibility = if (isEntriesEmpty) View.GONE else View.VISIBLE
        binding.llNoEntries.visibility = if (isEntriesEmpty) View.VISIBLE else View.GONE
        
        // Categories list visibility - ALWAYS VISIBLE if there is data
        binding.lblCategoryExpenses.visibility = if (isEntriesEmpty) View.GONE else View.VISIBLE
        
        if (isPie) {
            binding.recyclerCategoryExpenses.adapter = categoryExpenseAdapter
            binding.lblCategoryExpenses.text = getString(R.string.category_expenses)
            binding.lblCategoryExpenses.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_expenses, 0, 0, 0)
        } else {
            binding.recyclerCategoryExpenses.adapter = barChartItemAdapter
            binding.lblCategoryExpenses.text = getString(R.string.period)
            binding.lblCategoryExpenses.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_period, 0, 0, 0)
        }
        binding.recyclerCategoryExpenses.visibility = if (isEntriesEmpty) View.GONE else View.VISIBLE

        binding.pieChart.visibility = if (isEntriesEmpty || !isPie) View.GONE else View.VISIBLE
        binding.barChart.visibility = if (isEntriesEmpty || isPie) View.GONE else View.VISIBLE
        
        // Pie-specific selection details
        binding.imgIcon.visibility = if (isEntriesEmpty || !isPie) View.GONE else View.VISIBLE
        binding.tvCategory.visibility = if (isEntriesEmpty || !isPie) View.GONE else View.VISIBLE
        binding.tvSum.visibility = if (isEntriesEmpty || !isPie) View.GONE else View.VISIBLE
        binding.tvPercent.visibility = if (isEntriesEmpty || !isPie) View.GONE else View.VISIBLE
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