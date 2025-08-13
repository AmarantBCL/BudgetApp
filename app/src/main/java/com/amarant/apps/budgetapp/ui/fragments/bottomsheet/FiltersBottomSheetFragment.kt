package com.amarant.apps.budgetapp.ui.fragments.bottomsheet

import android.app.Dialog
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
import com.amarant.apps.budgetapp.entities.QuickCategoryItem
import com.amarant.apps.budgetapp.ui.adapter.QuickCategoriesAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.util.CategoryUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FiltersBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FiltersBottomSheetBinding? = null
    private val binding: FiltersBottomSheetBinding
        get() = _binding ?: throw RuntimeException("FiltersBottomSheetBinding == null")

    private val budgetViewModel: BudgetViewModel by activityViewModels()

    private lateinit var quickCategoriesAdapter: QuickCategoriesAdapter

//    private val chipMap = mutableMapOf<Chip, Int>()

    private var selectedCategory = ""

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
//        setChips()
        initRecyclerView()
        observeViewModel()
        setClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme)
    }

    private fun initRecyclerView() {
        quickCategoriesAdapter = QuickCategoriesAdapter()
        binding.recyclerCategories.adapter = quickCategoriesAdapter
        quickCategoriesAdapter.onCategoryClickListener = { name ->
            Log.d("WTF", "Click: $selectedCategory != $name")
            if (selectedCategory != name) {
                val items = mutableListOf<QuickCategoryItem>()
                items.add(
                    QuickCategoryItem(
                    "All", R.drawable.circle_all, name == "All"
                )
                )
                selectedCategory = name
                for (category in CategoryUtils.ALL_CATEGORIES) {
                    val item = QuickCategoryItem(
                        category,
                        getCategoryDrawable(category),
                        selectedCategory == category
                    )
                    items.add(item)
                }
                binding.tvSelectedCategory.text = name
                quickCategoriesAdapter.submitList(items)
            }
        }
    }

//    private fun setChips() {
//        for ((index, category) in CategoryUtils.ALL_CATEGORIES.withIndex()) {
//            val chip = Chip(requireContext())
//            chip.text = resources.getStringArray(R.array.categories)[index]
//            val resId = resources.getIdentifier(
//                "drawable/cat_${category.lowercase()}",
//                "drawable",
//                requireContext().packageName
//            )
//            chip.chipIcon = ContextCompat.getDrawable(requireContext(), resId)
//            chip.isChipIconVisible = true
//            binding.filtersChipGroup.addView(chip)
//            chipMap[chip] = index
//        }
//    }

    private fun getCategoryDrawable(categoryName: String): Int {
        return when(categoryName) {
            "Car" -> R.drawable.circle_transportation
            "Restaurants" -> R.drawable.circle_cafe
            "Groceries" -> R.drawable.circle_shopping
            "Rent" -> R.drawable.circle_housing
            "Health" -> R.drawable.circle_health
            "Entertainment" -> R.drawable.circle_entertainment
            "Cash" -> R.drawable.circle_transfer
            "Taxes" -> R.drawable.circle_taxes
            "Clothes" -> R.drawable.circle_clothing
            "Pets" -> R.drawable.circle_pets
            "Education" -> R.drawable.circle_education
            "Gifts" -> R.drawable.circle_gifts
            "Charity" -> R.drawable.circle_charity
            "Traveling" -> R.drawable.circle_traveling
            "Beauty" -> R.drawable.circle_personal_care
            "Utilities" -> R.drawable.circle_utilities
            "Taxi" -> R.drawable.circle_subscriptions
            "House" -> R.drawable.circle_housing
            else -> R.drawable.circle_all
        }
    }

    private fun observeViewModel() {
        budgetViewModel.appliedFilter.observe(viewLifecycleOwner) {
            if (it.isNotEmpty() && it != "All") {
//                for (chip in chipMap.keys) {
//                    if (chip.text == it) {
//                        chip.isChecked = true
//                        break
//                    }
//                }
                val items = mutableListOf<QuickCategoryItem>()
                items.add(QuickCategoryItem(
                    "All", R.drawable.circle_all, false
                ))
                selectedCategory = it
                for (category in CategoryUtils.ALL_CATEGORIES) {
                    val item = QuickCategoryItem(
                        category,
                        getCategoryDrawable(category),
                        selectedCategory == category
                    )
                    items.add(item)
                }
                quickCategoriesAdapter.submitList(items)
            } else {
//                binding.chipNone.isChecked = true
                selectedCategory = ""
                val items = mutableListOf<QuickCategoryItem>()
                items.add(QuickCategoryItem(
                    "All", R.drawable.circle_all, true
                ))
                for (category in CategoryUtils.ALL_CATEGORIES) {
                    val item = QuickCategoryItem(category, getCategoryDrawable(category), selectedCategory == category)
                    items.add(item)
                }
                quickCategoriesAdapter.submitList(items)
            }
            binding.tvSelectedCategory.text = selectedCategory
        }
    }

    private fun setClickListeners() {
        binding.btnApplyFilters.setOnClickListener {
//            val checkedId = binding.filtersChipGroup.checkedChipId
//            val chip = requireView().findViewById<Chip>(checkedId)
//            val category = if (chip == binding.chipNone) {
//                ""
//            } else {
//                CategoryUtils.ALL_CATEGORIES[chipMap[chip] ?: 0]
//            }
            if (selectedCategory == "All") {
                budgetViewModel.applyFilter("")
            } else {
                budgetViewModel.applyFilter(selectedCategory)
            }
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