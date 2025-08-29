package com.amarant.apps.budgetapp.ui.fragments.bottomsheet

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.BottomSheetUpdateSavingBinding
import com.amarant.apps.budgetapp.entities.Saving
import com.amarant.apps.budgetapp.ui.MainActivity
import com.amarant.apps.budgetapp.ui.viewmodels.PiggyBankViewModel
import com.amarant.apps.budgetapp.util.MessageUtils
import com.amarant.apps.budgetapp.util.NumberUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdateSavingBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetUpdateSavingBinding? = null
    private val binding: BottomSheetUpdateSavingBinding
        get() = _binding ?: throw RuntimeException("BottomSheetUpdateSavingBinding == null")

    private lateinit var savingItem: Saving

    private var isSubtract = false
    private var currencySymbol = ""
    private var currentBalance = 0.0
    private var targetBalance = 0.0
    private var updatedBalance = 0.0

    private val piggyBankViewModel: PiggyBankViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savingItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable(KEY_SAVING_ITEM, Saving::class.java)
                ?: throw RuntimeException("No arguments passed to UpdateBudgetBottomSheetFragment")
        } else {
            requireArguments().getParcelable(KEY_SAVING_ITEM)
                ?: throw RuntimeException("No arguments passed to UpdateBudgetBottomSheetFragment")
        }
        isSubtract = requireArguments().getBoolean(KEY_IS_SUBTRACT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetUpdateSavingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() {
        val saved = NumberUtils.formatNumberWithThousandsSeparator(savingItem.saved.toDouble())
        val target = NumberUtils.formatNumberWithThousandsSeparator(savingItem.target.toDouble())
        val imageRes = if (isSubtract) ContextCompat.getDrawable(requireContext(), R.drawable.ic_expenses)
            else ContextCompat.getDrawable(requireContext(), R.drawable.ic_trend)
        val buttonIcon = if (isSubtract) R.drawable.ic_minus else R.drawable.ic_plus
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.shape_color_circle)
        drawable?.setTint(ContextCompat.getColor(requireContext(), savingItem.circleColor))
        currencySymbol = savingItem.currency.first().toString()
        currentBalance = savingItem.saved.toDouble()
        targetBalance = savingItem.target.toDouble()
        updatedBalance = currentBalance
        binding.lblHeader.text = if (isSubtract) getString(R.string.withdraw_money) else getString(R.string.deposit_money)
        binding.imgIcon.setImageDrawable(imageRes)
        binding.imgColorCircle.setImageDrawable(drawable)
        binding.tvGoalName.text = savingItem.title
        binding.tvCurrentAmount.text = getString(R.string.currency_and_amount_placeholder, currencySymbol, saved)
        binding.tvTargetAmount.text = getString(R.string.currency_and_amount_placeholder, currencySymbol, target)
        binding.btnAddEntry.text = if (isSubtract) getString(R.string.take_money) else getString(R.string.add_money)
        binding.btnAddEntry.setIconResource(buttonIcon)
        // TODO Expanded state
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.root.parent as View)
        binding.editAmount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    private fun setClickListeners() {
        binding.editAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                calculatePreliminarySavings(text.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        binding.btnAddEntry.setOnClickListener {
            val amount = binding.editAmount.text.toString()
            if (amount.toDoubleOrNull() != null && updatedBalance >= 0 && updatedBalance <= targetBalance) {
                piggyBankViewModel.addSaving(savingItem.copy(saved = updatedBalance.toFloat()))
                MessageUtils.showSnackbarMessage(
                    binding.coordinator,
                    getString(R.string.entry_added),
                    getString(R.string.hide)
                )
                dialog?.dismiss()
            } else {
                MessageUtils.showSnackbarMessage(
                    binding.coordinator,
                    getString(R.string.enter_correct_amount),
                    getString(R.string.hide)
                )
            }
        }
    }

    private fun calculatePreliminarySavings(enteredAmount: String) {
        val amount = enteredAmount.ifEmpty { ZERO_VALUE_FOR_DEFAULT_BALANCE }.toDouble()
        updatedBalance = currentBalance
        if (isSubtract) {
            updatedBalance -= amount
        } else {
            updatedBalance += amount
        }
        binding.tvCurrentAmount.text = currencySymbol + " " + NumberUtils.formatNumberWithThousandsSeparator(updatedBalance)
    }

    companion object {

        private const val KEY_SAVING_ITEM = "saving_item"
        private const val KEY_IS_SUBTRACT = "is_subtract"
        private const val ZERO_VALUE_FOR_DEFAULT_BALANCE = "0"

        const val TAG = "UpdateSavingBottomSheet"

        fun newInstance(saving: Saving, isSubtract: Boolean = false): UpdateSavingBottomSheetFragment {
            return UpdateSavingBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_SAVING_ITEM, saving)
                    putBoolean(KEY_IS_SUBTRACT, isSubtract)
                }
            }
        }
    }
}