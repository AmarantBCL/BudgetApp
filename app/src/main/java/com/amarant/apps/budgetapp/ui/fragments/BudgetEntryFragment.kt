package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentBudgetEntryBinding
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.entities.HistoryItem
import com.amarant.apps.budgetapp.ui.adapter.SpinnerAdapter
import com.amarant.apps.budgetapp.ui.adapter.SpinnerItem
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.HistoryViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.ProfileViewModel
import com.amarant.apps.budgetapp.util.CategoryUtils.ALL_CATEGORIES
import com.amarant.apps.budgetapp.util.Constants
import com.amarant.apps.budgetapp.util.UtilityFunctions.dateStringToMillis
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@Deprecated("Different fragment is used now")
@AndroidEntryPoint
class BudgetEntryFragment : Fragment() {

    private var _binding: FragmentBudgetEntryBinding? = null
    private val binding: FragmentBudgetEntryBinding
        get() = _binding ?: throw RuntimeException("FragmentBudgetEntryBinding == null")

    private val args: BudgetEntryFragmentArgs by navArgs()
    private val profileViewModel: ProfileViewModel by viewModels()
    private var currentBalance: Float = 0.0f
    private lateinit var bankName: String
    private var debitOrCredit = Constants.DEBIT
    private lateinit var remainingBalance: String
    private val budgetViewModel: BudgetViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()

    private val chipMap = mutableMapOf<Chip, Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.enter_budget_for, args.selectedDate)
        getProfileData()
        setSpinnerForDebitOrCredit()
        readHistory()
        setChips()
        binding.bankSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                bankName = parent?.getItemAtPosition(position).toString()

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                bankName = getString(R.string.none)
            }
        }
        binding.debitCreditSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val spinnerItem = parent?.getItemAtPosition(position) as SpinnerItem
                    val spinnerText = spinnerItem.text
                    debitOrCredit =
                        if (spinnerText == resources.getStringArray(R.array.debit_or_credit)[0]) {
                            Constants.DEBIT
                        } else {
                            Constants.CREDIT
                        }
                    calculatePreliminaryBalance(binding.editAmount.text.toString())
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
//                    debitOrCredit = "Debit"
                }
            }
        binding.editAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                calculatePreliminaryBalance(text.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
        binding.submitBudgetEntry.setOnClickListener {
            val amount = binding.editAmount.text.toString().trim()
            val purpose = binding.editPurpose.text.toString().trim()
            if (amount.isEmpty() || purpose.isEmpty()) {
                val snackbar = Snackbar.make(
                    binding.budgetEntryConstraint,
                    getString(R.string.fill_in_all_fields),
                    Snackbar.LENGTH_SHORT
                )
                snackbar.setAction(getString(R.string.hide)) {
                    snackbar.dismiss()
                }
                snackbar.show()
                return@setOnClickListener
            }
            val date = dateStringToMillis(args.selectedDate!!).toString()
            val revisedCurrentBalance = remainingBalance
            val checkedId = binding.chipGroup.checkedChipId
            val chip = requireView().findViewById<Chip>(checkedId)
            val category = ALL_CATEGORIES[chipMap[chip] ?: 0]
            submitBudgetEntryToDB(
                bankName,
                debitOrCredit,
                amount,
                purpose,
                date,
                revisedCurrentBalance,
                category
            )
            historyViewModel.addHistory(HistoryItem(purpose, chipMap[chip] ?: 0))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getProfileData() {
        profileViewModel.profileLiveData.observe(viewLifecycleOwner) {
            val bankNames = ArrayList<String>() // TODO Fix empty profile
            bankNames.add(it[0].bankName)
            bankName = it[0].bankName
            currentBalance = it[0].currentBalance
            binding.remainingBalance.text = it[0].currentBalance.toString()
            val arrayAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bankNames)
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.bankSpinner.adapter = arrayAdapter
        }
    }

    private fun setSpinnerForDebitOrCredit() {
        val spinnerItems = listOf(
            SpinnerItem(R.drawable.ic_debit, resources.getStringArray(R.array.debit_or_credit)[0]),
            SpinnerItem(R.drawable.ic_credit, resources.getStringArray(R.array.debit_or_credit)[1])
        )
        val arrayAdapter = SpinnerAdapter(requireContext(), spinnerItems)
        binding.debitCreditSpinner.adapter = arrayAdapter
    }

    private fun setChips() {
        for ((index, category) in ALL_CATEGORIES.withIndex()) {
            val chip = Chip(requireContext())
            chip.text = resources.getStringArray(R.array.categories)[index]
            val resId = resources.getIdentifier(
                "drawable/cat_${category.lowercase()}",
                "drawable",
                requireContext().packageName
            )
            chip.chipIcon = ContextCompat.getDrawable(requireContext(), resId)
            chip.isChipIconVisible = true
            binding.chipGroup.addView(chip)
            if (category == ALL_CATEGORIES[0]) {
                chip.isChecked = true
            }
            chipMap[chip] = index
        }
        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = binding.chipGroup.checkedChipId
            val chip = requireView().findViewById<Chip>(checkedId)
            historyViewModel.switchHistoryCategory(chipMap[chip] ?: 0)
        }
        historyViewModel.switchHistoryCategory(0)
    }

    private fun submitBudgetEntryToDB(
        bankName: String,
        debitOrCredit: String,
        amount: String,
        purpose: String,
        date: String,
        revisedCurrentBalance: String,
        category: String
    ) {
        var amountToInsert = amount.toFloat()
        if (debitOrCredit == Constants.DEBIT) {
            amountToInsert *= -1
        }
        budgetViewModel.insertBudget(
            Budget(
                date = date,
                bankName = bankName,
                amount = amountToInsert,
                purpose = purpose,
                creditOrDebit = debitOrCredit,
                category = category
            )
        )
        profileViewModel.updateCurrentBalance(revisedBalance = revisedCurrentBalance.toFloat())
        val snackbar = Snackbar.make(
            binding.budgetEntryConstraint,
            getString(R.string.entry_added), Snackbar.LENGTH_SHORT
        )
        snackbar.setAction(getString(R.string.hide)) {
            snackbar.dismiss()
        }
        snackbar.show()
        findNavController().popBackStack()
    }

    private fun calculatePreliminaryBalance(enteredAmount: String) {
        val amount = enteredAmount.ifEmpty { "0" }
        val temp = if (debitOrCredit == Constants.DEBIT) {
            (currentBalance - amount.toFloat())
        } else {
            (currentBalance + amount.toFloat())
        }
        remainingBalance = temp.toString()
        binding.remainingBalance.text = remainingBalance
    }

    private fun readHistory() {
        historyViewModel.getHistory().observe(viewLifecycleOwner) { history ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                history.map { it.entry }
            )
            binding.editPurpose.setAdapter(adapter)
        }
    }
}