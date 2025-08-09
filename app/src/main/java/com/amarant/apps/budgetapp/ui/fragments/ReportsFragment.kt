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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentReportsBinding
import com.amarant.apps.budgetapp.ui.adapter.ReportsAdapter
import com.amarant.apps.budgetapp.ui.fragments.bottomsheet.FiltersBottomSheetFragment
import com.amarant.apps.budgetapp.ui.fragments.bottomsheet.StatisticsBottomSheetFragment
import com.amarant.apps.budgetapp.ui.fragments.bottomsheet.UpdateBudgetBottomSheetFragment
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
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

@AndroidEntryPoint
class ReportsFragment : Fragment(), ReportsAdapter.MyOnClickListener {

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
        activity?.title = getString(R.string.spending_reports)
        startDate = setStartDate()
        initializeRecyclerView()
        setSpinnerValues()
        setHasOptionsMenu(true)
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
                Snackbar.make(view, getString(R.string.item_deleted), Snackbar.LENGTH_LONG).apply {
                    setAction(getString(R.string.undo)) {
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
        val currentBudgetItem = reportsAdapter.differ.currentList[position]
        val bottomSheet = UpdateBudgetBottomSheetFragment.newInstance(currentBudgetItem)
        bottomSheet.show(requireActivity().supportFragmentManager, UpdateBudgetBottomSheetFragment.TAG)
    }

    private fun setClickListeners() {
        binding.statistics.setOnClickListener {
            val bottomSheet = StatisticsBottomSheetFragment.newInstance(period)
            bottomSheet.show(requireActivity().supportFragmentManager, StatisticsBottomSheetFragment.TAG)
        }
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
        reportsAdapter = ReportsAdapter(this)
        binding.rcvReports.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reportsAdapter
        }
    }

    private fun getAllEntries() {
        budgetViewModel.getReportsBetweenDates().observe(viewLifecycleOwner) {
            reportsAdapter.differ.submitList(it)
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