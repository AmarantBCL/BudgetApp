package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentBudgetPlanningBinding
import com.amarant.apps.budgetapp.ui.adapter.BudgetPlanningAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetPlanningViewModel
import com.amarant.apps.budgetapp.util.NumberUtils.formatNumberWithThousandsSeparator
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class BudgetPlanningFragment : Fragment() {

    private var _binding: FragmentBudgetPlanningBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetPlanningViewModel by viewModels()
    private lateinit var adapter: BudgetPlanningAdapter

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
        setupClickListeners()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        val activity = requireActivity() as MainActivity
//        val appBar = activity.findViewById<AppBarLayout>(R.id.app_bar)
//        TransitionManager.beginDelayedTransition(appBar, Fade())
        inflater.inflate(R.menu.calendar_menu, menu)
        val buttonItem = menu.findItem(R.id.action_button_item)
        val button = buttonItem.actionView?.findViewById<MaterialButton>(R.id.menu_button)
        button?.setOnClickListener {
//            val action = CalendarFragmentDirections.actionCalendarFragmentToFragmentAddEntry(
//                selectedDate = currentDateMillis,
//                budgetEntry = null
//            )
//            findNavController().navigate(action)
            showCreateBudgetDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = BudgetPlanningAdapter(
            onDeleteClick = { budget -> viewModel.deleteBudget(budget) },
            onEditClick = { budget -> 
                // TODO: Open edit dialog
            }
        )
        binding.rvBudgets.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.budgetListWithProgress.observe(viewLifecycleOwner) { budgets ->
            adapter.submitList(budgets)
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
            }
        }
    }

    private fun setupClickListeners() {
//        binding.btnAddBudget.setOnClickListener {
//            showCreateBudgetDialog()
//        }
        binding.btnCreateFirstBudget.setOnClickListener {
            showCreateBudgetDialog()
        }
    }

    private fun showCreateBudgetDialog() {
        val bottomSheet = com.amarant.apps.budgetapp.ui.fragments.bottomsheet.CreateBudgetBottomSheetFragment()
        bottomSheet.show(childFragmentManager, com.amarant.apps.budgetapp.ui.fragments.bottomsheet.CreateBudgetBottomSheetFragment.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
