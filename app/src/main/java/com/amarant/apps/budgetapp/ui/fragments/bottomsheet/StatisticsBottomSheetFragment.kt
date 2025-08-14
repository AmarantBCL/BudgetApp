package com.amarant.apps.budgetapp.ui.fragments.bottomsheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.amarant.apps.budgetapp.databinding.StatisticsBottomSheetBinding
import com.amarant.apps.budgetapp.ui.adapter.DetailsAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_SHOW_ALL
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@Deprecated("Obsolete")
@AndroidEntryPoint
class StatisticsBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: StatisticsBottomSheetBinding? = null
    private val binding: StatisticsBottomSheetBinding
        get() = _binding ?: throw RuntimeException("StatisticsBottomSheetBinding == null")

    private val budgetViewModel: BudgetViewModel by viewModels()

    private var period: Int = PERIOD_SHOW_ALL

    private lateinit var detailsAdapter: DetailsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        period = requireArguments().getInt(KEY_PERIOD)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        detailsAdapter.itemClickListener = { position, operation ->
            val item = detailsAdapter.differ.currentList[position]
            val currentSpending = binding.totalSpending.text.toString().toFloat()
            Log.d("WTF", "currentSpending: $currentSpending [${binding.totalSpending.text}]")
            Log.d("WTF", "${item.amount} (${item.amount * -1})")
            if (operation) {
                Log.e("WTF", "ADD")
                binding.totalSpending.text = (currentSpending + item.amount * -1).toString()
                Log.e("WTF", "Became: ${currentSpending + item.amount * -1}")
            } else {
                Log.e("WTF", "SUBTRACT")
                binding.totalSpending.text = (currentSpending - item.amount * -1).toString()
                Log.e("WTF", "Became: ${currentSpending - item.amount * -1}")
            }
        }
    }

    companion object {

        private const val KEY_PERIOD = "period"

        const val TAG = "StatisticsBottomSheet"

        fun newInstance(period: Int): StatisticsBottomSheetFragment {
            return StatisticsBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_PERIOD, period)
                }
            }
        }
    }
}