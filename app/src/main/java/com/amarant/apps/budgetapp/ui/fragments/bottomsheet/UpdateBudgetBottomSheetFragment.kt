package com.amarant.apps.budgetapp.ui.fragments.bottomsheet

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.UpdateBudgetBottomSheetBinding
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.util.CategoryUtils.ALL_CATEGORIES
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdateBudgetBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: UpdateBudgetBottomSheetBinding? = null
    private val binding: UpdateBudgetBottomSheetBinding
        get() = _binding ?: throw RuntimeException("UpdateBudgetBottomSheetBinding == null")

    private lateinit var currentBudgetItem: Budget

    private val budgetViewModel: BudgetViewModel by activityViewModels()

    private val chipMap = mutableMapOf<Chip, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentBudgetItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable(KEY_BUDGET_ITEM, Budget::class.java)
                ?: throw RuntimeException("No arguments passed to UpdateBudgetBottomSheetFragment")
        } else {
            requireArguments().getParcelable(KEY_BUDGET_ITEM)
                ?: throw RuntimeException("No arguments passed to UpdateBudgetBottomSheetFragment")
        }
    }

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
            val category = ALL_CATEGORIES[chipMap[chip] ?: 0]
            budgetViewModel.updateBudget(
                "Debit", // TODO Hardcoded and incorrect, should be deleted soon
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
        for ((index, category) in ALL_CATEGORIES.withIndex()) {
            val chip = Chip(requireContext())
            chip.text = resources.getStringArray(R.array.categories)[index]
            val resId = resources.getIdentifier(
                "drawable/cat_${category.lowercase()}",
                "drawable",
                requireContext().packageName
            )
            chip.chipIcon = ContextCompat.getDrawable(requireContext(), resId)
            chip.isChipIconVisible = true
            binding.chipGroup.addView(chip)
            if (category == ALL_CATEGORIES[0]) {
                chip.isChecked = true
            }
            chipMap[chip] = index
        }
    }

    companion object {

        private const val KEY_BUDGET_ITEM = "budget_item"

        const val TAG = "UpdateBudgetBottomSheet"

        fun newInstance(budgetItem: Budget): UpdateBudgetBottomSheetFragment {
            return UpdateBudgetBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_BUDGET_ITEM, budgetItem)
                }
            }
        }
    }
}