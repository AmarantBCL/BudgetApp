package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.amarant.apps.budgetapp.databinding.FragmentBudgetHistoryBinding
import com.amarant.apps.budgetapp.ui.adapter.BudgetHistoryAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetPlanningViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BudgetHistoryFragment : Fragment() {

    private var _binding: FragmentBudgetHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetPlanningViewModel by viewModels()
    private val historyAdapter = BudgetHistoryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvHistory.adapter = historyAdapter
    }

    private fun observeViewModel() {
        viewModel.budgetHistory.observe(viewLifecycleOwner) { history ->
            historyAdapter.submitList(history)
            binding.tvEmpty.visibility = if (history.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
