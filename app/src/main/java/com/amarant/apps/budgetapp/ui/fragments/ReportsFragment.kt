package com.amarant.apps.budgetapp.ui.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentReportsBinding
import com.amarant.apps.budgetapp.entities.BudgetUI
import com.amarant.apps.budgetapp.entities.Category
import com.amarant.apps.budgetapp.entities.ReportsItem
import com.amarant.apps.budgetapp.ui.adapter.ReportsAdapter
import com.amarant.apps.budgetapp.ui.adapter.decorations.CustomDividerDecoration
import com.amarant.apps.budgetapp.ui.fragments.bottomsheet.FiltersBottomSheetFragment
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.util.Constants
import com.amarant.apps.budgetapp.util.NumberUtils
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_THIS_MONTH
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.toColorInt
import androidx.fragment.app.viewModels
import com.amarant.apps.budgetapp.entities.ReportType
import com.amarant.apps.budgetapp.entities.SortOrder
import com.amarant.apps.budgetapp.ui.fragments.bottomsheet.PeriodBottomSheetFragment
import com.amarant.apps.budgetapp.ui.fragments.bottomsheet.ReportTypeBottomSheetFragment
import com.amarant.apps.budgetapp.ui.fragments.bottomsheet.SortingBottomSheetFragment
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_CUSTOM
import com.amarant.apps.budgetapp.util.PeriodUtils.PERIOD_SHOW_ALL
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

@AndroidEntryPoint
class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding: FragmentReportsBinding
        get() = _binding ?: throw RuntimeException("FragmentReportsBinding == null")

    private val budgetViewModel: BudgetViewModel by activityViewModels()

    private lateinit var reportsAdapter: ReportsAdapter

    private var reportsMenu: Menu? = null
    private var searchItem: MenuItem? = null
//    private var filterItem: MenuItem? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        initRecyclerView()
        setSpinnerValues()
        observeViewModel()
        setClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reports_menu, menu)
        reportsMenu = menu
        searchItem = menu.findItem(R.id.search)
//        filterItem = menu.findItem(R.id.filters)
        val searchItem = menu.findItem(R.id.search)
        val searchView = searchItem?.actionView as? SearchView
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { budgetViewModel.setSearchQuery(it) }
                searchView.clearFocus()
                searchItem.collapseActionView()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
