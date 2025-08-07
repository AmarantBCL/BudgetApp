package com.amarant.apps.budgetapp.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.DialogAddBudgetEntryBinding
import com.amarant.apps.budgetapp.ui.adapter.AutoCompleteAdapter
import com.amarant.apps.budgetapp.ui.adapter.SpinnerAdapter
import com.amarant.apps.budgetapp.ui.adapter.SpinnerItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout

class DialogFragmentAddEntry : DialogFragment() {

    private var _binding: DialogAddBudgetEntryBinding? = null
    private val binding: DialogAddBudgetEntryBinding
        get() = _binding ?: throw RuntimeException("DialogAddBudgetEntryBinding == null")

    private lateinit var typeAutoCompleteTextView: AutoCompleteTextView
    private lateinit var categoryAutoCompleteTextView: AutoCompleteTextView

    private var dialogView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setStyle(STYLE_NORMAL, R.style.CustomDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), theme).apply {
            dialogView = onCreateView(layoutInflater, null, savedInstanceState)
            setView(dialogView)
        }.create()
    }

    override fun getView(): View? {
        return dialogView
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddBudgetEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setDimAmount(0.7f)
        initAutoCompleteTextViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initAutoCompleteTextViews() {
//        val spinnerItems = listOf(
//            SpinnerItem(R.drawable.ic_debit, resources.getStringArray(R.array.operation_types)[0]),
//            SpinnerItem(R.drawable.ic_credit, resources.getStringArray(R.array.operation_types)[1])
//        )
//        val arrayAdapter = SpinnerAdapter(requireContext(), spinnerItems)
        val arrayAdapter = AutoCompleteAdapter(
            requireContext(),
            arrayOf("Expense", "Income"),
            arrayOf(R.drawable.ic_debit, R.drawable.ic_credit)
        )
//        val adapter = ArrayAdapter(
//            requireContext(),
//              R.layout.list_item_exposed_dropdown
//            resources.getStringArray(R.array.operation_types)
//        )
        val textInputLayout = dialogView?.findViewById<TextInputLayout>(R.id.til_type)
        typeAutoCompleteTextView = (textInputLayout?.editText as AutoCompleteTextView)
        typeAutoCompleteTextView.setAdapter(arrayAdapter)
        typeAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener {
                parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            if (selectedItem == "Income") {
                textInputLayout.startIconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_credit)
                textInputLayout.setStartIconTintList(ContextCompat.getColorStateList(requireContext(), R.color.positive_green))
            } else {
                textInputLayout.startIconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_debit)
                textInputLayout.setStartIconTintList(ContextCompat.getColorStateList(requireContext(), R.color.negative_red))
            }
            Log.d("WTF", "$selectedItem")
//            onboardingViewModel.setCurrency(selectedCurrency)
//            typeAutoCompleteTextView.setText(selectedItem, false)
        }
        typeAutoCompleteTextView.setSelection(0)
        typeAutoCompleteTextView.setText("Expense", false)

        // TODO Categories

        val categoriesAdapter = AutoCompleteAdapter(
            requireContext(),
            arrayOf(
                "Car",
                "Restaurants",
                "Groceries",
                "Rent",
                "Health",
                "Entertainment",
                "Cash",
                "Taxes",
                "Clothes",
                "Pets",
                "Education",
                "Gifts",
                "Charity",
                "Traveling",
                "Beauty",
                "Utilities",
                "Taxi"
            ),
            arrayOf(
                R.drawable.ic_car,
                R.drawable.ic_coffee,
                R.drawable.ic_shopping,
                R.drawable.ic_home,
                R.drawable.ic_heart,
                R.drawable.ic_joystick,
                R.drawable.ic_credit_card,
                R.drawable.ic_document,
                R.drawable.ic_tshirt,
                R.drawable.ic_pets,
                R.drawable.ic_graduation,
                R.drawable.ic_gift,
                R.drawable.ic_hand_heart,
                R.drawable.ic_plane,
                R.drawable.ic_scissors,
                R.drawable.ic_thunder,
                R.drawable.ic_smartphone,
                R.drawable.cat_unknown
            )
        )
        val textInputLayout2 = dialogView?.findViewById<TextInputLayout>(R.id.til_category)
        categoryAutoCompleteTextView = (textInputLayout2?.editText as AutoCompleteTextView)
        categoryAutoCompleteTextView.setAdapter(categoriesAdapter)
        categoryAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener {
                parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            val drawable = when(selectedItem) {
                "Car" -> R.drawable.ic_car
                "Restaurants" -> R.drawable.ic_coffee
                "Groceries" -> R.drawable.ic_shopping
                "Rent" -> R.drawable.ic_home
                "Health" -> R.drawable.ic_heart
                "Entertainment" -> R.drawable.ic_joystick
                "Cash" -> R.drawable.ic_credit_card
                "Taxes" -> R.drawable.ic_document
                "Clothes" -> R.drawable.ic_tshirt
                "Pets" -> R.drawable.ic_pets
                "Education" -> R.drawable.ic_graduation
                "Gifts" -> R.drawable.ic_gift
                "Charity" -> R.drawable.ic_hand_heart
                "Traveling" -> R.drawable.ic_plane
                "Beauty" -> R.drawable.ic_scissors
                "Utilities" -> R.drawable.ic_thunder
                "Taxi" -> R.drawable.ic_smartphone
                else -> R.drawable.cat_unknown
            }
            textInputLayout2.startIconDrawable = ContextCompat.getDrawable(requireContext(), drawable)
            Log.d("WTF", "$selectedItem")
//            onboardingViewModel.setCurrency(selectedCurrency)
//            categoryAutoCompleteTextView.setText(selectedItem, false)
        }
        categoryAutoCompleteTextView.setSelection(0)
        categoryAutoCompleteTextView.setText("Groceries", false)
    }
}