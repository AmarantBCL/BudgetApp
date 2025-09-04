package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentExpensesBinding
import com.amarant.apps.budgetapp.entities.Category
import com.amarant.apps.budgetapp.entities.ReportsItem
import com.amarant.apps.budgetapp.ui.adapter.ReportsAdapter
import com.amarant.apps.budgetapp.ui.adapter.TodayBudgetAdapter
import com.amarant.apps.budgetapp.ui.adapter.decorations.CustomDividerDecoration
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.util.NumberUtils

class ExpensesFragment : Fragment() {

    private val args by navArgs<ExpensesFragmentArgs>()

    private var _binding: FragmentExpensesBinding? = null
    private val binding: FragmentExpensesBinding
        get() = _binding ?: throw RuntimeException("FragmentExpensesBinding == null")

    private val budgetViewModel: BudgetViewModel by activityViewModels()

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
        initViews()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        binding.recyclerExpenses.adapter = budgetAdapter
    }

    private fun observeViewModel() {
        budgetViewModel.getExpensesByCategory(args.category).observe(viewLifecycleOwner) { reports ->
            val totalSum = reports.filterIsInstance<ReportsItem.Entry>()
                .sumOf { it.entry.budget.amount.toInt() }.toFloat().toString()
            budgetAdapter.submitList(reports)
            val resId = args.category.rawIconRes
            binding.imgCategory.setImageResource(resId)
            binding.lblCategory.text = getString(R.string.total_in_category, args.category.getLocalizedName(requireContext()))
            binding.tvAmount.text = NumberUtils.formatNumberWithThousandsSeparator(totalSum.toDouble())
        }
    }
}