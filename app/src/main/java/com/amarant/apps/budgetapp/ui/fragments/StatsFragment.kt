package com.amarant.apps.budgetapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentStatsBinding
import com.amarant.apps.budgetapp.entities.Category
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.util.NumberUtils
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlin.math.absoluteValue

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding: FragmentStatsBinding
        get() = _binding ?: throw RuntimeException("FragmentStatsBinding == null")

    private val budgetViewModel: BudgetViewModel by activityViewModels()

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
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initPieChart() {
        val pieChart = binding.pieChart
        val font = ResourcesCompat.getFont(requireContext(), R.font.rubik)
        pieChart.setEntryLabelTypeface(font)
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.isRotationEnabled = false
        pieChart.description.isEnabled = false
        pieChart.holeRadius = 55f
        pieChart.animateY(300)
        pieChart.setDrawEntryLabels(false)

        val legend = pieChart.legend
        legend.isEnabled = false
    }

    private fun observeViewModel() {
        budgetViewModel.getBudgetEntriesBetweenDates().observe(viewLifecycleOwner) { reports ->
            totalSum = reports.filter { it.budget.creditOrDebit == "Debit" }
                .sumOf { it.budget.amount.toInt() }
            binding.tvTotal.text = NumberUtils.formatNumberWithThousandsSeparator(totalSum.toDouble())
            val list = mutableListOf<PieEntry>()
            val categoryArr = resources.getStringArray(R.array.categories)
            reports.filter { it.budget.creditOrDebit == "Debit" }
                .groupBy { it.budget.category }
                .forEach { (category, items) ->
                    list.add(PieEntry(
                        items.sumOf { it.budget.amount.toInt() }.toFloat().absoluteValue,
//                        categoryArr[category.ordinal]
                        category
                    ))
            }
            initDataSet(list.toList())
        }
    }

    private fun initDataSet(entries: List<PieEntry>) {
        val font = ResourcesCompat.getFont(requireContext(), R.font.rubik)
//        val entries = listOf(
//            PieEntry(11225f, "Shopping"),
//            PieEntry(5234f, "Home"),
//            PieEntry(3210f, "Transportation")
//        )
        val dataSet = PieDataSet(entries, "")
        dataSet.valueTextSize = 16f
        dataSet.valueTypeface = font
        dataSet.setDrawValues(true)
        dataSet.sliceSpace = 2f
        dataSet.setDrawValues(false)
        val categoryArr = resources.getStringArray(R.array.categories)
        val categoryColors = mapOf(
            Category.GROCERIES to ContextCompat.getColor(requireContext(), R.color.accent_purple),
            Category.RESTAURANTS to ContextCompat.getColor(requireContext(), R.color.orange),
            Category.TRANSFERS to ContextCompat.getColor(requireContext(), R.color.state_gray),
            Category.INCOME to ContextCompat.getColor(requireContext(), R.color.positive_green),
            Category.UTILITIES to ContextCompat.getColor(requireContext(), R.color.amber),
            Category.CLOTHING to ContextCompat.getColor(requireContext(), R.color.violet),
            Category.HOME to ContextCompat.getColor(requireContext(), R.color.forest_green),
            Category.TRANSPORTATION to ContextCompat.getColor(requireContext(), R.color.blue),
            Category.BEAUTY to ContextCompat.getColor(requireContext(), R.color.negative_red),
            Category.HEALTH to ContextCompat.getColor(requireContext(), R.color.red),
            Category.PETS to ContextCompat.getColor(requireContext(), R.color.amber),
            Category.SUBSCRIPTIONS to ContextCompat.getColor(requireContext(), R.color.cyan),
            Category.ENTERTAINMENT to ContextCompat.getColor(requireContext(), R.color.pink),
            Category.EDUCATION to ContextCompat.getColor(requireContext(), R.color.deep_blue),
            Category.TRAVELING to ContextCompat.getColor(requireContext(), R.color.sky),
            Category.GIFTS to ContextCompat.getColor(requireContext(), R.color.rose),
            Category.CHARITY to ContextCompat.getColor(requireContext(), R.color.teal),
            Category.TAXES to ContextCompat.getColor(requireContext(), R.color.state_gray),
            Category.RENT to ContextCompat.getColor(requireContext(), R.color.brown),
            Category.CHILDREN to ContextCompat.getColor(requireContext(), R.color.body),
            Category.SPORTS to ContextCompat.getColor(requireContext(), R.color.orange),
            Category.MUSIC to ContextCompat.getColor(requireContext(), R.color.indigo),
            Category.APPLIANCES to ContextCompat.getColor(requireContext(), R.color.state_gray)
        )
        dataSet.colors = categoryColors.map { it.value }

        val data = PieData(dataSet)
//        data.setValueFormatter(PercentFormatter(pieChart))
//        data.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.primary_white))
        binding.pieChart.data = data
//        binding.pieChart.invalidate()
        binding.pieChart.highlightValue(0f, 0)
        binding.imgIcon.setImageResource((entries[0].data as Category).iconRes)
        binding.tvCategory.text = (entries[0].data as Category).getLocalizedName(requireContext()) //entries[0].label.toString()
        binding.tvSum.text = NumberUtils.formatNumberWithThousandsSeparator(entries[0].value * -1.0)
        val percent = NumberUtils.formatDecimal((entries[0].value / totalSum.absoluteValue) * 100.0)
        binding.tvPercent.text = getString(R.string.percent_placeholder, percent)

        binding.pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                val value = NumberUtils.formatNumberWithThousandsSeparator(e.y * -1.0)
                val percent = NumberUtils.formatDecimal((e.y / totalSum.absoluteValue) * 100.0)
                binding.imgIcon.setImageResource(((e as PieEntry).data as Category).iconRes)
                binding.tvCategory.text = ((e as PieEntry).data as Category).getLocalizedName(requireContext())//(e as PieEntry).label
                binding.tvSum.text = value
                binding.tvPercent.text = getString(R.string.percent_placeholder, percent)
            }

            override fun onNothingSelected() {
                // Вызывается, когда ничего не выбрано
            }
        })
    }
}