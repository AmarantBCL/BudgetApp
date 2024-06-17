package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.StatisticsBottomSheetBinding
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticsBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: StatisticsBottomSheetBinding? = null
    private val binding: StatisticsBottomSheetBinding
        get() = _binding ?: throw RuntimeException("StatisticsBottomSheetBinding == null")

    private val budgetViewModel: BudgetViewModel by viewModels()

    private lateinit var period: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        period = requireArguments().getString(KEY_PERIOD) ?: "Show All"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = StatisticsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        budgetViewModel.calculateTotalCredit(period).observe(viewLifecycleOwner) {
            if (it == null) {
                binding.totalCredit.text = "0.0"
            } else {
                binding.totalCredit.text = it.toString()
            }
        }
        budgetViewModel.calculateTotalSpending(period).observe(viewLifecycleOwner) {
            if (it == null) {
                binding.totalSpending.text = "0.0"
            } else {
                binding.totalSpending.text = (-1 * it).toString()
            }
        }
    }

    companion object {

        private const val KEY_PERIOD = "period"

        fun newInstance(period: String): StatisticsBottomSheetFragment {
            return StatisticsBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_PERIOD, period)
                }
            }
        }
    }
}