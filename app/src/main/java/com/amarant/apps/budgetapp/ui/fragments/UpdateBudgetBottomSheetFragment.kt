package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.UpdateBudgetBottomSheetBinding
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
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
        binding.updateAmount.setText(currentBudgetItem.amount.toString())
        binding.updatePurpose.setText(currentBudgetItem.purpose)
        binding.updateBudgetEntry.setOnClickListener {
            val updatedAmount = binding.updateAmount.text.toString().trim()
            val updatedPurpose = binding.updatePurpose.text.toString().trim()
            budgetViewModel.updateBudget(
                updatedAmount.toFloat(),
                updatedPurpose,
                currentBudgetItem.id!!
            )
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}