//        updateSearchIcon(budgetViewModel.searchQuery.value ?: "")
//        updateFilterIcon(budgetViewModel.appliedFilter.value ?: Category.ALL)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item = menu.findItem(R.id.sort)
        item?.isVisible = !budgetViewModel.searchQuery.value.isNullOrEmpty()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.filters) {
//            val bottomSheet = FiltersBottomSheetFragment.newInstance()
//            bottomSheet.show(requireActivity().supportFragmentManager, FiltersBottomSheetFragment.TAG)
//        }
        if (item.itemId == R.id.sort) {
            budgetViewModel.setSearchQuery("")
        }
        return true
    }

    private fun initRecyclerView() {
        reportsAdapter = ReportsAdapter()
        val customDivider = ContextCompat.getDrawable(requireContext(), R.drawable.divider)
        if (customDivider != null) {
            binding.recyclerReports.addItemDecoration(CustomDividerDecoration(customDivider))
        } else {
            val defaultDivider = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            binding.recyclerReports.addItemDecoration(defaultDivider)
        }
        binding.recyclerReports.adapter = reportsAdapter
        reportsAdapter.onItemClickListener = {
            budgetViewModel.toggleSelection(it.budget.id ?: -1)
        }
        reportsAdapter.onItemLongClickListener = {
            val action = ReportsFragmentDirections.actionReportsFragmentToFragmentAddEntry(
                selectedDate = it.budget.date.toLong(),
                budgetEntry = it
            )
            findNavController().navigate(action)
        }
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val budget = reportsAdapter.currentList[position]
                if (budget is ReportsItem.Entry) {
                    budgetViewModel.deleteEntry(budget.entry.budget)
                    Snackbar.make(requireView(), getString(R.string.item_deleted), Snackbar.LENGTH_LONG).apply {
                        setAction(getString(R.string.undo)) {
                            budgetViewModel.insertBudget(budget.entry.budget)
                        }
                        show()
                    }
                }
            }

            override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                return if (viewHolder.itemViewType == ReportsAdapter.VIEW_TYPE_DATE) {
                    0
                } else {
                    super.getSwipeDirs(recyclerView, viewHolder)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerReports)
    }

    private fun observeViewModel() {
//        budgetViewModel.changeDateRange(PERIOD_THIS_MONTH)
        budgetViewModel.groupedEntries.observe(viewLifecycleOwner) { groupedReports ->
            reportsAdapter.submitList(groupedReports)
        }
        budgetViewModel.getBudgetEntriesBetweenDates().observe(viewLifecycleOwner) { reports ->
            setIncomeExpensesViews(reports)
            binding.imgDollar.visibility = if (reports.isNotEmpty()) View.GONE else View.VISIBLE
            binding.lblEmptyEntries.visibility = if (reports.isNotEmpty()) View.GONE else View.VISIBLE
        }
        budgetViewModel.searchQuery.observe(viewLifecycleOwner) {
//            updateSearchIcon(it)
            if (it.isNotEmpty()) {
                activity?.title = it
                requireActivity().invalidateOptionsMenu()
            } else {
                activity?.title = getString(R.string.spending_reports)
                requireActivity().invalidateOptionsMenu()
            }
        }
        budgetViewModel.appliedFilter.observe(viewLifecycleOwner) {
//            updateFilterIcon(it)
            binding.chipFilter.chipIcon = ContextCompat.getDrawable(requireContext(), it.rawIconRes)
            binding.chipFilter.text = it.getLocalizedName(requireContext())
            if (it != Category.ALL) {
                binding.chipFilter.setChipBackgroundColorResource(R.color.background_dark_purple)
            } else {
                binding.chipFilter.setChipBackgroundColorResource(R.color.card_background)
            }
        }
        budgetViewModel.period.observe(viewLifecycleOwner) {
            binding.chipPeriod.text = resources.getStringArray(R.array.periods)[it]
            if (it != PERIOD_SHOW_ALL) {
                binding.chipPeriod.setChipBackgroundColorResource(R.color.background_dark_purple)
            } else {
                binding.chipPeriod.setChipBackgroundColorResource(R.color.card_background)
            }
        }
        budgetViewModel.customRangeText.observe(viewLifecycleOwner) {
            if (it != "") {
                binding.chipPeriod.text = it
            }
        }
//        budgetViewModel.sorting.observe(viewLifecycleOwner) {
//            val sortArr = resources.getStringArray(R.array.sorting)
//            binding.chipSort.text = sortArr[it.field.ordinal]
//            val iconRes = if (it.order == SortOrder.DESC) R.drawable.ic_desc else R.drawable.ic_asc
//            val drawable = ContextCompat.getDrawable(requireContext(), iconRes)
//            binding.chipSort.closeIcon = drawable
//        }
        budgetViewModel.reportType.observe(viewLifecycleOwner) {
            val typeArr = resources.getStringArray(R.array.report_types)
            binding.chipType.text = typeArr[it.ordinal]
            val iconRes = when(it) {
                ReportType.INCOME -> R.drawable.ic_trend
                ReportType.EXPENSE -> R.drawable.ic_expenses
                else -> R.drawable.ic_loop
            }
            val drawable = ContextCompat.getDrawable(requireContext(), iconRes)
            binding.chipType.chipIcon = drawable
            if (it != ReportType.ALL) {
                binding.chipType.setChipBackgroundColorResource(R.color.background_dark_purple)
            } else {
                binding.chipType.setChipBackgroundColorResource(R.color.card_background)
            }
        }
    }

