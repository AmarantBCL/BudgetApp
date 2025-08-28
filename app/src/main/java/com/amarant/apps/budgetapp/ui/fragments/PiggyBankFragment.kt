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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentPiggyBankBinding
import com.amarant.apps.budgetapp.entities.PiggyBank
import com.amarant.apps.budgetapp.entities.ReportsItem
import com.amarant.apps.budgetapp.entities.Saving
import com.amarant.apps.budgetapp.ui.adapter.ReportsAdapter
import com.amarant.apps.budgetapp.ui.adapter.SavingsAdapter
import com.amarant.apps.budgetapp.ui.fragments.bottomsheet.UpdateSavingBottomSheetFragment
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.PiggyBankViewModel
import com.amarant.apps.budgetapp.util.NumberUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

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
            val action = PiggyBankFragmentDirections.actionPiggyBankFragmentToAddSavingFragment(null)
            findNavController().navigate(action)
        }
    }

    private fun initViews() {
        savingsAdapter = SavingsAdapter()
        binding.recyclerSavings.adapter = savingsAdapter
        savingsAdapter.onSavingLongClickListener = {
            val action = PiggyBankFragmentDirections.actionPiggyBankFragmentToAddSavingFragment(it)
            findNavController().navigate(action)
        }
        savingsAdapter.onSavingAddClickListener = {
            val bottomSheetDialog = UpdateSavingBottomSheetFragment.newInstance(it)
            bottomSheetDialog.show(requireActivity().supportFragmentManager, UpdateSavingBottomSheetFragment.TAG)
        }
        savingsAdapter.onSavingSubtractClickListener = {
            val bottomSheetDialog = UpdateSavingBottomSheetFragment.newInstance(it, isSubtract = true)
            bottomSheetDialog.show(requireActivity().supportFragmentManager, UpdateSavingBottomSheetFragment.TAG)
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
                val saving = savingsAdapter.currentList[position]
                piggyBankViewModel.deleteSaving(saving.id)
                Snackbar.make(requireView(), getString(R.string.item_deleted), Snackbar.LENGTH_LONG).apply {
                    setAction(getString(R.string.undo)) {
                        piggyBankViewModel.addSaving(saving)
                    }
                    show()
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
        itemTouchHelper.attachToRecyclerView(binding.recyclerSavings)
    }

    private fun observeViewModel() {
        piggyBankViewModel.getAllSavings().observe(viewLifecycleOwner) {
            savingsAdapter.submitList(it)
            calculateSavings(it)
            binding.imgGoal.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            binding.lblEmptyEntries.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
//        piggyBankViewModel.getPiggyBank().observe(viewLifecycleOwner) {
//            binding.editCurrencySaved.setText(it.currencySaved.toString())
//            binding.editHryvniaSaved.setText(it.hryvniaSaved.toString())
//            binding.editCurrencyTaken.setText(it.currencyTaken.toString())
//            binding.editHryvniaTaken.setText(it.hryvniaTaken.toString())
//            piggyBankId = it.id
//        }
    }

    private fun calculateSavings(items: List<Saving>) {
        val totalSaved = items.sumOf { it.saved.toDouble() }
        val totalTarget = items.sumOf { it.target.toDouble() }
        val formattedTotalSavedAsDouble = NumberUtils.formatDecimal(totalSaved).toDouble()
        val formattedTotalTargetAsDouble = NumberUtils.formatDecimal(totalTarget).toDouble()
        binding.tvTotalSaved.text = NumberUtils.formatNumberWithThousandsSeparator(formattedTotalSavedAsDouble)
        binding.tvTotalTarget.text = NumberUtils.formatNumberWithThousandsSeparator(formattedTotalTargetAsDouble)
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