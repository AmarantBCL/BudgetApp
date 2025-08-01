package com.amarant.apps.budgetapp.ui.fragments.viewpager

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.activityViewModels
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentStepThreeBinding
import com.amarant.apps.budgetapp.ui.viewmodels.OnboardingViewModel

class StepThreeFragment : Fragment() {

    private var _binding: FragmentStepThreeBinding? = null
    private val binding: FragmentStepThreeBinding
        get() = _binding ?: throw RuntimeException("FragmentStepThreeBinding == null")

    private val onboardingViewModel: OnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStepThreeBinding.inflate(inflater, container, false)
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
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.list_item_exposed_dropdown,
            resources.getStringArray(R.array.currency)
        )
        val autoCompleteTextView = (binding.tilCurrency.editText as? AutoCompleteTextView)
        autoCompleteTextView?.setAdapter(adapter)
        autoCompleteTextView?.onItemClickListener = AdapterView.OnItemClickListener {
                parent, view, position, id ->
            val selectedCurrency = parent.getItemAtPosition(position).toString()
            onboardingViewModel.setCurrency(selectedCurrency)
        }
        onboardingViewModel.currency.value?.let {
            autoCompleteTextView?.setText(it, false)
        }
        binding.editIncome.setText(onboardingViewModel.monthlyIncome.value)
        binding.editSavingsGoal.setText(onboardingViewModel.savingGoal.value)
    }

    private fun setTextWatchers() {
        binding.editIncome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                onboardingViewModel.setMonthlyIncome(editable.toString())
            }
        })
        binding.editSavingsGoal.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                onboardingViewModel.setSavingGoal(editable.toString())
            }
        })
    }
}