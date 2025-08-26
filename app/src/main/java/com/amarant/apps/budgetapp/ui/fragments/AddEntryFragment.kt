package com.amarant.apps.budgetapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentAddEntryBinding
import com.amarant.apps.budgetapp.entities.Category
import com.amarant.apps.budgetapp.entities.HistoryItem
import com.amarant.apps.budgetapp.ui.adapter.QuickCategoriesAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.EntryViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.HistoryViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.ProfileViewModel
import com.amarant.apps.budgetapp.util.Constants
import com.amarant.apps.budgetapp.util.DateUtils
import com.amarant.apps.budgetapp.util.MessageUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.absoluteValue

@AndroidEntryPoint
class AddEntryFragment : Fragment() {

    private val args by navArgs<AddEntryFragmentArgs>()

    private var _binding: FragmentAddEntryBinding? = null
    private val binding: FragmentAddEntryBinding
        get() = _binding ?: throw RuntimeException("FragmentAddEntryBinding == null")

    private val entryViewModel: EntryViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val budgetViewModel: BudgetViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()

    private lateinit var quickCategoriesAdapter: QuickCategoriesAdapter

    private var selectedCategory = Category.ALL

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        readHistory()
        setClickListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() {
        val formattedDate = DateUtils.getFormattedDate(args.selectedDate)
        binding.lblAddEntry.text = getString(R.string.on_date, formattedDate)
        args.budgetEntry?.let {
            binding.btnAddEntry.text = getString(R.string.edit_entry)
            binding.btnAddEntry.setIconResource(R.drawable.ic_edit)
            binding.btnExpense.isChecked = it.budget.creditOrDebit == Constants.DEBIT
            binding.btnIncome.isChecked = it.budget.creditOrDebit == Constants.CREDIT
            binding.tilAmount.editText?.setText(it.budget.amount.toInt().absoluteValue.toString())
            binding.tilName.editText?.setText(it.budget.purpose)
        }
        quickCategoriesAdapter = QuickCategoriesAdapter()
        binding.recyclerQuickCategories.adapter = quickCategoriesAdapter
        quickCategoriesAdapter.onCategoryClickListener = { name ->
            entryViewModel.selectCategory(name)
        }
    }

    private fun setClickListeners() {
        binding.lblShowMore.setOnClickListener {
            entryViewModel.changeExpandedState()
        }
        binding.btnAddEntry.setOnClickListener {
            val isDebit = binding.btnExpense.isChecked
            val amount = binding.tilAmount.editText?.text.toString()
            val purpose = binding.tilName.editText?.text.toString()
            val budgetEntry = args.budgetEntry
            var wasEntrySubmitted = false
            var message = getString(R.string.entry_added)
            if (budgetEntry == null) {
                val selectedDate = args.selectedDate
                wasEntrySubmitted = budgetViewModel.validateAndAddEntries(
                    isDebit,
                    amount,
                    purpose,
                    selectedDate,
                    selectedCategory
                )
            } else {
                val id = budgetEntry.budget.id ?: -1
                wasEntrySubmitted = budgetViewModel.validateAndEditEntries(
                    id,
                    isDebit,
                    amount,
                    purpose,
                    selectedCategory
                )
                message = getString(R.string.entry_edited)
            }
            submitEntry(wasEntrySubmitted, purpose, message)
        }
        binding.editName.onItemClickListener = AdapterView.OnItemClickListener {
                _, _, _, _ ->
                binding.editName.clearFocus()
                hideKeyboardFrom(binding.editName)
        }
        binding.editName.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboardFrom(textView)
                textView.clearFocus()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun hideKeyboardFrom(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun observeViewModel() {
        entryViewModel.getCategoryStats().observe(viewLifecycleOwner) { categories ->
//        entryViewModel.getCategoryStats().observe(viewLifecycleOwner) { categoryStats ->
//            entryViewModel.initCategories(Category.entries, categoryStats)
            Log.d("WTF", "getCategoryStats: $categories")
            entryViewModel.simpleInitCategories(categories)
            args.budgetEntry?.let {
                entryViewModel.selectCategory(it.budget.category)
            }
        }
        entryViewModel.categories.observe(viewLifecycleOwner) {
            quickCategoriesAdapter.submitList(it) {
                binding.scrollView.post {
                    val top = binding.lblCategory.top
                    binding.scrollView.smoothScrollTo(0, top)
                }
            }
        }
        entryViewModel.isExpanded.observe(viewLifecycleOwner) {
            binding.lblShowMore.text = if (it) getString(R.string.show_less) else getString(R.string.show_more)
        }
        entryViewModel.selectedCategory.observe(viewLifecycleOwner) {
            val category = entryViewModel.getSelectedCategoryName()
            selectedCategory = category
            binding.tvSelectedCategory.text = category.getLocalizedName(requireContext())
//            historyViewModel.switchHistoryCategory(CATEGORY_MAPPING[categoryName] ?: 0)
        }
        entryViewModel.selectedCategories.observe(viewLifecycleOwner) {
            Log.d("WTF", "Cats: $it")
        }
    }

    private fun submitEntry(wasEntrySubmitted: Boolean, purpose: String, message: String) {
        if (wasEntrySubmitted) {
            saveHistory(purpose)
            updateCurrentBalance()
            MessageUtils.showSnackbarMessage(
                binding.btnAddEntry,
                message,
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

    private fun updateCurrentBalance() {
        val revisedCurrentBalance = "0" // TODO Temp
        profileViewModel.updateCurrentBalance(revisedBalance = revisedCurrentBalance.toFloat())
    }

    private fun saveHistory(purpose: String) {
        historyViewModel.addHistory(
            HistoryItem(
                purpose,
                selectedCategory.ordinal
            )
        )
    }

    private fun readHistory() {
        historyViewModel.getAllHistory().observe(viewLifecycleOwner) { history ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                history.map { it.entry }
            )
            binding.editName.setAdapter(adapter)
        }
    }
}