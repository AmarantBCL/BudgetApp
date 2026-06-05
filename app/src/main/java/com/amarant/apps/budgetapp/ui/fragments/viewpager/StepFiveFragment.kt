package com.amarant.apps.budgetapp.ui.fragments.viewpager

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentStepFiveBinding
import com.amarant.apps.budgetapp.ui.viewmodels.OnboardingViewModel
import com.amarant.apps.budgetapp.util.NumberUtils
import com.amarant.apps.budgetapp.util.NumberUtils.formatNumberWithThousandsSeparator
import java.util.Locale

class StepFiveFragment : Fragment() {

    private var _binding: FragmentStepFiveBinding? = null
    private val binding: FragmentStepFiveBinding
        get() = _binding ?: throw RuntimeException("FragmentStepFiveBinding == null")

    private val onboardingViewModel: OnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStepFiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        onboardingViewModel.fullName.observe(viewLifecycleOwner) {
            binding.tvName.text = it
        }
        onboardingViewModel.monthlyIncome.observe(viewLifecycleOwner) {
            var number = getString(R.string.not_set)
            if (!it.isNullOrEmpty()) {
                number = String.format(Locale.getDefault(), "%s", formatNumberWithThousandsSeparator(it.toDouble()))
            }
            binding.tvIncome.text = number
        }
        onboardingViewModel.currency.observe(viewLifecycleOwner) {
            binding.tvCurrency.text = it
        }
        onboardingViewModel.categories.observe(viewLifecycleOwner) { categories ->
            binding.tvCategories.text = "${categories.filter { it.isSelected }.size} Selected"
//            binding.tvCategories.text = "${categories.filter { it.isChecked }.size} Selected"
        }
        onboardingViewModel.savingGoal.observe(viewLifecycleOwner) {
            var number = "Not Set"
            if (!it.isNullOrEmpty()) {
                number = String.format(Locale.getDefault(), "%s", formatNumberWithThousandsSeparator(it.toDouble()))
            }
            binding.tvCategories.text = number
        }
    }
}