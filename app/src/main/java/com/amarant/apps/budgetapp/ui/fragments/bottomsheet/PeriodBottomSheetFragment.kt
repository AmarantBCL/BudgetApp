package com.amarant.apps.budgetapp.ui.fragments.bottomsheet

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.BottomSheetPeriodBinding
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.util.MessageUtils
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_CUSTOM
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class PeriodBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPeriodBinding? = null
    private val binding: BottomSheetPeriodBinding
        get() = _binding ?: throw RuntimeException("BottomSheetPeriodBinding == null")

    private val budgetViewModel: BudgetViewModel by activityViewModels()

    private var selectedPeriod = 0
    private var customStartDate = 0L
    private var customEndDate = 0L
    private var customRangeText = ""
    private var isCustomDateSet = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPeriodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeViewModel()
        setClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() {
        val periods = resources.getStringArray(R.array.periods)
        for ((index, period) in periods.withIndex()) {
            val chip = if (index == CUSTOM_DATE_RANGE_ID) {
                val customChip = createChip(requireContext(), period)
                customChip.setOnClickListener {
                    initDateRangePicker(customChip)
                }
                customChip
            } else {
                createChip(requireContext(), period)
            }
            binding.chipGroupPeriod.addView(chip)
        }
    }

    private fun observeViewModel() {
        budgetViewModel.period.observe(viewLifecycleOwner) {
            val chip = binding.chipGroupPeriod.getChildAt(it) as Chip
            chip.isChecked = true
            selectedPeriod = it
            binding.tvSelectedPeriod.text = chip.text
        }
        budgetViewModel.customRangeText.observe(viewLifecycleOwner) {
            if (it != "") {
                val chip = binding.chipGroupPeriod.getChildAt(CUSTOM_DATE_RANGE_ID) as Chip
                isCustomDateSet = true
                chip.text = it
            }
            customRangeText = it
        }
        budgetViewModel.dateRange.observe(viewLifecycleOwner) {
//            Log.d("WTF", "dateRange: $it")
            customStartDate = it.first
            customEndDate = it.second
        }
    }

    private fun setClickListeners() {
        binding.chipGroupPeriod.setOnCheckedStateChangeListener { group, checkedIds ->
            val selectedChip = group.findViewById<Chip>(checkedIds.first())
            val selectedOption = selectedChip.text.toString()
            selectedPeriod = group.indexOfChild(selectedChip)
            binding.tvSelectedPeriod.text = selectedOption
        }
        binding.btnApplyFilters.setOnClickListener {
//            Log.d("WTF", "start: $customStartDate, end: $customEndDate")
            if (selectedPeriod == CUSTOM_DATE_RANGE_ID) {
//                val defaultText = resources.getStringArray(R.array.periods)[CUSTOM_DATE_RANGE_ID]
                if (!isCustomDateSet) {
//                if (customRangeText.isEmpty() || customRangeText == defaultText) {
                    MessageUtils.showSnackbarMessage(
                        binding.root,
                        getString(R.string.select_range),
                        getString(R.string.hide)
                    )
                    return@setOnClickListener
                }
                budgetViewModel.changeDateRange(selectedPeriod, isPeriodOnly = true)
                budgetViewModel.setReportsBetweenDates(customStartDate, customEndDate)
                budgetViewModel.setCustomRangeDisplayedText(customRangeText)
            } else {
                budgetViewModel.changeDateRange(selectedPeriod)
                budgetViewModel.setCustomRangeDisplayedText("")
            }
            dialog?.dismiss()
        }
    }

    private fun createChip(context: Context, text: String): Chip {
        val chip = Chip(context)
        val textColor = ContextCompat.getColorStateList(context, R.color.primary_white)
        val backgroundColor = ContextCompat.getColorStateList(context, R.color.chip_background_color_selector)
        chip.text = text
        chip.setTextColor(textColor)
        chip.chipBackgroundColor = backgroundColor
        chip.iconStartPadding = 4f
        return chip
    }

    private fun initDateRangePicker(chip: Chip) {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(getString(R.string.select_range))
            .build()
        picker.addOnPositiveButtonClickListener { selection ->
            customStartDate = selection.first
            customEndDate = selection.second
            val startText = SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(customStartDate))
            val endText = SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(customEndDate))
            customRangeText = "$startText - $endText"
            isCustomDateSet = true
            chip.text = customRangeText
        }
//        picker.addOnNegativeButtonClickListener {
//            chip.text = resources.getStringArray(R.array.periods)[CUSTOM_DATE_RANGE_ID]
//        }
        picker.show(requireActivity().supportFragmentManager, "DATE_RANGE_PICKER")
    }

    companion object {

        private const val CUSTOM_DATE_RANGE_ID = PERIOD_CUSTOM

        const val TAG = "PeriodBottomSheet"

        fun newInstance(): PeriodBottomSheetFragment {
            return PeriodBottomSheetFragment()
        }
    }
}