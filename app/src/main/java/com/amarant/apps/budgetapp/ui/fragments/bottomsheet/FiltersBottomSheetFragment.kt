package com.amarant.apps.budgetapp.ui.fragments.bottomsheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FiltersBottomSheetBinding
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.util.CategoryUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FiltersBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FiltersBottomSheetBinding? = null
    private val binding: FiltersBottomSheetBinding
        get() = _binding ?: throw RuntimeException("FiltersBottomSheetBinding == null")

    private val budgetViewModel: BudgetViewModel by activityViewModels()

    private val chipMap = mutableMapOf<Chip, Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FiltersBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setChips()
        observeViewModel()
        setClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setChips() {
        for ((index, category) in CategoryUtils.ALL_CATEGORIES.withIndex()) {
            val chip = Chip(requireContext())
            chip.text = resources.getStringArray(R.array.categories)[index]
            val resId = resources.getIdentifier(
                "drawable/cat_${category.lowercase()}",
                "drawable",
                requireContext().packageName
            )
            chip.chipIcon = ContextCompat.getDrawable(requireContext(), resId)
            chip.isChipIconVisible = true
            binding.filtersChipGroup.addView(chip)
            chipMap[chip] = index
        }
    }

    private fun observeViewModel() {
        budgetViewModel.appliedFilter.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                for (chip in chipMap.keys) {
                    if (chip.text == it) {
                        chip.isChecked = true
                        break
                    }
                }
            } else {
                binding.chipNone.isChecked = true
            }
        }
    }

    private fun setClickListeners() {
        binding.applyFilters.setOnClickListener {
            val checkedId = binding.filtersChipGroup.checkedChipId
            val chip = requireView().findViewById<Chip>(checkedId)
            val category = if (chip == binding.chipNone) {
                ""
            } else {
                CategoryUtils.ALL_CATEGORIES[chipMap[chip] ?: 0]
            }
            budgetViewModel.applyFilter(category)
            dismiss()
        }
    }

    companion object {

        const val TAG = "FiltersBottomSheet"

        fun newInstance(): FiltersBottomSheetFragment {
            return FiltersBottomSheetFragment()
        }
    }
}