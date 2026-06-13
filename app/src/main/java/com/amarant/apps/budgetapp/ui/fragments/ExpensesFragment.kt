package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentExpensesBinding
import com.amarant.apps.budgetapp.entities.Category
import com.amarant.apps.budgetapp.entities.ReportType
import com.amarant.apps.budgetapp.entities.ReportsItem
import com.amarant.apps.budgetapp.ui.MainActivity
import com.amarant.apps.budgetapp.ui.adapter.ReportsAdapter
import com.amarant.apps.budgetapp.ui.adapter.decorations.CustomDividerDecoration
import com.amarant.apps.budgetapp.ui.viewmodels.StatsViewModel
import com.amarant.apps.budgetapp.util.DateUtils
import com.amarant.apps.budgetapp.util.NumberUtils

class ExpensesFragment : Fragment() {

    private val args by navArgs<ExpensesFragmentArgs>()

    private var _binding: FragmentExpensesBinding? = null
    private val binding: FragmentExpensesBinding
        get() = _binding ?: throw RuntimeException("FragmentExpensesBinding == null")

    private val statsViewModel: StatsViewModel by activityViewModels()

    private lateinit var budgetAdapter: ReportsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initActionBar()
        initViews()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initActionBar() {
        val activity = (requireActivity() as MainActivity)
        val title = if (args.isIncome) getString(R.string.income_s) else getString(R.string.expenses)
        activity.setActionBarTitle(title)
    }

    private fun initViews() {
        budgetAdapter = ReportsAdapter()
        val customDivider = ContextCompat.getDrawable(requireContext(), R.drawable.divider)
        if (customDivider != null) {
            binding.recyclerExpenses.addItemDecoration(CustomDividerDecoration(customDivider))
        } else {
            val defaultDivider = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            binding.recyclerExpenses.addItemDecoration(defaultDivider)
        }
        budgetAdapter.onItemClickListener = {
            statsViewModel.toggleBudgetSelection(it.budget.id ?: -1)
        }
        binding.recyclerExpenses.adapter = budgetAdapter
    }

    private fun observeViewModel() {
        val startDate = args.startDate
        val endDate = args.endDate
        val isFilteredByDate = startDate != 0L && endDate != 0L

        if (isFilteredByDate) {
            statsViewModel.getBudgetEntriesBetweenDates(startDate, endDate, args.category).observe(viewLifecycleOwner) { budgets ->
                val type = if (args.isIncome) ReportType.INCOME else ReportType.EXPENSE
                
                val filtered = budgets.filter { 
                    if (type == ReportType.INCOME) it.budget.amount > 0 else it.budget.amount < 0
                }

                val groupedList = mutableListOf<ReportsItem>()
                filtered.groupBy { it.budget.date }.forEach { (date, items) ->
                    groupedList.add(ReportsItem.DateHeader(DateUtils.getFormattedDate(date.toLong())))
                    items.reversed().forEach { budgetUI ->
                        groupedList.add(ReportsItem.Entry(budgetUI))
                    }
                }
                budgetAdapter.submitList(groupedList)
                
                val totalSum = filtered.filter { !it.isHidden }.sumOf { it.budget.amount.toDouble() }
                val formattedSum = NumberUtils.formatNumberWithThousandsSeparator(totalSum)
                
                val resId = if (args.isIncome) Category.INCOME.rawIconRes else Category.OTHER.rawIconRes
                binding.imgCategory.setImageResource(resId)
                if (totalSum != 0.0) {
                    binding.tvAmount.text = if (args.isIncome) getString(R.string.plus_placeholder, formattedSum) else formattedSum
                    binding.tvAmount.setTextColor(ContextCompat.getColor(requireContext(), if (args.isIncome) R.color.positive_green else R.color.negative_red))
                } else {
                    binding.tvAmount.text = formattedSum
                    binding.tvAmount.setTextColor(ContextCompat.getColor(requireContext(), R.color.negative_red))
                }
                
                initActionBar(args.title ?: args.category.getLocalizedName(requireContext()))
                setHiddenViews(filtered.any { it.isHidden })
            }
        } else {
            statsViewModel.getExpensesByCategory(args.category).observe(viewLifecycleOwner) { reports ->
                val totalSum = reports.filterIsInstance<ReportsItem.Entry>()
                    .filter { !it.entry.isHidden }
                    .sumOf { it.entry.budget.amount.toInt() }.toFloat()
                val formattedSum = NumberUtils.formatNumberWithThousandsSeparator(totalSum.toDouble())
                budgetAdapter.submitList(reports)
                val resId = args.category.rawIconRes
                binding.imgCategory.setImageResource(resId)
                if (totalSum > 0) {
                    binding.tvAmount.text = getString(R.string.plus_placeholder, formattedSum)
                    binding.tvAmount.setTextColor(ContextCompat.getColor(requireContext(), R.color.positive_green))
                } else {
                    binding.tvAmount.text = formattedSum
                    binding.tvAmount.setTextColor(ContextCompat.getColor(requireContext(), R.color.negative_red))
                }
                setHiddenViews(reports.filterIsInstance<ReportsItem.Entry>().any { it.entry.isHidden })
            }
        }
    }

    private fun initActionBar(title: String? = null) {
        (activity as MainActivity).setActionBarTitle(title ?: args.category.getLocalizedName(requireContext()))
    }

    private fun setHiddenViews(isHidden: Boolean) {
        val spanIncome = SpannableString("${getString(R.string.total_in_category, args.category.getLocalizedName(requireContext()))} ⬤")
        spanIncome.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.accent_purple)),
            spanIncome.length - 1, spanIncome.length, SPAN_EXCLUSIVE_EXCLUSIVE
        )
        if (isHidden) {
            binding.lblCategory.text = spanIncome
        } else {
            binding.lblCategory.text = getString(R.string.total_in_category, args.category.getLocalizedName(requireContext()))
        }
    }
}