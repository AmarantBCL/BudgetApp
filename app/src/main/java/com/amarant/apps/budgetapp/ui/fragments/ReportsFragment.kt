package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentReportsBinding
import com.amarant.apps.budgetapp.entities.BudgetUI
import com.amarant.apps.budgetapp.entities.ReportsItem
import com.amarant.apps.budgetapp.ui.adapter.ReportsAdapter
import com.amarant.apps.budgetapp.ui.adapter.decorations.CustomDividerDecoration
import com.amarant.apps.budgetapp.ui.fragments.bottomsheet.FiltersBottomSheetFragment
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.util.Constants
import com.amarant.apps.budgetapp.util.NumberUtils
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_THIS_MONTH
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.absoluteValue

@AndroidEntryPoint
class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding: FragmentReportsBinding
        get() = _binding ?: throw RuntimeException("FragmentReportsBinding == null")

    private val budgetViewModel: BudgetViewModel by activityViewModels()

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
        setHasOptionsMenu(true)
        initRecyclerView()
        setSpinnerValues()
        observeViewModel()
        setClickListeners()
        // TODO Debug navigation
        val navController = findNavController()
        Log.d("DebugNavController", "[CURRENT DEST] ${navController.currentDestination}")
        Log.e("DebugNavController", "[START DEST] ${navController.graph.startDestinationId}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reports_menu, menu)
        val searchItem = menu.findItem(R.id.search)
        val searchView = searchItem?.actionView as? SearchView
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { budgetViewModel.setSearchQuery(it) }
                searchView.clearFocus()
                searchItem.collapseActionView()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.filters) {
            val bottomSheet = FiltersBottomSheetFragment.newInstance()
            bottomSheet.show(requireActivity().supportFragmentManager, FiltersBottomSheetFragment.TAG)
        }
        if (item.itemId == R.id.sort) {
            budgetViewModel.setSearchQuery("")
            true
        }
        return true
    }

    private fun setClickListeners() {
        binding.spinnerDateRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                budgetViewModel.changeDateRange(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        binding.spinnerDateRange.setSelection(PERIOD_THIS_MONTH)
    }

    private fun initRecyclerView() {
        reportsAdapter = ReportsAdapter()
        val customDivider = ContextCompat.getDrawable(requireContext(), R.drawable.divider)
        if (customDivider != null) {
            binding.recyclerReports.addItemDecoration(CustomDividerDecoration(customDivider))
        } else {
            val defaultDivider = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            binding.recyclerReports.addItemDecoration(defaultDivider)
        }
        binding.recyclerReports.adapter = reportsAdapter
        reportsAdapter.onItemClickListener = {
            budgetViewModel.toggleSelection(it.budget.id ?: -1)
        }
        reportsAdapter.onItemLongClickListener = {
            val action = ReportsFragmentDirections.actionReportsFragmentToFragmentAddEntry(
                selectedDate = it.budget.date.toLong(),
                budgetEntry = it
            )
            findNavController().navigate(action)
        }
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
                val budget = reportsAdapter.currentList[position]
                if (budget is ReportsItem.Entry) {
                    budgetViewModel.deleteEntry(budget.entry.budget)
                    Snackbar.make(requireView(), getString(R.string.item_deleted), Snackbar.LENGTH_LONG).apply {
                        setAction(getString(R.string.undo)) {
                            budgetViewModel.insertBudget(budget.entry.budget)
                        }
                        show()
                    }
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
        itemTouchHelper.attachToRecyclerView(binding.recyclerReports)
    }

    private fun observeViewModel() {
        budgetViewModel.reportsUI.observe(viewLifecycleOwner) { groupedReports ->
            reportsAdapter.submitList(groupedReports)
        }
        budgetViewModel.getBudgetUIEntriesBetweenDates().observe(viewLifecycleOwner) { reports ->
            setIncomeExpensesViews(reports)
            binding.imgDollar.visibility = if (reports.isNotEmpty()) View.GONE else View.VISIBLE
            binding.lblEmptyEntries.visibility = if (reports.isNotEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun setIncomeExpensesViews(reports: List<BudgetUI>) {
        val totalIncome = reports.filter { !it.isHidden && it.budget.creditOrDebit == Constants.CREDIT }.sumOf {
            it.budget.amount.toDouble()
        }
        val totalExpenses = reports.filter { !it.isHidden && it.budget.creditOrDebit == Constants.DEBIT }.sumOf {
            it.budget.amount.toDouble()
        }
        val netIncome = totalIncome - totalExpenses.absoluteValue
        val formattedIncome = NumberUtils.formatNumberWithThousandsSeparator(totalIncome)
        val formattedExpenses = NumberUtils.formatNumberWithThousandsSeparator(totalExpenses)
        val formattedNet = NumberUtils.formatNumberWithThousandsSeparator(netIncome)
        binding.tvIncome.text =
            if (totalIncome > 0) getString(R.string.placeholder_plus, formattedIncome) else formattedIncome
        binding.tvExpenses.text = formattedExpenses
        if (netIncome > 0) {
            binding.tvNetIncome.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.positive_green
                )
            )
            binding.tvNetIncome.text = getString(R.string.placeholder_plus, formattedNet)
        } else {
            binding.tvNetIncome.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.negative_red
                )
            )
            binding.tvNetIncome.text = formattedNet
        }
    }

    private fun setSpinnerValues() {
        val dateRangeArray = resources.getStringArray(R.array.periods)
        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dateRangeArray)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDateRange.adapter = arrayAdapter
    }
}