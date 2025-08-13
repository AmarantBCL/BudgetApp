package com.amarant.apps.budgetapp.ui.fragments.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FiltersBottomSheetBinding
import com.amarant.apps.budgetapp.entities.QuickCategoryItem
import com.amarant.apps.budgetapp.ui.adapter.QuickCategoriesAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.util.CategoryUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FiltersBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FiltersBottomSheetBinding? = null
    private val binding: FiltersBottomSheetBinding
        get() = _binding ?: throw RuntimeException("FiltersBottomSheetBinding == null")

    private val budgetViewModel: BudgetViewModel by activityViewModels()

    private lateinit var quickCategoriesAdapter: QuickCategoriesAdapter

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
        initRecyclerView()
        observeViewModel()
        setClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initRecyclerView() {
        quickCategoriesAdapter = QuickCategoriesAdapter()
        binding.recyclerCategories.adapter = quickCategoriesAdapter
        quickCategoriesAdapter.onCategoryClickListener = { name ->
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
                binding.tvSelectedCategory.text = selectedCategory
                quickCategoriesAdapter.submitList(items)
            } else {
                selectedCategory = ""
                val items = mutableListOf<QuickCategoryItem>()
                items.add(QuickCategoryItem(
                    "All", R.drawable.circle_all, true
                ))
                for (category in CategoryUtils.ALL_CATEGORIES) {
                    val item = QuickCategoryItem(category, getCategoryDrawable(category), selectedCategory == category)
                    items.add(item)
                }
                binding.tvSelectedCategory.text = "All"
                quickCategoriesAdapter.submitList(items)
            }
        }
    }

    private fun setClickListeners() {
        binding.btnApplyFilters.setOnClickListener {
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