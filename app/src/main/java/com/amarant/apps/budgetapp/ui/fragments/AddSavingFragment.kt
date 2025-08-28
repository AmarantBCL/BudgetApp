package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentAddSavingBinding
import com.amarant.apps.budgetapp.ui.adapter.ColorPaletteAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.PiggyBankViewModel
import com.amarant.apps.budgetapp.util.DateUtils
import com.amarant.apps.budgetapp.util.KeyboardUtils.hideKeyboardFrom
import com.amarant.apps.budgetapp.util.MessageUtils
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.absoluteValue

@AndroidEntryPoint
class AddSavingFragment : Fragment() {

    private val args by navArgs<AddSavingFragmentArgs>()

    private var _binding: FragmentAddSavingBinding? = null
    private val binding: FragmentAddSavingBinding
        get() = _binding ?: throw RuntimeException("FragmentAddSavingBinding == null")

    private val piggyBankViewModel: PiggyBankViewModel by viewModels()

    private lateinit var colorPaletteAdapter: ColorPaletteAdapter
    private lateinit var autoCompleteTextView: AutoCompleteTextView

    private var dateSelection: Long = System.currentTimeMillis()

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
        piggyBankViewModel.initColorPalette()
        if (args.saving != null) {
            val saving = args.saving
            binding.editGoalName.setText(saving?.title)
            binding.editAmount.setText(saving?.target?.toInt()?.absoluteValue.toString())
            binding.editCurrency.setText(saving?.currency)
            dateSelection = saving?.dueTo ?: System.currentTimeMillis()
            binding.editTargetDate.setText(DateUtils.getFormattedDate(dateSelection))
            piggyBankViewModel.selectColorPalette(saving?.circleColor ?: R.color.positive_green)
            binding.btnAddEntry.text = getString(R.string.edit_target)
            binding.btnAddEntry.setIconResource(R.drawable.ic_edit)
        } else {
            binding.btnAddEntry.text = getString(R.string.add_target)
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
            binding.editCurrency.clearFocus()
            hideKeyboardFrom(binding.editCurrency)
        }
    }

    private fun observeViewModel() {
        piggyBankViewModel.colorPalette.observe(viewLifecycleOwner) {
            colorPaletteAdapter.submitList(it)
        }
    }

    private fun setClickListeners() {
        binding.editTargetDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_target_date))
                .setSelection(dateSelection)
//                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
            picker.show(requireActivity().supportFragmentManager, picker.toString())
            picker.addOnPositiveButtonClickListener { selection ->
                dateSelection = picker.selection ?: System.currentTimeMillis()
//                binding.editTargetDate.setText(picker.headerText)
                binding.editTargetDate.setText(DateUtils.getFormattedDate(selection))
            }
        }
        binding.btnAddEntry.setOnClickListener {
            val saving = args.saving
            val goalName = binding.editGoalName.text.toString()
            val amount = binding.editAmount.text.toString()
            val currency = binding.editCurrency.text.toString()
            val color = colorPaletteAdapter.currentList.find { it.isSelected }?.color
                ?: R.color.positive_green
            if (saving == null) {
                val wasAdded = piggyBankViewModel.tryToAddSaving(
                    goalName, amount, currency, dateSelection, color, 0
                )
                if (wasAdded) {
                    MessageUtils.showSnackbarMessage(
                        binding.btnAddEntry,
                        getString(R.string.added_new_goal),
                        getString(R.string.hide)
                    )
                    findNavController().popBackStack()
                } else {
                    MessageUtils.showSnackbarMessage(
                        binding.btnAddEntry,
                        getString(R.string.fill_in_all_fields),
                        getString(R.string.hide)
                    )
                }
            } else {
                val wasAdded = piggyBankViewModel.tryToAddSaving(
                    goalName, amount, currency, dateSelection, color, args.saving?.id ?: 0
                )
                if (wasAdded) {
                    MessageUtils.showSnackbarMessage(
                        binding.btnAddEntry,
                        getString(R.string.entry_edited),
                        getString(R.string.hide)
                    )
                    findNavController().popBackStack()
                } else {
                    MessageUtils.showSnackbarMessage(
                        binding.btnAddEntry,
                        getString(R.string.fill_in_all_fields),
                        getString(R.string.hide)
                    )
                }
            }
        }
    }
}