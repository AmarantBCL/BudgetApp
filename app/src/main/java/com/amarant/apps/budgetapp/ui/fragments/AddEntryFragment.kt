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
import com.amarant.apps.budgetapp.entities.HistoryItem
import com.amarant.apps.budgetapp.ui.adapter.QuickCategoriesAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.EntryViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.HistoryViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.ProfileViewModel
import com.amarant.apps.budgetapp.util.CategoryUtils.ALL_CATEGORIES
import com.amarant.apps.budgetapp.util.CategoryUtils.CATEGORY_MAPPING
import com.amarant.apps.budgetapp.util.DateUtils
import com.amarant.apps.budgetapp.util.MessageUtils
import dagger.hilt.android.AndroidEntryPoint

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

    private var selectedCategory = ""

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
        // TODO Debug navigation
        val navController = findNavController()
        Log.d("DebugNavController", "[CURRENT DEST] ${navController.currentDestination}")
        Log.e("DebugNavController", "[START DEST] ${navController.graph.startDestDisplayName}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() {
        val formattedDate = DateUtils.getFormattedDate(args.selectedDate)
        binding.lblAddEntry.text = getString(R.string.on_date, formattedDate)
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
            val selectedDate = args.selectedDate
            val wasEntrySubmitted = budgetViewModel.validateEntries(
                isDebit,
                amount,
                purpose,
                selectedDate,
                selectedCategory
            )
            if (wasEntrySubmitted) {
                saveHistory(purpose)
                updateCurrentBalance()
                MessageUtils.showSnackbarMessage(
                    binding.btnAddEntry,
                    getString(R.string.entry_added),
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
        val categoriesArr = resources.getStringArray(R.array.categories)
        budgetViewModel.getCategoryStats().observe(viewLifecycleOwner) {
            entryViewModel.initCategories(ALL_CATEGORIES.toTypedArray(), it)
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
            val categoryName = entryViewModel.getSelectedCategoryName()
            selectedCategory = categoryName
            binding.tvSelectedCategory.text = categoryName//categoriesArr[it]
            historyViewModel.switchHistoryCategory(CATEGORY_MAPPING[categoryName] ?: 0)
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
                CATEGORY_MAPPING[selectedCategory] ?: 0
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