package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.amarant.apps.budgetapp.databinding.UpdateBudgetBottomSheetBinding
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.util.CategoryUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdateBudgetBottomSheetFragment(
    val currentBudgetItem: Budget
) : BottomSheetDialogFragment() {

    private var _binding: UpdateBudgetBottomSheetBinding? = null
    private val binding: UpdateBudgetBottomSheetBinding
        get() = _binding ?: throw RuntimeException("UpdateBudgetBottomSheetBinding == null")

    val budgetViewModel: BudgetViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UpdateBudgetBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setChips()
        binding.updateAmount.setText(currentBudgetItem.amount.toString())
        binding.updatePurpose.setText(currentBudgetItem.purpose)
        binding.updateBudgetEntry.setOnClickListener {
            val updatedAmount = binding.updateAmount.text.toString().trim()
            val updatedPurpose = binding.updatePurpose.text.toString().trim()
            val checkedId = binding.chipGroup.checkedChipId
            val chip = requireView().findViewById<Chip>(checkedId)
            val category = chip.text.toString()
            budgetViewModel.updateBudget(
                updatedAmount.toFloat(),
                updatedPurpose,
                category,
                currentBudgetItem.id!!
            )
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setChips() {
        for (category in CategoryUtils.ALL_CATEGORIES) {
            val chip = Chip(requireContext())
            chip.text = category
            val resId = resources.getIdentifier("drawable/cat_${category.lowercase()}", "drawable", requireContext().packageName)
            chip.chipIcon = ContextCompat.getDrawable(requireContext(), resId)
            chip.isChipIconVisible = true
            binding.chipGroup.addView(chip)
            if (category == "Groceries") {
                chip.isChecked = true
            }
        }
    }
}