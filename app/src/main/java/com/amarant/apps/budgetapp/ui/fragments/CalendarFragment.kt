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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentCalendarBinding
import com.amarant.apps.budgetapp.ui.MainActivity
import com.amarant.apps.budgetapp.ui.adapter.TodayBudgetAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.CalendarViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.PiggyBankViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.ProfileViewModel
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_IS_PIN_ENTERED_KEY
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_NAME
import com.amarant.apps.budgetapp.util.DateUtils
import com.amarant.apps.budgetapp.util.UtilityFunctions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
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

    private var initialScrollFlags = 0
    private var currentDate: String? = null

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
        val cardCalendarParams = binding.cardCalendar.layoutParams as AppBarLayout.LayoutParams
        initialScrollFlags = cardCalendarParams.scrollFlags
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() {
        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            val selectedDate = "$day/${month + 1}/$year"
            currentDate = selectedDate
            val start = UtilityFunctions.dateStringToMillis(selectedDate)
            val end = UtilityFunctions.dateStringToMillis("${day}/${month + 1}/$year")
            budgetViewModel.setReportsBetweenDates(start, end)
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)
//            Log.e("WTF", "[CALENDAR DATE CHANGED] Calendar.DAY: ${calendar.get(Calendar.DATE)} Calendar.MONTH: ${calendar.get(Calendar.MONTH)} Calendar.YEAR: ${calendar.get(Calendar.YEAR)}")
            val date = DateUtils.getFormattedDate(calendar.timeInMillis)
//            Log.d("WTF", "[FORMATTED DATE] $date")
            binding.tvTodayDate.text = date
            calendarViewModel.setSelectedDate(calendar.timeInMillis)
        }
        todayBudgetAdapter = TodayBudgetAdapter()
        val divider = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.recyclerEntries.addItemDecoration(divider)
        binding.recyclerEntries.adapter = todayBudgetAdapter
        val toolbar = (requireActivity() as MainActivity).findViewById<MaterialToolbar>(R.id.toolbar)
        val button = toolbar.findViewById<Button>(R.id.btn_action)
        button.setOnClickListener {
            currentDate?.let {
                val action = CalendarFragmentDirections.actionCalendarFragmentToBudgetEntryFragment(it)
                Log.e("[CalendarFragment]", "$action")
                findNavController().navigate(action)
            }
        }
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
        budgetViewModel.getReportsBetweenDates().observe(viewLifecycleOwner) {
            todayBudgetAdapter.submitList(it.reversed())
            binding.tvNumberOfEntries.text = "${it.size} entries"
            Log.d("WTF", "[LIVE DATA UPDATE] getReportsBetweenDates()")
            if (it.isNotEmpty()) {
                val totalIncome = it.filter { it.creditOrDebit == "Credit" }.sumOf { it.amount.toDouble() }
                val totalExpenses = it.filter { it.creditOrDebit == "Debit" }.sumOf { it.amount.toDouble() }
                val netIncome = totalIncome - totalExpenses.absoluteValue
                binding.recyclerEntries.visibility = View.VISIBLE
                binding.cardIncomeExpenses.visibility = View.VISIBLE
                binding.tvEmptyEntries.visibility = View.GONE
                binding.imgDollar.visibility = View.GONE
                binding.tvIncome.text = "+$totalIncome"
                binding.tvExpenses.text = totalExpenses.toString()
                setAppBarScrolling(binding.cardCalendar, true)
                if (netIncome > 0) {
                    binding.tvNetIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.positive_green))
                    binding.tvNetIncome.text = "+$netIncome"
                } else {
                    binding.tvNetIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.negative_red))
                    binding.tvNetIncome.text = netIncome.toString()
                }
            } else {
                setAppBarScrolling(binding.cardCalendar, false)
                binding.recyclerEntries.visibility = View.GONE
                binding.cardIncomeExpenses.visibility = View.GONE
                binding.tvEmptyEntries.visibility = View.VISIBLE
                binding.imgDollar.visibility = View.VISIBLE
            }
        }
        calendarViewModel.selectedDate.value?.let { savedDate ->
            val date = Date(savedDate)
            val debugFormatter = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.getDefault())
            val anotherFormatter = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
            val debugFormattedDate = debugFormatter.format(date)
            val formattedDate = DateUtils.getFormattedDate(savedDate)
            val anotherFormattedDate = anotherFormatter.format(date)
            Log.e("WTF", "[LIVE DATA UPDATE] savedDate: $savedDate ($debugFormattedDate)")
            val start = UtilityFunctions.dateStringToMillis(anotherFormattedDate)
            val end = UtilityFunctions.dateStringToMillis(anotherFormattedDate)
            budgetViewModel.setReportsBetweenDates(start, end)
            currentDate = anotherFormattedDate
            binding.calendarView.date = savedDate
            binding.tvTodayDate.text = formattedDate
        }
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
        val sharedPrefs = requireContext().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val isPinEntered = sharedPrefs.getBoolean(PREFERENCE_IS_PIN_ENTERED_KEY, false)
        Log.e("WTF", "Check PIN: $isPinEntered")
        if (!isPinEntered) {
            findNavController().navigate(R.id.action_calendarFragment_to_pinFragment)
        }
    }
}