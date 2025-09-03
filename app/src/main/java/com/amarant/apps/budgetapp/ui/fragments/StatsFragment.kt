package com.amarant.apps.budgetapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentStatsBinding
import com.amarant.apps.budgetapp.entities.BudgetUI
import com.amarant.apps.budgetapp.entities.Category
import com.amarant.apps.budgetapp.ui.adapter.CategoryExpenseAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.util.NumberUtils
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

    private val budgetViewModel: BudgetViewModel by activityViewModels()

    private lateinit var categoryExpenseAdapter: CategoryExpenseAdapter

    private var totalSum = 0

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
        budgetViewModel.getBudgetEntriesBetweenDates().observe(viewLifecycleOwner) { reports ->
            totalSum = getTotalSum(reports)
            binding.tvTotal.text = NumberUtils.formatNumberWithThousandsSeparator(totalSum.toDouble())
            initDataSet(prepareDataSet(reports))
        }
        budgetViewModel.testCategoryData.observe(viewLifecycleOwner) {
            categoryExpenseAdapter.submitList(it)
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
    }

    private fun initVisibility(isEntriesEmpty: Boolean) {
        binding.lblTotal.visibility = if (isEntriesEmpty) View.GONE else View.VISIBLE
        binding.tvTotal.visibility = if (isEntriesEmpty) View.GONE else View.VISIBLE
        binding.llNoEntries.visibility = if (isEntriesEmpty) View.VISIBLE else View.GONE
        binding.lblCategoryExpenses.visibility = if (isEntriesEmpty) View.GONE else View.VISIBLE
        binding.recyclerCategoryExpenses.visibility = if (isEntriesEmpty) View.GONE else View.VISIBLE
    }

    private fun getTotalSum(reports: List<BudgetUI>): Int {
        return reports.filter { it.budget.creditOrDebit == "Debit" }
            .sumOf { it.budget.amount.toInt() }
    }

    private fun prepareDataSet(reports: List<BudgetUI>): List<PieEntry> {
        val list = mutableListOf<PieEntry>()
        reports.filter { it.budget.creditOrDebit == "Debit" }
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
        binding.tvSum.text = NumberUtils.formatNumberWithThousandsSeparator(value * -1.0)
        binding.tvPercent.text = getString(R.string.percent_placeholder, percent)
    }
}