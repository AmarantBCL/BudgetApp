package com.amarant.apps.budgetapp.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentAddSavingBinding
import com.amarant.apps.budgetapp.entities.ColorPalette
import com.amarant.apps.budgetapp.entities.Saving
import com.amarant.apps.budgetapp.ui.adapter.ColorPaletteAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.PiggyBankViewModel
import com.amarant.apps.budgetapp.util.KeyboardUtils
import com.amarant.apps.budgetapp.util.KeyboardUtils.hideKeyboardFrom
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText

class AddSavingFragment : Fragment() {

    private var _binding: FragmentAddSavingBinding? = null
    private val binding: FragmentAddSavingBinding
        get() = _binding ?: throw RuntimeException("FragmentAddSavingBinding == null")

    private val piggyBankViewModel: PiggyBankViewModel by activityViewModels()

    private lateinit var colorPaletteAdapter: ColorPaletteAdapter
    private lateinit var autoCompleteTextView: AutoCompleteTextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddSavingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initAutoCompleteTextViews()
        observeViewModel()
        setClickListeners()
    }

    private fun initViews() {
        colorPaletteAdapter = ColorPaletteAdapter()
        binding.recyclerColorPalette.adapter = colorPaletteAdapter
        colorPaletteAdapter.onColorClickListener = { color ->
            piggyBankViewModel.selectColorPalette(color)
            if (binding.editGoalName.isFocused) {
                binding.editGoalName.clearFocus()
                hideKeyboardFrom(binding.editGoalName)
            }
            if (binding.editAmount.isFocused) {
                binding.editAmount.clearFocus()
                hideKeyboardFrom(binding.editAmount)
            }
            if (binding.editCurrency.isFocused) {
                binding.editCurrency.clearFocus()
                hideKeyboardFrom(binding.editCurrency)
            }
            if (binding.editTargetDate.isFocused) {
                binding.editTargetDate.clearFocus()
                hideKeyboardFrom(binding.editTargetDate)
            }
        }
    }

    private fun initAutoCompleteTextViews() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.list_item_exposed_dropdown,
            resources.getStringArray(R.array.currency)
        )
        autoCompleteTextView = (binding.tilCurrency.editText as AutoCompleteTextView)
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener {
                parent, view, position, id ->
            val selectedCurrency = parent.getItemAtPosition(position).toString()

        }
    }

    private fun observeViewModel() {
        piggyBankViewModel.initColorPalette()
        piggyBankViewModel.colorPalette.observe(viewLifecycleOwner) {
            colorPaletteAdapter.submitList(it)
        }
    }

    private fun setClickListeners() {
        val etTargetDate = binding.editTargetDate
        etTargetDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_target_date))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
            picker.show(requireActivity().supportFragmentManager, picker.toString())
            picker.addOnPositiveButtonClickListener { selection ->
                etTargetDate.setText(picker.headerText) // красиво форматированная дата
            }
        }
    }
}