package com.amarant.apps.budgetapp.ui.fragments.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.BottomSheetReportTypeBinding
import com.amarant.apps.budgetapp.entities.ReportType
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.StatsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ReportTypeBottomSheetFragment: BottomSheetDialogFragment() {

    private var _binding: BottomSheetReportTypeBinding? = null
    private val binding: BottomSheetReportTypeBinding
        get() = _binding ?: throw RuntimeException("BottomSheetReportTypeBinding == null")

    private val budgetViewModel: BudgetViewModel by activityViewModels()
    private val statsViewModel: StatsViewModel by activityViewModels()

    private var reportType = ReportType.ALL
    private var isStats = false

    var reportTypeSelectionListener: ReportTypeSelectionListener? = null

    interface ReportTypeSelectionListener {
        fun onTypeSelected(type: ReportType)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isStats = requireArguments().getBoolean(KEY_IS_STATS)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetReportTypeBinding.inflate(inflater, container, false)
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
        binding.radioAll.visibility = if (isStats) View.GONE else View.VISIBLE
    }

    private fun observeViewModel() {
        if (isStats) {
            statsViewModel.reportType.observe(viewLifecycleOwner) {
                when (it) {
                    ReportType.INCOME -> binding.radioIncome.isChecked = true
                    ReportType.EXPENSE -> binding.radioExpenses.isChecked = true
                    else -> binding.radioAll.isChecked = true
                }
                reportType = it
                initSelectedReportType()
            }
        } else {
            budgetViewModel.reportType.observe(viewLifecycleOwner) {
                when (it) {
                    ReportType.INCOME -> binding.radioIncome.isChecked = true
                    ReportType.EXPENSE -> binding.radioExpenses.isChecked = true
                    else -> binding.radioAll.isChecked = true
                }
                reportType = it
                initSelectedReportType()
            }
        }
    }

    private fun setClickListeners() {
        binding.radioGroupReportType.setOnCheckedChangeListener { radioGroup, i ->
            reportType = when(i) {
                R.id.radio_all -> ReportType.ALL
                R.id.radio_income -> ReportType.INCOME
                R.id.radio_expenses -> ReportType.EXPENSE
                else -> ReportType.ALL
            }
            initSelectedReportType()
        }
        binding.btnApplyFilters.setOnClickListener {
//            budgetViewModel.setType(reportType)
            reportTypeSelectionListener?.onTypeSelected(reportType)
            dialog?.dismiss()
        }
    }

    private fun initSelectedReportType() {
        val typeArr = resources.getStringArray(R.array.report_types)
        val iconRes = when(reportType) {
            ReportType.INCOME -> R.drawable.ic_trend
            ReportType.EXPENSE -> R.drawable.ic_expenses
            else -> R.drawable.ic_loop
        }
        val drawable = ContextCompat.getDrawable(requireContext(), iconRes)
        binding.tvReportType.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        binding.tvReportType.text = typeArr[reportType.ordinal]
    }

    companion object {

        const val TAG = "ReportTypeBottomSheet"

        private const val KEY_IS_STATS = "stats"

        fun newInstance(isStats: Boolean): ReportTypeBottomSheetFragment {
            return ReportTypeBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(KEY_IS_STATS, isStats)
                }
            }
        }
    }
}