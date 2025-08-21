package com.amarant.apps.budgetapp.ui.fragments.viewpager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentStepFiveBinding
import com.amarant.apps.budgetapp.ui.viewmodels.OnboardingViewModel

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
            binding.tvIncome.text = it
        }
        onboardingViewModel.currency.observe(viewLifecycleOwner) {
            binding.tvCurrency.text = it
        }
        onboardingViewModel.categories.observe(viewLifecycleOwner) { categories ->
            binding.tvCategories.text = "${categories.filter { it.isSelected }.size} Selected"
//            binding.tvCategories.text = "${categories.filter { it.isChecked }.size} Selected"
        }
    }
}