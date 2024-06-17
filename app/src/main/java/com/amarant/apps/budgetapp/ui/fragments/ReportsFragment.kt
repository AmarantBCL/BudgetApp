package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.util.Log
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
import com.amarant.apps.budgetapp.util.UtilityFunctions
import com.amarant.apps.budgetapp.util.UtilityFunctions.dateMillisToString
import com.amarant.apps.budgetapp.util.UtilityFunctions.getEndDate
import com.amarant.apps.budgetapp.util.UtilityFunctions.getStartOfMonth
import com.amarant.apps.budgetapp.util.UtilityFunctions.getStartOfPreviousWeek
import com.amarant.apps.budgetapp.util.UtilityFunctions.getStartOfWeek
import com.google.android.material.snackbar.BaseTransientBottomBar
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
    private val dateRangeArray = arrayOf("Select Date Range", "1 Week", "2 Weeks", "1 Month", "Show All")
    private lateinit var startDate: String

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
            val bottomSheet = StatisticsBottomSheetFragment()
            bottomSheet.show(requireActivity().supportFragmentManager, "StatisticsBottomSheet")
        }
        binding.dateRangeReportSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(parent?.getItemAtPosition(position)) {
                    "1 Week" -> getReportsForSelectedPeriod("1 Week")
                    "2 Weeks" -> getReportsForSelectedPeriod("2 Weeks")
                    "1 Month" -> getReportsForSelectedPeriod("1 Month")
                    else -> getAllEntries()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
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
            "1 Week" -> getStartOfWeek()
            "2 Weeks" -> getStartOfPreviousWeek()
            "1 Month" -> getStartOfMonth()
            else -> {
                getAllEntries()
                return
            }
        }
        val end = UtilityFunctions.dateStringToMillis(startDate)
        getReportsBetweenDates(dateMillisToString(end), dateMillisToString(start))
    }
}