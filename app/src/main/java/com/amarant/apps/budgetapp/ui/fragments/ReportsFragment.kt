package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.amarant.apps.budgetapp.databinding.FragmentReportsBinding
import com.amarant.apps.budgetapp.ui.adapter.ReportsAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReportsFragment : Fragment(), ReportsAdapter.MyOnClickListener {

    private var _binding: FragmentReportsBinding? = null
    private val binding: FragmentReportsBinding
        get() = _binding ?: throw RuntimeException("FragmentReportsBinding == null")

    private val budgetViewModel: BudgetViewModel by viewModels()
    private lateinit var reportsAdapter: ReportsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeRecyclerView()
        getAllEntries()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(position: Int) {
        val currentBudgetItem = reportsAdapter.differ.currentList[position]
        val bottomSheet = UpdateBudgetBottomSheetFragment(currentBudgetItem)
        bottomSheet.show(requireActivity().supportFragmentManager, "UpdateBudget")
    }

    private fun initializeRecyclerView() {
        reportsAdapter = ReportsAdapter(this)
        binding.rcvReports.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reportsAdapter
        }
    }

    private fun getAllEntries() {
        budgetViewModel.allBudgetEntries.observe(viewLifecycleOwner) {
            reportsAdapter.differ.submitList(it)
        }
    }
}