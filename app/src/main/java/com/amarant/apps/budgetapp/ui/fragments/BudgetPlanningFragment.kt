package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import java.util.Locale
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentBudgetPlanningBinding
import com.amarant.apps.budgetapp.ui.adapter.BudgetPlanningAdapter
import com.amarant.apps.budgetapp.ui.adapter.ReportsAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetPlanningViewModel
import com.amarant.apps.budgetapp.util.NumberUtils.formatNumberWithThousandsSeparator
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BudgetPlanningFragment : Fragment() {

    private var _binding: FragmentBudgetPlanningBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetPlanningViewModel by viewModels()
    private lateinit var budgetPlanningAdapter: BudgetPlanningAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetPlanningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.budget_planning_menu, menu)
        val buttonItem = menu.findItem(R.id.action_button_item)
        val button = buttonItem.actionView?.findViewById<MaterialButton>(R.id.menu_button)
        button?.setOnClickListener {
            findNavController().navigate(BudgetPlanningFragmentDirections.actionBudgetPlanningFragmentToAddBudgetFragment(null))
        }
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_budget_history -> {
                findNavController().navigate(R.id.action_budgetPlanningFragment_to_budgetHistoryFragment)
                true
            }
            R.id.action_settings -> {
                findNavController().navigate(BudgetPlanningFragmentDirections.actionBudgetPlanningFragmentToProfileFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        budgetPlanningAdapter = BudgetPlanningAdapter(
            onDeleteClick = { budget -> viewModel.deleteBudget(budget) },
            onEditClick = { budget ->
                findNavController().navigate(BudgetPlanningFragmentDirections.actionBudgetPlanningFragmentToAddBudgetFragment(budget))
            }
        )
        binding.rvBudgets.adapter = budgetPlanningAdapter
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val budget = budgetPlanningAdapter.currentList[position]
                viewModel.deleteBudget(budget.budget)
                Snackbar.make(requireView(), getString(R.string.item_deleted), Snackbar.LENGTH_LONG).apply {
                    setAction(getString(R.string.undo)) {
                        viewModel.insertBudget(budget.budget)
                    }
                    show()
                }
            }

            override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                return if (viewHolder.itemViewType == ReportsAdapter.VIEW_TYPE_DATE) {
                    0
                } else {
                    super.getSwipeDirs(recyclerView, viewHolder)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rvBudgets)
    }

    private fun observeViewModel() {
        viewModel.budgetListWithProgress.observe(viewLifecycleOwner) { budgets ->
            budgetPlanningAdapter.submitList(budgets)
            binding.groupEmptyState.visibility = if (budgets.isEmpty()) View.VISIBLE else View.GONE
            binding.rvBudgets.visibility = if (budgets.isEmpty()) View.GONE else View.VISIBLE
//            binding.cardSummary.visibility = if (budgets.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.budgetSummary.observe(viewLifecycleOwner) { summary ->
            binding.apply {
//                tvTotalBudgeted.text = String.format(Locale.getDefault(), "USD %.2f", summary.totalBudgeted)
//                tvTotalSpent.text = String.format(Locale.getDefault(), "USD %.2f", summary.totalSpent)
//                tvProgressPercent.text = String.format(Locale.getDefault(), "%.1f%%", summary.overallProgress)
//                progressOverall.progress = summary.overallProgress.toInt()
                tvTotalBudgeted.text = formatNumberWithThousandsSeparator(summary.totalBudgeted) //String.format(Locale.getDefault(), "USD %.2f", summary.totalBudgeted)
                tvTotalSpent.text = formatNumberWithThousandsSeparator(summary.totalSpent) //String.format(Locale.getDefault(), "USD %.2f", summary.totalSpent)
                tvProgressPercent.text = String.format(Locale.getDefault(), "%.2f%%", summary.overallProgress)
                progressOverall.progress = summary.overallProgress.toInt()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
