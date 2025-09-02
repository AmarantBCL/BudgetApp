package com.amarant.apps.budgetapp.ui.fragments.bottomsheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.BottomSheetSortingBinding
import com.amarant.apps.budgetapp.entities.SortField
import com.amarant.apps.budgetapp.entities.SortOption
import com.amarant.apps.budgetapp.entities.SortOrder
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

@Deprecated("Sorting was rejected in favor of type")
class SortingBottomSheetFragment: BottomSheetDialogFragment() {

    private var _binding: BottomSheetSortingBinding? = null
    private val binding: BottomSheetSortingBinding
        get() = _binding ?: throw RuntimeException("BottomSheetSortingBinding == null")

    private val budgetViewModel: BudgetViewModel by activityViewModels()

    private var sort = SortField.DATE
    private var order = SortOrder.DESC

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSortingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        budgetViewModel.sorting.observe(viewLifecycleOwner) {
            when(it.field) {
                SortField.DATE -> binding.radioDate.isChecked = true
                SortField.AMOUNT -> binding.radioAmount.isChecked = true
                SortField.CATEGORY -> binding.radioCategory.isChecked = true
                SortField.TYPE -> binding.radioType.isChecked = true
            }
            binding.cboxAscDesc.isChecked = it.order == SortOrder.DESC
            initSelectedSort(it.field)
            initSelectedOrder(it.order)
            sort = it.field
            order = it.order
        }
    }

    private fun setClickListeners() {
        binding.radioGroupSortType.setOnCheckedChangeListener { radioGroup, i ->
            sort = when(i) {
                R.id.radio_date -> SortField.DATE
                R.id.radio_amount -> SortField.AMOUNT
                R.id.radio_category -> SortField.CATEGORY
                R.id.radio_type -> SortField.TYPE
                else -> SortField.DATE
            }
            initSelectedSort(sort)
        }
        binding.cboxAscDesc.setOnClickListener {
            order = if (order == SortOrder.DESC) SortOrder.ASC else SortOrder.DESC
            initSelectedOrder(order)
        }
        binding.btnApplyFilters.setOnClickListener {
            budgetViewModel.setSort(sort, order)
            dialog?.dismiss()
        }
    }

    private fun initSelectedSort(sort: SortField) {
        val sortArr = resources.getStringArray(R.array.sorting)
        binding.tvSortBy.text = sortArr[sort.ordinal]
    }

    private fun initSelectedOrder(order: SortOrder) {
        val iconRes = if (order == SortOrder.DESC) R.drawable.ic_desc else R.drawable.ic_asc
        val drawable = ContextCompat.getDrawable(requireContext(), iconRes)
        binding.tvSortBy.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
    }

    companion object {

        const val TAG = "SortingBottomSheet"

        fun newInstance(): SortingBottomSheetFragment {
            return SortingBottomSheetFragment()
        }
    }
}