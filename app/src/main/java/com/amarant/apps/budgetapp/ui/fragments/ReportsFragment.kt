package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.databinding.FragmentReportsBinding
import com.amarant.apps.budgetapp.ui.adapter.ReportsAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.util.Constants.PERIOD_LAST_MONTH
import com.amarant.apps.budgetapp.util.Constants.PERIOD_ONE_MONTH
import com.amarant.apps.budgetapp.util.Constants.PERIOD_ONE_WEEK
import com.amarant.apps.budgetapp.util.Constants.PERIOD_TODAY
import com.amarant.apps.budgetapp.util.Constants.PERIOD_TWO_WEEKS
import com.amarant.apps.budgetapp.util.Constants.PERIOD_YESTERDAY
import com.amarant.apps.budgetapp.util.Constants.SELECT_DATA_RANGE
import com.amarant.apps.budgetapp.util.Constants.PERIOD_SHOW_ALL
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

    private val budgetViewModel: BudgetViewModel by viewModels()
    private lateinit var reportsAdapter: ReportsAdapter
    private val dateRangeArray = arrayOf(
        SELECT_DATA_RANGE,
        PERIOD_TODAY,
        PERIOD_YESTERDAY,
        PERIOD_ONE_WEEK,
        PERIOD_TWO_WEEKS,
        PERIOD_ONE_MONTH,
        PERIOD_LAST_MONTH,
        PERIOD_SHOW_ALL
    )
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
        activity?.title = "Spending Reports"
        startDate = setStartDate()
        initializeRecyclerView()
        setSpinnerValues()
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
        binding.statistics.setOnClickListener {
            val bottomSheet = StatisticsBottomSheetFragment.newInstance(period)
            bottomSheet.show(requireActivity().supportFragmentManager, "StatisticsBottomSheet")
        }
        binding.dateRangeReportSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(parent?.getItemAtPosition(position)) {
                    PERIOD_TODAY -> getReportsForSelectedPeriod(PERIOD_TODAY)
                    PERIOD_YESTERDAY -> getReportsForSelectedPeriod(PERIOD_YESTERDAY)
                    PERIOD_ONE_WEEK -> getReportsForSelectedPeriod(PERIOD_ONE_WEEK)
                    PERIOD_TWO_WEEKS -> getReportsForSelectedPeriod(PERIOD_TWO_WEEKS)
                    PERIOD_ONE_MONTH -> getReportsForSelectedPeriod(PERIOD_ONE_MONTH)
                    PERIOD_LAST_MONTH -> getReportsForSelectedPeriod(PERIOD_LAST_MONTH)
                    else -> getAllEntries()
                }
                period = parent?.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
        binding.dateRangeReportSpinner.setSelection(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(position: Int) {
        val currentBudgetItem = reportsAdapter.differ.currentList[position]
        val bottomSheet = UpdateBudgetBottomSheetFragment(currentBudgetItem)
        bottomSheet.show(requireActivity().supportFragmentManager, "UpdateBudgetBottomSheet")
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

    private fun setStartDate(): String {
        val dateInMillies = Calendar.getInstance().timeInMillis
        return dateMillisToString(dateInMillies)
    }

    private fun setSpinnerValues() {
        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dateRangeArray)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.dateRangeReportSpinner.adapter = arrayAdapter
    }

    private fun getReportsBetweenDates(startDate: String, endDate: String) {
        val start = UtilityFunctions.dateStringToMillis(endDate)
        val end = UtilityFunctions.dateStringToMillis(startDate)
        budgetViewModel.getReportsBetweenDates(start, end)
        budgetViewModel.dateRandeBudgetEntries.observe(viewLifecycleOwner) {
            reportsAdapter.differ.submitList(it)
        }
    }

    private fun getReportsForSelectedPeriod(period: String) {
        val start = when(period) {
            PERIOD_TODAY -> getToday()
            PERIOD_YESTERDAY -> getYesterday()
            PERIOD_ONE_WEEK -> getStartOfWeek()
            PERIOD_TWO_WEEKS -> getStartOfPreviousWeek()
            PERIOD_ONE_MONTH -> getStartOfMonth()
            PERIOD_LAST_MONTH -> getStartOfLastMonth()
            else -> {
                getAllEntries()
                return
            }
        }
        val end = UtilityFunctions.dateStringToMillis(startDate)
        getReportsBetweenDates(dateMillisToString(end), dateMillisToString(start))
    }
}