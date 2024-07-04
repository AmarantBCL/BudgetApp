package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.amarant.apps.budgetapp.databinding.StatisticsBottomSheetBinding
import com.amarant.apps.budgetapp.ui.adapter.DetailsAdapter
import com.amarant.apps.budgetapp.entities.BudgetCategoryDetails
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

    private lateinit var detailsAdapter: DetailsAdapter

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
        initRecyclerView()
        observeViewModel()
        setClickListeners()
    }

    private fun observeViewModel() {
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
        budgetViewModel.getSpendingsByCategory(period).observe(viewLifecycleOwner) {
            Log.d("WTF", it.toString())
            detailsAdapter.differ.submitList(it)
        }
    }

    private fun setClickListeners() {
        binding.lblMoreDetails.setOnClickListener {
            binding.rcvSpendingDetails.visibility = View.VISIBLE
        }
    }

    private fun initRecyclerView() {
        detailsAdapter = DetailsAdapter()
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rcvSpendingDetails.layoutManager = layoutManager
        binding.rcvSpendingDetails.adapter = detailsAdapter
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