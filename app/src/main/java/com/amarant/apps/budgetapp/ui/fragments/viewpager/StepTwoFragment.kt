package com.amarant.apps.budgetapp.ui.fragments.viewpager

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.amarant.apps.budgetapp.databinding.FragmentStepTwoBinding
import com.amarant.apps.budgetapp.ui.viewmodels.OnboardingViewModel

class StepTwoFragment : Fragment() {

    private var _binding: FragmentStepTwoBinding? = null
    private val binding: FragmentStepTwoBinding
        get() = _binding ?: throw RuntimeException("FragmentStepTwoBinding == null")

    private val onboardingViewModel: OnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStepTwoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setTextWatchers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() {
        binding.editFullName.setText(onboardingViewModel.fullName.value)
        binding.editEmail.setText(onboardingViewModel.emailAddress.value)
    }

    private fun setTextWatchers() {
        binding.editFullName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                onboardingViewModel.setFullName(editable.toString())
            }
        })
        binding.editEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                onboardingViewModel.setEmailAddress(editable.toString())
            }
        })
    }
}