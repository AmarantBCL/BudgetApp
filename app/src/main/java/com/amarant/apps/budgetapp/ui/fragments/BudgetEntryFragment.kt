package com.amarant.apps.budgetapp.ui.fragments

import android.app.ProgressDialog.show
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentBudgetEntryBinding
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.ProfileViewModel
import com.amarant.apps.budgetapp.util.UtilityFunctions.dateStringToMillis
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BudgetEntryFragment : Fragment() {

    private var _binding: FragmentBudgetEntryBinding? = null
    private val binding: FragmentBudgetEntryBinding
        get() = _binding ?: throw RuntimeException("FragmentBudgetEntryBinding == null")

    private val args: BudgetEntryFragmentArgs by navArgs()
    private val profileViewModel: ProfileViewModel by viewModels()
    private var currentBalance: Float = 0.0f
    private lateinit var bankName: String
    private var debitOrCredit: String = "Debit"
    private lateinit var remainingBalance: String
    private val budgetViewModel: BudgetViewModel by viewModels()

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
        activity?.title = "Enter budget for: ${args.selectedDate}"
        getProfileData()
        setSpinnerForDebitOrCredit()
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
                bankName = "NONE"
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
                    debitOrCredit = parent?.getItemAtPosition(position).toString()
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
            val date = dateStringToMillis(args.selectedDate!!).toString()
            val revisedCurrentBalance = remainingBalance
            submitBudgetEntryToDB(
                bankName,
                debitOrCredit,
                amount,
                purpose,
                date,
                revisedCurrentBalance
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getProfileData() {
        profileViewModel.profileLiveData.observe(viewLifecycleOwner) {
            val bankNames = ArrayList<String>()
            bankNames.add(it[0].bankName)
            currentBalance = it[0].currentBalance
            binding.remainingBalance.text = it[0].currentBalance.toString()
            val arrayAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bankNames)
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.bankSpinner.adapter = arrayAdapter
        }
    }

    private fun setSpinnerForDebitOrCredit() {
        val debitOrCreditArray = ArrayList<String>()
        debitOrCreditArray.add("Debit")
        debitOrCreditArray.add("Credit")
        val arrayAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, debitOrCreditArray)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.debitCreditSpinner.adapter = arrayAdapter
    }

    private fun submitBudgetEntryToDB(
        bankName: String,
        debitOrCredit: String,
        amount: String,
        purpose: String,
        date: String,
        revisedCurrentBalance: String
    ) {
        var amountToInsert = amount.toFloat()
        if (debitOrCredit.equals("Debit")) {
            amountToInsert = -1 * amountToInsert
        }
        budgetViewModel.insertBudget(
            Budget(
                date = date,
                bankName = bankName,
                amount = amountToInsert,
                purpose = purpose,
                creditOrDebit = debitOrCredit
            )
        )
        profileViewModel.updateCurrentBalance(revisedBalance = revisedCurrentBalance.toFloat())
        val snackbar = Snackbar.make(binding.budgetEntryConstraint, "Entry added", Snackbar.LENGTH_SHORT)
        snackbar.setAction("Remove") {
            snackbar.dismiss()
        }
        snackbar.show()
//        findNavController().navigate(R.id.action_budgetEntryFragment_to_calendarFragment)
        findNavController().popBackStack()
    }

    private fun calculatePreliminaryBalance(enteredAmount: String) {
        val amount = enteredAmount.ifEmpty { "0" }
        val temp = if (debitOrCredit == "Debit") {
            (currentBalance - amount.toFloat())
        } else {
            (currentBalance + amount.toFloat())
        }
        remainingBalance = temp.toString()
        binding.remainingBalance.text = remainingBalance
    }
}