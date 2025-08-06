package com.amarant.apps.budgetapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.Orientation
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentCalendarBinding
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.entities.PiggyBank
import com.amarant.apps.budgetapp.ui.MainActivity
import com.amarant.apps.budgetapp.ui.adapter.TodayBudgetAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.PiggyBankViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.ProfileViewModel
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_IS_PIN_ENTERED_KEY
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_NAME
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_SHOW_ALL
import com.amarant.apps.budgetapp.util.UtilityFunctions
import com.amarant.apps.budgetapp.util.UtilityFunctions.dateMillisToString
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Locale.filter
import kotlin.math.absoluteValue
import kotlin.math.log

@AndroidEntryPoint
class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding: FragmentCalendarBinding
        get() = _binding ?: throw RuntimeException("FragmentCalendarBinding == null")

    private val profileViewModel: ProfileViewModel by viewModels()
    private val piggyBankViewModel: PiggyBankViewModel by viewModels()
    private val budgetViewModel: BudgetViewModel by activityViewModels()

    private lateinit var todayBudgetAdapter: TodayBudgetAdapter
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() {
        activity?.title = "Calendar"
        currentDate = getFormattedDate()
        val start = UtilityFunctions.dateStringToMillis(currentDate.toString())
        val end = UtilityFunctions.dateStringToMillis(currentDate.toString())
        budgetViewModel.setReportsBetweenDates(start, end)
//        Log.e("WTF", "### $currentDate")
//        Log.d("WTF", "### $start -> $end")
        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            val selectedDate = "$day/${month + 1}/$year"
            currentDate = selectedDate
            val start = UtilityFunctions.dateStringToMillis(selectedDate)
            val end = UtilityFunctions.dateStringToMillis("${day}/${month + 1}/$year")
//            Log.e("WTF", "$selectedDate")
//            Log.d("WTF", "$start -> $end")
            budgetViewModel.setReportsBetweenDates(start, end)
//            val action = CalendarFragmentDirections.actionCalendarFragmentToBudgetEntryFragment(selectedDate)
//            findNavController().navigate(action)
        }
        todayBudgetAdapter = TodayBudgetAdapter()
        val divider = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.recyclerEntries.addItemDecoration(divider)
        binding.recyclerEntries.adapter = todayBudgetAdapter
//        val entryOne = Budget(1, "2025-08-04", "Privatbank", -75f, "Morning coffee", "Debit", "Restaurants")
//        val entryTwo = Budget(2, "2025-08-04", "Privatbank", -208f, "Uber ride", "Debit", "Transportation")
//        val entryThree = Budget(3, "2025-08-04", "Privatbank", 25000f, "Salary", "Credit", "Cash")
//        val entryFour = Budget(4, "2025-08-04", "Privatbank", -855f, "Blood tests", "Debit", "Health")
//        val entryFive = Budget(5, "2025-08-04", "Privatbank", -9500f, "Apartment", "Debit", "Rent")
//        val entrySix = Budget(6, "2025-08-05", "Privatbank", -528f, "Supermarket", "Debit", "Groceries")
//        val entrySeven = Budget(7, "2025-08-05", "Privatbank", -791f, "Cyberpunk 2077", "Debit", "Entertainment")
//        val list = listOf<Budget>()//(entryOne, entryTwo, entryThree, entryFour, entryFive, entrySix, entrySeven)
//        todayBudgetAdapter.submitList(list)
//        binding.tvNumberOfEntries.text = "${list.size} entries"
//        if (list.isNotEmpty()) {
//            val totalIncome = list.filter { it.creditOrDebit == "Credit" }.sumOf { it.amount.toDouble() }
//            val totalExpenses = list.filter { it.creditOrDebit == "Debit" }.sumOf { it.amount.toDouble() }
//            val netIncome = totalIncome - totalExpenses.absoluteValue
//            binding.recyclerEntries.visibility = View.VISIBLE
//            binding.cardIncomeExpenses.visibility = View.VISIBLE
//            binding.tvEmptyEntries.visibility = View.GONE
//            binding.imgDollar.visibility = View.GONE
//            binding.tvIncome.text = "+$totalIncome"
//            binding.tvExpenses.text = totalExpenses.toString()
//            if (netIncome > 0) {
//                binding.tvNetIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.positive_green))
//                binding.tvNetIncome.text = "+$netIncome"
//            } else {
//                binding.tvNetIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.negative_red))
//                binding.tvNetIncome.text = netIncome.toString()
//            }
//        } else {
//            binding.recyclerEntries.visibility = View.GONE
//            binding.cardIncomeExpenses.visibility = View.GONE
//            binding.tvEmptyEntries.visibility = View.VISIBLE
//            binding.imgDollar.visibility = View.VISIBLE
//        }
        val toolbar = (requireActivity() as MainActivity).findViewById<MaterialToolbar>(R.id.toolbar)
        val button = toolbar.findViewById<Button>(R.id.btn_action)
        button.setOnClickListener {
            currentDate?.let {
                val action = CalendarFragmentDirections.actionCalendarFragmentToBudgetEntryFragment(it)
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
            todayBudgetAdapter.submitList(it)
            Log.d("WTF", "LiveData UPDATED")
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
                if (netIncome > 0) {
                    binding.tvNetIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.positive_green))
                    binding.tvNetIncome.text = "+$netIncome"
                } else {
                    binding.tvNetIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.negative_red))
                    binding.tvNetIncome.text = netIncome.toString()
                }
            } else {
                binding.recyclerEntries.visibility = View.GONE
                binding.cardIncomeExpenses.visibility = View.GONE
                binding.tvEmptyEntries.visibility = View.VISIBLE
                binding.imgDollar.visibility = View.VISIBLE
            }
        }
    }

    fun getFormattedDate(): String {
        // Create a SimpleDateFormat object with the desired pattern.
        // The Locale.US is used to ensure the format is consistent, as some locales might handle M/d/yyyy differently.
        val formatter = SimpleDateFormat("d/M/yyyy", Locale.US)

        // Get the current date.
        val currentDate = Date()

        // Format the date using the formatter.
        val formattedDate = formatter.format(currentDate)

        return formattedDate
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