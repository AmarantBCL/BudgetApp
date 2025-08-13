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
import androidx.recyclerview.widget.ItemTouchHelper
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
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_MONTH
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_TWO_DAYS
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_TWO_MONTHS
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_TWO_WEEKS
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_WEEK
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_THIS_MONTH
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_THIS_WEEK
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_TODAY
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_YESTERDAY
import com.amarant.apps.budgetapp.util.UtilityFunctions
import com.amarant.apps.budgetapp.util.UtilityFunctions.dateMillisToString
import com.amarant.apps.budgetapp.util.UtilityFunctions.getStartOfLastMonth
import com.amarant.apps.budgetapp.util.UtilityFunctions.getStartOfMonth
import com.amarant.apps.budgetapp.util.UtilityFunctions.getStartOfPreviousWeek
import com.amarant.apps.budgetapp.util.UtilityFunctions.getStartOfWeek
import com.amarant.apps.budgetapp.util.UtilityFunctions.getToday
import com.amarant.apps.budgetapp.util.UtilityFunctions.getYesterday
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import kotlin.math.absoluteValue

@AndroidEntryPoint
class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding: FragmentReportsBinding
        get() = _binding ?: throw RuntimeException("FragmentReportsBinding == null")

    private val budgetViewModel: BudgetViewModel by activityViewModels()

    private lateinit var reportsAdapter: ReportsAdapter
    private lateinit var startDate: String

    private var period = PERIOD_THIS_MONTH

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
        startDate = setStartDate()
        initRecyclerView()
        setSpinnerValues()
        setHasOptionsMenu(true)
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
        binding.dateRangeReportSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                getReportsForSelectedPeriod(position)
                period = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        binding.dateRangeReportSpinner.setSelection(PERIOD_THIS_MONTH)
    }

    private fun initRecyclerView() {
        reportsAdapter = ReportsAdapter()
        val dividerDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.divider)!!
        binding.recyclerReports.addItemDecoration(CustomDividerDecoration(dividerDrawable))
        binding.recyclerReports.adapter = reportsAdapter
        reportsAdapter.onItemClickListener = {
            budgetViewModel.toggleSelection(it.entry.budget.id ?: -1)
        }
        reportsAdapter.onItemLongClickListener = {
            val action = ReportsFragmentDirections.actionReportsFragmentToFragmentAddEntry(
                selectedDate = it.entry.budget.date.toLong(),
                budgetEntry = it.entry
            )
            findNavController().navigate(action)
        }
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
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
                when(val budget = reportsAdapter.currentList[position]) {
                    is ReportsItem.Entry -> {
                        budgetViewModel.deleteEntry(budget.entry.budget)
                        Snackbar.make(requireView(), getString(R.string.item_deleted), Snackbar.LENGTH_LONG).apply {
                            setAction(getString(R.string.undo)) {
                                budgetViewModel.insertBudget(budget.entry.budget)
                            }
                            show()
                        }
                    }
                    else -> {}
                }
            }

            override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                return if (viewHolder.itemViewType == ReportsAdapter.VIEW_TYPE_DATE) {
                    0
                } else {
                    super.getSwipeDirs(recyclerView, viewHolder)
                }
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.recyclerReports)
        }
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

    private fun setStartDate(): String {
        val dateInMillis = Calendar.getInstance().timeInMillis
        return dateMillisToString(dateInMillis)
    }

    private fun setSpinnerValues() {
        val dateRangeArray = resources.getStringArray(R.array.periods)
        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dateRangeArray)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.dateRangeReportSpinner.adapter = arrayAdapter
    }

    private fun getReportsBetweenDates(startDate: String, endDate: String) {
        val start = UtilityFunctions.dateStringToMillis(startDate)
        val end = UtilityFunctions.dateStringToMillis(endDate)
        budgetViewModel.setReportsBetweenDates(start, end)
    }

    private fun getReportsForSelectedPeriod(period: Int) {
        val start = when(period) {
            PERIOD_TODAY -> getToday()
            PERIOD_YESTERDAY -> getYesterday()
            PERIOD_LAST_TWO_DAYS -> getYesterday()
            PERIOD_THIS_WEEK -> getStartOfWeek()
            PERIOD_LAST_WEEK -> getStartOfPreviousWeek()
            PERIOD_LAST_TWO_WEEKS -> getStartOfPreviousWeek()
            PERIOD_THIS_MONTH -> getStartOfMonth()
            PERIOD_LAST_MONTH -> getStartOfLastMonth()
            PERIOD_LAST_TWO_MONTHS -> getStartOfLastMonth()
            else -> {
                getReportsBetweenDates(dateMillisToString(0L), startDate)
                return
            }
        }
        val end = when(period) {
            PERIOD_YESTERDAY -> getToday() - 1000
            PERIOD_LAST_WEEK -> getStartOfWeek() - 1000
            PERIOD_LAST_MONTH -> getStartOfMonth() - 1000
            else -> UtilityFunctions.dateStringToMillis(startDate)
        }
        getReportsBetweenDates(dateMillisToString(start), dateMillisToString(end))
    }
}