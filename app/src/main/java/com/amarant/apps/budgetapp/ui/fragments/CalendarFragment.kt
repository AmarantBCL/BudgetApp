package com.amarant.apps.budgetapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentCalendarBinding
import com.amarant.apps.budgetapp.entities.BudgetUI
import com.amarant.apps.budgetapp.ui.MainActivity
import com.amarant.apps.budgetapp.ui.adapter.TodayBudgetAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.CalendarViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.PiggyBankViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.ProfileViewModel
import com.amarant.apps.budgetapp.util.Constants
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_IS_PIN_ENTERED_KEY
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_NAME
import com.amarant.apps.budgetapp.util.DateUtils.getFormattedDate
import com.amarant.apps.budgetapp.util.DateUtils.getTimestampFromDate
import com.amarant.apps.budgetapp.util.NumberUtils
import com.amarant.apps.budgetapp.util.UtilityFunctions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.Date
import kotlin.math.absoluteValue

@AndroidEntryPoint
class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding: FragmentCalendarBinding
        get() = _binding ?: throw RuntimeException("FragmentCalendarBinding == null")

    private val profileViewModel: ProfileViewModel by viewModels()
    private val piggyBankViewModel: PiggyBankViewModel by viewModels()
    private val budgetViewModel: BudgetViewModel by viewModels()
    private val calendarViewModel: CalendarViewModel by viewModels()

    private lateinit var todayBudgetAdapter: TodayBudgetAdapter

    private var currentDateMillis: Long = System.currentTimeMillis()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        initViews()
        observeViewModel()
        setClickListeners()
        setCalendarListeners()
        // TODO Debug navigation
        val navController = findNavController()
        Log.d("DebugNavController", "[CURRENT DEST] ${navController.currentDestination}")
        Log.e("DebugNavController", "[START DEST] ${navController.graph.startDestinationId}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() {
        todayBudgetAdapter = TodayBudgetAdapter()
        val divider = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.recyclerEntries.addItemDecoration(divider)
        binding.recyclerEntries.adapter = todayBudgetAdapter
        todayBudgetAdapter.onItemClickListener = {
            budgetViewModel.toggleSelection(it.budget.id ?: -1)
        }
        todayBudgetAdapter.onItemLongClickListener = {
            val action = CalendarFragmentDirections.actionCalendarFragmentToFragmentAddEntry(
                selectedDate = currentDateMillis,
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
                val item = todayBudgetAdapter.currentList[position]
                budgetViewModel.deleteEntry(item.budget)
                Snackbar.make(requireView(), getString(R.string.item_deleted), Snackbar.LENGTH_LONG).apply {
                    setAction(getString(R.string.undo)) {
                        budgetViewModel.insertBudget(item.budget)
                    }
                    show()
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerEntries)
    }

    private fun observeViewModel() {
        profileViewModel.profileLiveData.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                // TODO Disabled temporarily so as not to create lots of piggy banks
//                piggyBankViewModel.updatePiggyBank(PiggyBank(
//                    1,
//                    0,
//                    0,
//                    0,
//                    0)
//                )
//                findNavController().navigate(R.id.action_global_profileFragment, null, navOptions {
//                    popUpTo(R.id.calendarFragment) {
//                        inclusive = true
//                    }
//                })
            } else {
//                checkPin()
            }
        }
//        // TODO DEL Temp for testing
//        piggyBankViewModel.getPiggyBank().observe(viewLifecycleOwner) {
//            if (it == null) {
//                piggyBankViewModel.updatePiggyBank(PiggyBank(
//                    0,
//                    0,
//                    0,
//                    0,
//                    0)
//                )
//            }
//        }
        budgetViewModel.getBudgetEntriesBetweenDates().observe(viewLifecycleOwner) { reports ->
            todayBudgetAdapter.submitList(reports.reversed())
            val count = reports.size
            val entriesString = resources.getQuantityString(R.plurals.entries_count, count, count)
            binding.tvNumberOfEntries.text = entriesString
            if (reports.isNotEmpty()) {
                setIncomeExpensesViews(reports)
                setRecyclerViewVisibility(true)
                setAppBarScrolling(binding.cardCalendar, true)
            } else {
                setAppBarScrolling(binding.cardCalendar, false)
                setRecyclerViewVisibility(false)
            }
        }
        calendarViewModel.selectedDate.value?.let { savedDate ->
            val date = Date(savedDate)
            val startDate = getTimestampFromDate(date)
            val displayedDate = getFormattedDate(savedDate)
            budgetViewModel.setReportsBetweenDates(startDate, startDate)
            currentDateMillis = startDate
            binding.calendarView.date = savedDate
            binding.tvTodayDate.text = displayedDate
        }
    }

    private fun setCalendarListeners() {
        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            val selectedDate = "$day/${month + 1}/$year"
            val startDate = UtilityFunctions.dateStringToMillis(selectedDate)
            val endDate = UtilityFunctions.dateStringToMillis("${day}/${month + 1}/$year")
            currentDateMillis = startDate
            budgetViewModel.setReportsBetweenDates(startDate, endDate)
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)
            val displayedDate = getFormattedDate(calendar.timeInMillis)
            binding.tvTodayDate.text = displayedDate
            calendarViewModel.setSelectedDate(calendar.timeInMillis)
        }
    }

    private fun setClickListeners() {
        val toolbar =
            (requireActivity() as MainActivity).findViewById<MaterialToolbar>(R.id.toolbar)
        val button = toolbar.findViewById<Button>(R.id.btn_action)
        button.setOnClickListener {
            val action = CalendarFragmentDirections.actionCalendarFragmentToFragmentAddEntry(
                selectedDate = currentDateMillis,
                budgetEntry = null
            )
            findNavController().navigate(action)
        }
    }

    private fun setIncomeExpensesViews(reports: List<BudgetUI>) {
        val totalIncome = reports.filter { !it.isHidden && it.budget.creditOrDebit == Constants.CREDIT }.sumOf {
            it.budget.amount.toDouble()
        }
        val totalExpenses = reports.filter { !it.isHidden && it.budget.creditOrDebit == Constants.DEBIT }.sumOf {
            it.budget.amount.toDouble()
        }
//        val netIncome = totalIncome - totalExpenses.absoluteValue
        val formattedIncome = NumberUtils.formatNumberWithThousandsSeparator(totalIncome)
        val formattedExpenses = NumberUtils.formatNumberWithThousandsSeparator(totalExpenses)
//        val formattedNet = NumberUtils.formatNumberWithThousandsSeparator(netIncome)
        binding.tvIncome.text =
            if (totalIncome > 0) getString(R.string.placeholder_plus, formattedIncome) else formattedIncome
        binding.tvExpenses.text = formattedExpenses
//        if (netIncome > 0) {
//            binding.tvNetIncome.setTextColor(
//                ContextCompat.getColor(
//                    requireContext(),
//                    R.color.positive_green
//                )
//            )
//            binding.tvNetIncome.text = getString(R.string.placeholder_plus, formattedNet)
//        } else {
//            binding.tvNetIncome.setTextColor(
//                ContextCompat.getColor(
//                    requireContext(),
//                    R.color.negative_red
//                )
//            )
//            binding.tvNetIncome.text = formattedNet
//        }
    }

    private fun setRecyclerViewVisibility(isVisible: Boolean) {
        binding.recyclerEntries.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.cardIncomeExpenses.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.tvEmptyEntries.visibility = if (isVisible) View.GONE else View.VISIBLE
        binding.imgDollar.visibility = if (isVisible) View.GONE else View.VISIBLE
    }

    private fun setAppBarScrolling(calendarView: CardView, isEnabled: Boolean) {
        val params = calendarView.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = if (isEnabled) {
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        } else {
            0
        }
        calendarView.layoutParams = params
    }

    private fun checkPin() {
        val sharedPrefs =
            requireContext().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val isPinEntered = sharedPrefs.getBoolean(PREFERENCE_IS_PIN_ENTERED_KEY, false)
        Log.e("WTF", "Check PIN: $isPinEntered")
        if (!isPinEntered) {
            findNavController().navigate(R.id.action_calendarFragment_to_pinFragment)
        }
    }
}