//    private fun updateSearchIcon(query: String) {
//        if (query.isNotEmpty()) {
//            searchItem?.icon?.setTintList(ColorStateList.valueOf("#793CC9".toColorInt()))
//        } else {
//            searchItem?.icon?.setTintList(null)
//        }
//    }

//    private fun updateFilterIcon(filter: Category) {
//        if (filter != Category.ALL) {
//            filterItem?.icon?.setTintList(ColorStateList.valueOf("#793CC9".toColorInt()))
//        } else {
//            filterItem?.icon?.setTintList(null)
//        }
//    }

    private fun setIncomeExpensesViews(reports: List<BudgetUI>) {
        val totalIncome = reports.filter { !it.isHidden && it.budget.creditOrDebit == Constants.CREDIT }.sumOf {
            it.budget.amount.toDouble()
        }
        val totalExpenses = reports.filter { !it.isHidden && it.budget.creditOrDebit == Constants.DEBIT }.sumOf {
            it.budget.amount.toDouble()
        }
        val isHiddenIncome =
            reports.any { it.isHidden && it.budget.creditOrDebit == Constants.CREDIT }
        val isHiddenExpenses =
            reports.any { it.isHidden && it.budget.creditOrDebit == Constants.DEBIT }
        val formattedIncome = NumberUtils.formatNumberWithThousandsSeparator(totalIncome)
        val formattedExpenses = NumberUtils.formatNumberWithThousandsSeparator(totalExpenses)
        binding.tvIncome.text =
            if (totalIncome > 0) getString(R.string.plus_placeholder, formattedIncome) else formattedIncome
        binding.tvExpenses.text = formattedExpenses
        setHiddenIncomeExpensesViews(isHiddenIncome, isHiddenExpenses)
    }

    private fun setHiddenIncomeExpensesViews(isHiddenIncome: Boolean, isHiddenExpenses: Boolean) {
        val spanIncome = SpannableString("${getString(R.string.income)} ⬤")
        val spanExpenses = SpannableString("${getString(R.string.expenses)} ⬤")
        spanIncome.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.accent_purple)),
            spanIncome.length - 1, spanIncome.length, SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spanExpenses.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.accent_purple)),
            spanExpenses.length - 1, spanExpenses.length, SPAN_EXCLUSIVE_EXCLUSIVE
        )
        if (isHiddenIncome) {
            binding.lblIncome.text = spanIncome
        } else {
            binding.lblIncome.text = getString(R.string.income)
        }
        if (isHiddenExpenses) {
            binding.lblExpenses.text = spanExpenses
        } else {
            binding.lblExpenses.text = getString(R.string.expenses)
        }
    }

    private fun setClickListeners() {
//        binding.spinnerDateRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                budgetViewModel.changeDateRange(position)
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//
//            }
//        }
//        binding.spinnerDateRange.setSelection(PERIOD_THIS_MONTH)
        binding.chipFilter.setOnClickListener {
            val bottomSheet = FiltersBottomSheetFragment.newInstance()
            bottomSheet.show(requireActivity().supportFragmentManager, FiltersBottomSheetFragment.TAG)
        }
        binding.chipPeriod.setOnClickListener {
            val bottomSheet = PeriodBottomSheetFragment.newInstance()
            bottomSheet.show(requireActivity().supportFragmentManager, PeriodBottomSheetFragment.TAG)
        }
//        binding.chipSort.setOnClickListener {
//            val bottomSheet = SortingBottomSheetFragment.newInstance()
//            bottomSheet.show(requireActivity().supportFragmentManager, SortingBottomSheetFragment.TAG)
//        }
        binding.chipType.setOnClickListener {
            val bottomSheet = ReportTypeBottomSheetFragment.newInstance()
            bottomSheet.show(requireActivity().supportFragmentManager, ReportTypeBottomSheetFragment.TAG)
        }
    }

    private fun setSpinnerValues() {
        val dateRangeArray = resources.getStringArray(R.array.periods)
        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dateRangeArray)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDateRange.adapter = arrayAdapter
    }
}