package com.amarant.apps.budgetapp.ui.fragments.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import com.amarant.apps.budgetapp.databinding.BottomSheetCreateBudgetBinding
import com.amarant.apps.budgetapp.entities.Category
import com.amarant.apps.budgetapp.entities.CategoryBudget
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetPlanningViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateBudgetBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCreateBudgetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetPlanningViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCreateBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdowns()
        setupClickListeners()
    }

    private fun setupDropdowns() {
        val categories = Category.entries.filter { it != Category.ALL }.map { it.dbName }
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.actCategory.setAdapter(categoryAdapter)

        val periods = listOf("Monthly", "Weekly")
        val periodAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, periods)
        binding.actPeriod.setAdapter(periodAdapter)
    }

    private fun setupClickListeners() {
        binding.btnCreate.setOnClickListener {
            val categoryName = binding.actCategory.text.toString()
            val limit = binding.etLimit.text.toString().toDoubleOrNull()
            val period = binding.actPeriod.text.toString()

            val category = Category.entries.find { it.dbName == categoryName }

            if (category != null && limit != null && period.isNotEmpty()) {
                val budget = CategoryBudget(
                    category = category,
                    amountLimit = limit,
                    period = period,
                    startDate = System.currentTimeMillis()
                )
                viewModel.insertBudget(budget)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CreateBudgetBottomSheet"
    }
}
