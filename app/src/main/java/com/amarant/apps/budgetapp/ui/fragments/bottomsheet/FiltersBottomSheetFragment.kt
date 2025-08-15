package com.amarant.apps.budgetapp.ui.fragments.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.amarant.apps.budgetapp.databinding.FiltersBottomSheetBinding
import com.amarant.apps.budgetapp.entities.Category
import com.amarant.apps.budgetapp.entities.QuickCategoryItem
import com.amarant.apps.budgetapp.ui.adapter.QuickCategoriesAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FiltersBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FiltersBottomSheetBinding? = null
    private val binding: FiltersBottomSheetBinding
        get() = _binding ?: throw RuntimeException("FiltersBottomSheetBinding == null")

    private val budgetViewModel: BudgetViewModel by activityViewModels()

    private lateinit var quickCategoriesAdapter: QuickCategoriesAdapter

    private var selectedCategory = Category.ALL

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
        quickCategoriesAdapter.onCategoryClickListener = { category ->
            if (selectedCategory != category) {
                val items = mutableListOf<QuickCategoryItem>()
                selectedCategory = category
                for (categoryEntry in Category.entries) {
                    val item = QuickCategoryItem(
                        categoryEntry,
//                        categoryEntry.dbName,
//                        categoryEntry.iconRes,
                        selectedCategory == categoryEntry
                    )
                    items.add(item)
                }
                binding.tvSelectedCategory.text = category.getLocalizedName(requireContext())//name
                quickCategoriesAdapter.submitList(items)
            }
        }
    }

    private fun observeViewModel() {
        budgetViewModel.appliedFilter.observe(viewLifecycleOwner) { filter ->
            if (filter != Category.ALL) {
                val items = mutableListOf<QuickCategoryItem>()
                selectedCategory = filter
                for (categoryEntry in Category.entries) {
                    val item = QuickCategoryItem(
                        categoryEntry,
//                        categoryEntry.dbName,
//                        categoryEntry.iconRes,
                        selectedCategory == categoryEntry
                    )
                    items.add(item)
                }
                binding.tvSelectedCategory.text = selectedCategory.getLocalizedName(requireContext())//selectedCategory
                quickCategoriesAdapter.submitList(items)
            } else {
                selectedCategory = Category.ALL
                val items = mutableListOf<QuickCategoryItem>()
                for (categoryEntry in Category.entries) {
                    val item = QuickCategoryItem(
                        categoryEntry,
//                        categoryEntry.dbName,
//                        categoryEntry.iconRes,
                        selectedCategory == categoryEntry
                    )
                    items.add(item)
                }
                binding.tvSelectedCategory.text = Category.ALL.getLocalizedName(requireContext())//"All"
                quickCategoriesAdapter.submitList(items)
            }
        }
    }

    private fun setClickListeners() {
        binding.btnApplyFilters.setOnClickListener {
            budgetViewModel.applyFilter(selectedCategory)
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