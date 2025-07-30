package com.amarant.apps.budgetapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentCalendarBinding
import com.amarant.apps.budgetapp.entities.PiggyBank
import com.amarant.apps.budgetapp.ui.viewmodels.PiggyBankViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.ProfileViewModel
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_IS_PIN_ENTERED_KEY
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_NAME
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding: FragmentCalendarBinding
        get() = _binding ?: throw RuntimeException("FragmentCalendarBinding == null")

    private val profileViewModel: ProfileViewModel by viewModels()
    private val piggyBankViewModel: PiggyBankViewModel by viewModels()

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
        Log.d("WTF", "CalendarFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() {
        activity?.title = resources.getString(R.string.enter_budget)
        binding.calendarView.setOnDateChangeListener { _, year, month, day ->
            val selectedDate = "$day/${month + 1}/$year"
            val action = CalendarFragmentDirections.actionCalendarFragmentToBudgetEntryFragment(selectedDate)
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        profileViewModel.profileLiveData.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                piggyBankViewModel.updatePiggyBank(PiggyBank(
                    0,
                    0,
                    0,
                    0,
                    0)
                )
                findNavController().navigate(R.id.action_global_profileFragment, null, navOptions {
                    popUpTo(R.id.calendarFragment) {
                        inclusive = true
                    }
                })
            } else {
                checkPin()
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