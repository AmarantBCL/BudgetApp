package com.amarant.apps.budgetapp.ui.fragments.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.amarant.apps.budgetapp.databinding.BottomSheetCategoriesBinding
import com.amarant.apps.budgetapp.entities.Category
import com.amarant.apps.budgetapp.entities.QuickCategoryItem
import com.amarant.apps.budgetapp.ui.adapter.QuickCategoriesAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoriesBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCategoriesBinding? = null
    private val binding: BottomSheetCategoriesBinding
        get() = _binding ?: throw RuntimeException("BottomSheetCategoriesBinding == null")

    private lateinit var adapter: QuickCategoriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        adapter = QuickCategoriesAdapter()
        binding.recyclerCategories.adapter = adapter
        
        val categories = Category.entries.filter { it != Category.ALL }.map { 
            QuickCategoryItem(it, isSelected = false) 
        }
        adapter.submitList(categories)

        adapter.onCategoryClickListener = { category ->
            setFragmentResult(REQUEST_KEY, bundleOf(BUNDLE_KEY_CATEGORY to category.ordinal))
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CategoriesBottomSheet"
        const val REQUEST_KEY = "category_request_key"
        const val BUNDLE_KEY_CATEGORY = "bundle_key_category"

        fun newInstance() = CategoriesBottomSheetFragment()
    }
}
