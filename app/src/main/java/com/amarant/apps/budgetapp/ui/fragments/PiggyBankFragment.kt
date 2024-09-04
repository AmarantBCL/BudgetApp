package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentPiggyBankBinding
import com.amarant.apps.budgetapp.entities.PiggyBank
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.PiggyBankViewModel
import com.google.android.material.snackbar.Snackbar

class PiggyBankFragment : Fragment() {

    private var _binding: FragmentPiggyBankBinding? = null
    private val binding: FragmentPiggyBankBinding
        get() = _binding ?: throw RuntimeException("FragmentPiggyBankBinding == null")

    private val piggyBankViewModel: PiggyBankViewModel by activityViewModels()

    private var piggyBankId = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPiggyBankBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.piggy_bank)
        observeViewModel()
        setClickListeners()
    }

    private fun observeViewModel() {
        piggyBankViewModel.getPiggyBank().observe(viewLifecycleOwner) {
            binding.editCurrencySaved.setText(it.currencySaved.toString())
            binding.editHryvniaSaved.setText(it.hryvniaSaved.toString())
            binding.editCurrencyTaken.setText(it.currencyTaken.toString())
            binding.editHryvniaTaken.setText(it.hryvniaTaken.toString())
            piggyBankId = it.id
        }
    }

    private fun setClickListeners() {
        binding.btnUpdateMoney.setOnClickListener {
            val currencySaved = binding.editCurrencySaved.text.toString().toInt()
            val hryvniaSaved = binding.editHryvniaSaved.text.toString().toInt()
            val currencyTaken = binding.editCurrencyTaken.text.toString().toInt()
            val hryvniaTaken = binding.editHryvniaTaken.text.toString().toInt()
            val piggyBank = PiggyBank(piggyBankId, currencySaved, hryvniaSaved, currencyTaken, hryvniaTaken)
            piggyBankViewModel.updatePiggyBank(piggyBank)
            val snackbar = Snackbar.make(
                binding.piggyBankConstraint,
                getString(R.string.balance_updated),
                Snackbar.LENGTH_SHORT
            )
            snackbar.setAction(getString(R.string.hide)) {
                snackbar.dismiss()
            }
            snackbar.show()
            findNavController().popBackStack()
        }
    }
}