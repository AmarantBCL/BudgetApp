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
import com.amarant.apps.budgetapp.ui.adapter.DeprecatedReportsAdapter
import com.amarant.apps.budgetapp.ui.adapter.ReportsAdapter
import com.amarant.apps.budgetapp.ui.adapter.TodayBudgetAdapter
import com.amarant.apps.budgetapp.ui.fragments.bottomsheet.FiltersBottomSheetFragment
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.util.Constants
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_MONTH
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_TWO_DAYS
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_TWO_MONTHS
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_TWO_WEEKS
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_LAST_WEEK
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_SHOW_ALL
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
class ReportsFragment : Fragment(), DeprecatedReportsAdapter.MyOnClickListener {

    private var _binding: FragmentReportsBinding? = null
    private val binding: FragmentReportsBinding
        get() = _binding ?: throw RuntimeException("FragmentReportsBinding == null")

    private val budgetViewModel: BudgetViewModel by activityViewModels()
    private lateinit var reportsAdapter: ReportsAdapter
    private lateinit var startDate: String
    private var period = PERIOD_SHOW_ALL

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
//        activity?.title = getString(R.string.spending_reports)
        startDate = setStartDate()
        initializeRecyclerView()
        setSpinnerValues()
        setHasOptionsMenu(true)
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
                val budget = reportsAdapter.currentList[position]
                budgetViewModel.deleteEntry(budget.budget)
                Snackbar.make(view, getString(R.string.item_deleted), Snackbar.LENGTH_LONG).apply {
                    setAction(getString(R.string.undo)) {
                        budgetViewModel.insertBudget(budget.budget)
                    }
                    show()
                }
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.recyclerReports)
        }
        getAllEntries()
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
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.filters) {
            val bottomSheet = FiltersBottomSheetFragment.newInstance()
            bottomSheet.show(requireActivity().supportFragmentManager, FiltersBottomSheetFragment.TAG)
        }
        return true
    }

    override fun onClick(position: Int) {
//        val currentBudgetItem = reportsAdapter.currentList[position]
//        val bottomSheet = UpdateBudgetBottomSheetFragment.newInstance(currentBudgetItem)
//        bottomSheet.show(requireActivity().supportFragmentManager, UpdateBudgetBottomSheetFragment.TAG)
    }

    private fun setClickListeners() {
//        binding.statistics.setOnClickListener {
//            val bottomSheet = StatisticsBottomSheetFragment.newInstance(period)
//            bottomSheet.show(requireActivity().supportFragmentManager, StatisticsBottomSheetFragment.TAG)
//        }
        binding.dateRangeReportSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                getReportsForSelectedPeriod(position)
                period = position
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
        binding.dateRangeReportSpinner.setSelection(PERIOD_SHOW_ALL)
    }

    private fun initializeRecyclerView() {
        reportsAdapter = ReportsAdapter()
        val divider = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.recyclerReports.addItemDecoration(divider)
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
    }

    private fun getAllEntries() {
        budgetViewModel.getBudgetUIEntriesBetweenDates().observe(viewLifecycleOwner) { reports ->
            reportsAdapter.submitList(reports)
//            binding.tvNumberOfEntries.text = getString(R.string.number_of_entries, reports.size)
            setIncomeExpensesViews(reports)
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
        binding.tvIncome.text =
            if (totalIncome > 0) getString(R.string.placeholder_plus, totalIncome.toString()) else totalIncome.toString()
        binding.tvExpenses.text = totalExpenses.toString()
        if (netIncome > 0) {
            binding.tvNetIncome.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.positive_green
                )
            )
            binding.tvNetIncome.text = getString(R.string.placeholder_plus, netIncome.toString())
        } else {
            binding.tvNetIncome.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.negative_red
                )
            )
            binding.tvNetIncome.text = netIncome.toString()
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