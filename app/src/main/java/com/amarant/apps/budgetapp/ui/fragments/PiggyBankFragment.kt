package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentPiggyBankBinding
import com.amarant.apps.budgetapp.entities.PiggyBank
import com.amarant.apps.budgetapp.entities.Saving
import com.amarant.apps.budgetapp.ui.adapter.SavingsAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.PiggyBankViewModel
import com.amarant.apps.budgetapp.util.NumberUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

class PiggyBankFragment : Fragment() {

    private var _binding: FragmentPiggyBankBinding? = null
    private val binding: FragmentPiggyBankBinding
        get() = _binding ?: throw RuntimeException("FragmentPiggyBankBinding == null")

    private val piggyBankViewModel: PiggyBankViewModel by activityViewModels()

    private var piggyBankId = 0

    private lateinit var savingsAdapter: SavingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPiggyBankBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        activity?.title = getString(R.string.piggy_bank)
        initViews()
        observeViewModel()
        setClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.piggy_bank_menu, menu)
        val buttonItem = menu.findItem(R.id.action_button_item)
        val button = buttonItem.actionView?.findViewById<MaterialButton>(R.id.menu_button)
        button?.setOnClickListener {

        }
    }

    private fun initViews() {
        savingsAdapter = SavingsAdapter()
//        val items = listOf(
//            Saving(1, "Новый ноутбук", 40000f, 500f, "HRN ₴",
//                1767268800000L, R.color.positive_green),
//            Saving(2, "Выезд за кордон", 10000f, 8500f, "USD $",
//                1772366400000L, R.color.blue),
//            Saving(3, "Отдых", 20000f, 2000f, "HRN ₴",
//                1780346881000L, R.color.amber)
//        )
        binding.recyclerSavings.adapter = savingsAdapter
//        savingsAdapter.submitList(items)
//        val totalSaved = items.sumOf { it.saved.toDouble() }
//        val totalTarget = items.sumOf { it.target.toDouble() }
//        binding.tvTotalSaved.text = NumberUtils.formatNumberWithThousandsSeparator(NumberUtils.formatDecimal(totalSaved).toDouble())
//        binding.tvTotalTarget.text = NumberUtils.formatNumberWithThousandsSeparator(NumberUtils.formatDecimal(totalTarget).toDouble())
    }

    private fun observeViewModel() {
//        piggyBankViewModel.getPiggyBank().observe(viewLifecycleOwner) {
//            binding.editCurrencySaved.setText(it.currencySaved.toString())
//            binding.editHryvniaSaved.setText(it.hryvniaSaved.toString())
//            binding.editCurrencyTaken.setText(it.currencyTaken.toString())
//            binding.editHryvniaTaken.setText(it.hryvniaTaken.toString())
//            piggyBankId = it.id
//        }
    }

    private fun setClickListeners() {
//        binding.btnUpdateMoney.setOnClickListener {
//            if (binding.editCurrencySaved.text.toString()
//                    .isNotEmpty() && binding.editHryvniaSaved.text.toString()
//                    .isNotEmpty() && binding.editCurrencyTaken.text.toString()
//                    .isNotEmpty() && binding.editHryvniaTaken.text.toString().isNotEmpty()
//            ) {
//                val currencySaved = binding.editCurrencySaved.text.toString().toInt()
//                val hryvniaSaved = binding.editHryvniaSaved.text.toString().toInt()
//                val currencyTaken = binding.editCurrencyTaken.text.toString().toInt()
//                val hryvniaTaken = binding.editHryvniaTaken.text.toString().toInt()
//                val piggyBank =
//                    PiggyBank(piggyBankId, currencySaved, hryvniaSaved, currencyTaken, hryvniaTaken)
//                piggyBankViewModel.updatePiggyBank(piggyBank)
//                val snackbar = Snackbar.make(
//                    binding.piggyBankConstraint,
//                    getString(R.string.balance_updated),
//                    Snackbar.LENGTH_SHORT
//                )
//                snackbar.setAction(getString(R.string.hide)) {
//                    snackbar.dismiss()
//                }
//                snackbar.show()
//                findNavController().popBackStack()
//            } else {
//                Snackbar.make(
//                    binding.piggyBankConstraint,
//                    getString(R.string.field_must_not_be_empty),
//                    Snackbar.LENGTH_SHORT
//                ).show()
//            }
//        }
    }
}