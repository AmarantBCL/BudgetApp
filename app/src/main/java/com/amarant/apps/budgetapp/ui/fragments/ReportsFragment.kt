package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.databinding.FragmentReportsBinding
import com.amarant.apps.budgetapp.ui.adapter.ReportsAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.google.android.material.snackbar.Snackbar
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
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val budget = reportsAdapter.differ.currentList[position]
                budgetViewModel.deleteEntry(budget)
                Snackbar.make(view, "Item Deleted", Snackbar.LENGTH_LONG).apply {
                    setAction("UNDO") {
                        budgetViewModel.insertBudget(budget)
                    }
                    show()
                }
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.rcvReports)
        }
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