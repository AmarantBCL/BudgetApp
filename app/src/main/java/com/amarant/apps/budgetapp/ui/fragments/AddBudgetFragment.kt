package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentAddBudgetBinding
import com.amarant.apps.budgetapp.entities.Category
import com.amarant.apps.budgetapp.entities.CategoryBudget
import com.amarant.apps.budgetapp.entities.QuickCategoryItem
import com.amarant.apps.budgetapp.ui.adapter.QuickCategoriesAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetPlanningViewModel
import com.amarant.apps.budgetapp.util.KeyboardUtils.hideKeyboardFrom
import com.amarant.apps.budgetapp.util.MessageUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddBudgetFragment : Fragment() {

    private val args by navArgs<AddBudgetFragmentArgs>()

    private var _binding: FragmentAddBudgetBinding? = null
    private val binding: FragmentAddBudgetBinding
        get() = _binding ?: throw RuntimeException("FragmentAddBudgetBinding == null")

    private val budgetPlanningViewModel: BudgetPlanningViewModel by viewModels()

    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var categoriesAdapter: QuickCategoriesAdapter

    private var selectedCategory = Category.GROCERIES

    // Non-localized values for the database
    private val periodDbValues = listOf("Weekly", "Monthly")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initAutoCompleteTextViews()
        initCategoriesRecyclerView()
        setClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() {
        args.budget?.let {
            binding.editBudgetName.setText(it.category.getLocalizedName(requireContext()))
            binding.editLimitAmount.setText(it.amountLimit.toString())
            selectedCategory = it.category
            
            // Set selection for period from DB (English) to Localized
            val periods = resources.getStringArray(R.array.budget_periods)
            val periodIndex = periodDbValues.indexOf(it.period)
            if (periodIndex != -1 && periodIndex < periods.size) {
                binding.editPeriod.setText(periods[periodIndex], false)
            }
            binding.btnAddEntry.text = getString(R.string.edit_target)
            binding.btnAddEntry.setIconResource(R.drawable.ic_edit)
        }
    }

    private fun initCategoriesRecyclerView() {
        categoriesAdapter = QuickCategoriesAdapter()
        binding.recyclerCategories.adapter = categoriesAdapter
        
        val categories = Category.entries.filter { it != Category.ALL }.map { 
            QuickCategoryItem(it, isSelected = it == selectedCategory) 
        }
        categoriesAdapter.submitList(categories)

        categoriesAdapter.onCategoryClickListener = { category ->
            selectedCategory = category
            val updatedList = categoriesAdapter.currentList.map { 
                it.copy(isSelected = it.category == category) 
            }
            categoriesAdapter.submitList(updatedList)
        }
    }

    private fun setClickListeners() {
        binding.btnAddEntry.setOnClickListener {
            val limit = binding.editLimitAmount.text.toString().toDoubleOrNull()
            val periodText = binding.editPeriod.text.toString()
            
            val periods = resources.getStringArray(R.array.budget_periods)
            val periodIndex = periods.indexOf(periodText)
            
            if (limit != null && periodIndex != -1) {
                // Map localized period back to English for DB
                val dbPeriod = periodDbValues[periodIndex]
                
                val budget = CategoryBudget(
                    id = args.budget?.id ?: 0,
                    category = selectedCategory,
                    amountLimit = limit,
                    period = dbPeriod,
                    startDate = args.budget?.startDate ?: System.currentTimeMillis()
                )
                budgetPlanningViewModel.insertBudget(budget)
                MessageUtils.showSnackbarMessage(
                    binding.btnAddEntry,
                    if (args.budget == null) getString(R.string.budget_added) else getString(R.string.budget_updated),
                    getString(R.string.hide)
                )
                findNavController().popBackStack()
            } else {
                MessageUtils.showSnackbarMessage(
                    binding.btnAddEntry,
                    getString(R.string.complete_all_required_fields),
                    getString(R.string.hide)
                )
            }
        }
    }

    private fun initAutoCompleteTextViews() {
        val periods = resources.getStringArray(R.array.budget_periods)
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.list_item_exposed_dropdown,
            periods
        )
        autoCompleteTextView = (binding.tilPeriod.editText as AutoCompleteTextView)
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener {
                _, _, _, _ ->
            binding.editPeriod.clearFocus()
            hideKeyboardFrom(binding.editPeriod)
        }
    }
}
