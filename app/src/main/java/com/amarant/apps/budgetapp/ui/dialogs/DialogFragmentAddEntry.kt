package com.amarant.apps.budgetapp.ui.dialogs

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionManager
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.DialogAddBudgetEntryBinding
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.entities.HistoryItem
import com.amarant.apps.budgetapp.entities.QuickCategoryItem
import com.amarant.apps.budgetapp.ui.adapter.QuickCategoriesAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.BudgetViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.EntryViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.HistoryViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.ProfileViewModel
import com.amarant.apps.budgetapp.util.CategoryUtils
import com.amarant.apps.budgetapp.util.CategoryUtils.ALL_CATEGORIES
import com.amarant.apps.budgetapp.util.CategoryUtils.CATEGORY_MAPPING
import com.amarant.apps.budgetapp.util.Constants
import com.amarant.apps.budgetapp.util.DateUtils
import com.amarant.apps.budgetapp.util.UtilityFunctions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class DialogFragmentAddEntry : Fragment() {

    private val args by navArgs<DialogFragmentAddEntryArgs>()

    private var _binding: DialogAddBudgetEntryBinding? = null
    private val binding: DialogAddBudgetEntryBinding
        get() = _binding ?: throw RuntimeException("DialogAddBudgetEntryBinding == null")

    private val entryViewModel: EntryViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val budgetViewModel: BudgetViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()

    private lateinit var quickCategoriesAdapter: QuickCategoriesAdapter
    private lateinit var typeAutoCompleteTextView: AutoCompleteTextView
    private lateinit var categoryAutoCompleteTextView: AutoCompleteTextView

//    private var isExpanded = false
    private var selectedCategory = ""

//    private var dialogView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setStyle(STYLE_NORMAL, R.style.CustomDialog)
    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return MaterialAlertDialogBuilder(requireContext(), theme).apply {
//            dialogView = onCreateView(layoutInflater, null, savedInstanceState)
//            setView(dialogView)
//        }.create()
//    }

//    override fun getView(): View? {
//        return dialogView
//    }

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
//        dialog?.window?.setDimAmount(0.7f)
//        initAutoCompleteTextViews()
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
        quickCategoriesAdapter = QuickCategoriesAdapter()
        binding.recyclerQuickCategories.adapter = quickCategoriesAdapter
        quickCategoriesAdapter.onCategoryClickListener = { name, isSelected ->
//            val currentList = quickCategoriesAdapter.currentList.toMutableList()
//            val newList = if (!isExpanded) {
//                mutableListOf(
//                    QuickCategoryItem("Groceries", R.drawable.circle_shopping, true),
//                    QuickCategoryItem("Restaurants", R.drawable.circle_cafe),
//                    QuickCategoryItem("Cash", R.drawable.circle_transfer),
//                    QuickCategoryItem("Utilities", R.drawable.circle_utilities),
//                    QuickCategoryItem("Clothes", R.drawable.circle_clothing),
//                    QuickCategoryItem("House", R.drawable.circle_housing),
//                    QuickCategoryItem("Car", R.drawable.circle_transportation),
//                    QuickCategoryItem("Beauty", R.drawable.circle_personal_care)
//                )
//            } else {
//                mutableListOf(
//                    QuickCategoryItem("Groceries", R.drawable.circle_shopping, true),
//                    QuickCategoryItem("Restaurants", R.drawable.circle_cafe),
//                    QuickCategoryItem("Cash", R.drawable.circle_transfer),
//                    QuickCategoryItem("Utilities", R.drawable.circle_utilities),
//                    QuickCategoryItem("Clothes", R.drawable.circle_clothing),
//                    QuickCategoryItem("House", R.drawable.circle_housing),
//                    QuickCategoryItem("Car", R.drawable.circle_transportation),
//                    QuickCategoryItem("Beauty", R.drawable.circle_personal_care),
//
//                    QuickCategoryItem("Health", R.drawable.circle_health),
//                    QuickCategoryItem("Pets", R.drawable.circle_pets),
//                    QuickCategoryItem("Taxi", R.drawable.circle_subscriptions),
//                    QuickCategoryItem("Entertainment", R.drawable.circle_entertainment),
//                    QuickCategoryItem("Education", R.drawable.circle_education),
//                    QuickCategoryItem("Traveling", R.drawable.circle_traveling),
//                    QuickCategoryItem("Gifts", R.drawable.circle_gifts),
//                    QuickCategoryItem("Charity", R.drawable.circle_charity),
//
//                    QuickCategoryItem("Taxes", R.drawable.circle_taxes),
//                    QuickCategoryItem("Rent", R.drawable.circle_housing),
//                )
//            }
            entryViewModel.selectCategory(name)
//            val index = newList.indexOfFirst { it.name == name }
//            if (index != -1) {
//                val oldItem = newList[index]
//                newList[index] = oldItem.copy(isSelected = !isSelected)
//            }
//            if (!isSelected) {
//                selectedCategory = name
//                binding.tvSelectedCategory.text = name
//                historyViewModel.switchHistoryCategory(CATEGORY_MAPPING[name] ?: 0)
//            } else {
//                binding.tvSelectedCategory.text = ""
//            }
//            quickCategoriesAdapter.submitList(newList)
        }
//        quickCategoriesAdapter.submitList(
//            listOf(
//                QuickCategoryItem("Groceries", R.drawable.circle_shopping, true),
//                QuickCategoryItem("Restaurants", R.drawable.circle_cafe),
//                QuickCategoryItem("Cash", R.drawable.circle_transfer),
//                QuickCategoryItem("Utilities", R.drawable.circle_utilities),
//                QuickCategoryItem("Clothes", R.drawable.circle_clothing),
//                QuickCategoryItem("House", R.drawable.circle_housing),
//                QuickCategoryItem("Car", R.drawable.circle_transportation),
//                QuickCategoryItem("Beauty", R.drawable.circle_personal_care)
//            )
//        )
        binding.lblAddEntry.text = "On ${args.selectedDate}"
    }

    private fun setClickListeners() {
        binding.lblShowMore.setOnClickListener {
//            val list = if (isExpanded) {
//                listOf(
//                    QuickCategoryItem("Groceries", R.drawable.circle_shopping, true),
//                    QuickCategoryItem("Restaurants", R.drawable.circle_cafe),
//                    QuickCategoryItem("Cash", R.drawable.circle_transfer),
//                    QuickCategoryItem("Utilities", R.drawable.circle_utilities),
//                    QuickCategoryItem("Clothes", R.drawable.circle_clothing),
//                    QuickCategoryItem("House", R.drawable.circle_housing),
//                    QuickCategoryItem("Car", R.drawable.circle_transportation),
//                    QuickCategoryItem("Beauty", R.drawable.circle_personal_care)
//                )
//            } else {
//                listOf(
//                    QuickCategoryItem("Groceries", R.drawable.circle_shopping, true),
//                    QuickCategoryItem("Restaurants", R.drawable.circle_cafe),
//                    QuickCategoryItem("Cash", R.drawable.circle_transfer),
//                    QuickCategoryItem("Utilities", R.drawable.circle_utilities),
//                    QuickCategoryItem("Clothes", R.drawable.circle_clothing),
//                    QuickCategoryItem("House", R.drawable.circle_housing),
//                    QuickCategoryItem("Car", R.drawable.circle_transportation),
//                    QuickCategoryItem("Beauty", R.drawable.circle_personal_care),
//
//                    QuickCategoryItem("Health", R.drawable.circle_health),
//                    QuickCategoryItem("Pets", R.drawable.circle_pets),
//                    QuickCategoryItem("Taxi", R.drawable.circle_subscriptions),
//                    QuickCategoryItem("Entertainment", R.drawable.circle_entertainment),
//                    QuickCategoryItem("Education", R.drawable.circle_education),
//                    QuickCategoryItem("Traveling", R.drawable.circle_traveling),
//                    QuickCategoryItem("Gifts", R.drawable.circle_gifts),
//                    QuickCategoryItem("Charity", R.drawable.circle_charity),
//
//                    QuickCategoryItem("Taxes", R.drawable.circle_taxes),
//                    QuickCategoryItem("Rent", R.drawable.circle_housing),
//                )
//            }
            entryViewModel.changeExpandedState()
//            isExpanded = !isExpanded
//            quickCategoriesAdapter.submitList(list) {
//                if (!isExpanded) {
//                    binding.scrollView.fullScroll(View.FOCUS_UP)
//                    TransitionManager.beginDelayedTransition(binding.scrollView)
//                    binding.scrollView.post {
//                        binding.scrollView.scrollTo(0, 0)
//                    }
//                } else {
//                    binding.scrollView.post {
//                        TransitionManager.beginDelayedTransition(binding.scrollView)
//                        binding.scrollView.scrollTo(0, binding.lblCategory.top)
//                    }
//                }
//                TransitionManager.beginDelayedTransition(binding.scrollView)
//                binding.lblShowMore.text = if (isExpanded) "Show less" else "Show more"
//                binding.recyclerQuickCategories.layoutAnimation =
//                    AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation)
//                binding.recyclerQuickCategories.scheduleLayoutAnimation()

//            }
        }
        binding.btnAddEntry.setOnClickListener {
            val bankName = ""
            val debitOrCredit = if (binding.btnExpense.isChecked) "Debit" else "Credit"
            val amount = binding.tilAmount.editText?.text.toString()
            val purpose = binding.tilName.editText?.text.toString()
            val d = Date(System.currentTimeMillis())
            val formatter = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
            val date = UtilityFunctions.dateStringToMillis(args.selectedDate ?: formatter.format(d))
                .toString()
            val revisedCurrentBalance = "0"
            val category = selectedCategory
            Log.d("WTF", "$amount")
            Log.d("WTF", "$purpose")
            Log.d("WTF", "$date")
            Log.d("WTF", "$category")
            val amountAsInt = amount.toIntOrNull()
            if (amountAsInt == null || purpose.isEmpty() || category.isEmpty()) {
                val snackbar = Snackbar.make(
                    binding.btnAddEntry,
                    getString(R.string.fill_in_all_fields), Snackbar.LENGTH_SHORT
                )
                snackbar.setAction(getString(R.string.hide)) {
                    snackbar.dismiss()
                }
                snackbar.show()
                return@setOnClickListener
            }
            submitBudgetEntryToDB(
                bankName,
                debitOrCredit,
                amount,
                purpose,
                date,
                revisedCurrentBalance,
                category
            )
            historyViewModel.addHistory(
                HistoryItem(
                    purpose,
                    CATEGORY_MAPPING[selectedCategory] ?: 0
                )
            )
        }
        binding.editName.onItemClickListener = AdapterView.OnItemClickListener {
            parent, view, position, id ->
                binding.editName.clearFocus()
                hideKeyboardFrom(binding.editName)
        }
        binding.editName.setOnEditorActionListener { textView, actionId, keyEvent ->
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
//        Log.e("WTF", "${categoriesArr[0]}")
//        entryViewModel.initCategories(ALL_CATEGORIES.toTypedArray())
//        historyViewModel.switchHistoryCategory(0)
        budgetViewModel.getCategoryStats().observe(viewLifecycleOwner) {
            Log.d("WTF", it.toString())
            entryViewModel.initCategories(ALL_CATEGORIES.toTypedArray(), it)
        }
        entryViewModel.categories.observe(viewLifecycleOwner) {
//            val list = if (isExpanded) it else it.take(8)
            quickCategoriesAdapter.submitList(it) {
//                TransitionManager.beginDelayedTransition(binding.recyclerQuickCategories)
                binding.scrollView.post {
                    val top = binding.lblCategory.top
                    binding.scrollView.smoothScrollTo(0, top)
                }
            }
        }
        entryViewModel.isExpanded.observe(viewLifecycleOwner) {
            binding.lblShowMore.text = if (it) "Show less" else "Show more"
        }
        entryViewModel.selectedCategory.observe(viewLifecycleOwner) {
//            val categoriesArr = ALL_CATEGORIES.toTypedArray()
            val categoryName = entryViewModel.getSelectedCategoryName()
            selectedCategory = categoryName
            binding.tvSelectedCategory.text = categoryName//categoriesArr[it]
            historyViewModel.switchHistoryCategory(CATEGORY_MAPPING[categoryName] ?: 0)
        }
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
            binding.btnAddEntry,
            getString(R.string.entry_added), Snackbar.LENGTH_SHORT
        )
        snackbar.setAction(getString(R.string.hide)) {
            snackbar.dismiss()
        }
        snackbar.show()
        findNavController().popBackStack()
    }

    private fun readHistory() {
        historyViewModel.getHistory().observe(viewLifecycleOwner) { history ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                history.map { it.entry }
            )
            binding.editName.setAdapter(adapter)
        }
    }

    private fun initAutoCompleteTextViews() {
//        val spinnerItems = listOf(
//            SpinnerItem(R.drawable.ic_debit, resources.getStringArray(R.array.operation_types)[0]),
//            SpinnerItem(R.drawable.ic_credit, resources.getStringArray(R.array.operation_types)[1])
//        )
//        val arrayAdapter = SpinnerAdapter(requireContext(), spinnerItems)
//        val arrayAdapter = AutoCompleteAdapter(
//            requireContext(),
//            arrayOf("Expense", "Income"),
//            arrayOf(R.drawable.ic_debit, R.drawable.ic_credit)
//        )
////        val adapter = ArrayAdapter(
////            requireContext(),
////              R.layout.list_item_exposed_dropdown
////            resources.getStringArray(R.array.operation_types)
////        )
//        val textInputLayout = binding.tilType//dialogView?.findViewById<TextInputLayout>(R.id.til_type)
//        typeAutoCompleteTextView = (textInputLayout.editText as AutoCompleteTextView)
//        typeAutoCompleteTextView.setAdapter(arrayAdapter)
//        typeAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener {
//                parent, view, position, id ->
//            val selectedItem = parent.getItemAtPosition(position).toString()
//            if (selectedItem == "Income") {
//                textInputLayout.startIconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_credit)
//                textInputLayout.setStartIconTintList(ContextCompat.getColorStateList(requireContext(), R.color.positive_green))
//            } else {
//                textInputLayout.startIconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_debit)
//                textInputLayout.setStartIconTintList(ContextCompat.getColorStateList(requireContext(), R.color.negative_red))
//            }
//            Log.d("WTF", "$selectedItem")
////            onboardingViewModel.setCurrency(selectedCurrency)
////            typeAutoCompleteTextView.setText(selectedItem, false)
//        }
//        typeAutoCompleteTextView.setSelection(0)
//        typeAutoCompleteTextView.setText("Expense", false)

        // TODO Categories

//        val categoriesAdapter = AutoCompleteAdapter(
//            requireContext(),
//            arrayOf(
//                "Car",
//                "Restaurants",
//                "Groceries",
//                "Rent",
//                "Health",
//                "Entertainment",
//                "Cash",
//                "Taxes",
//                "Clothes",
//                "Pets",
//                "Education",
//                "Gifts",
//                "Charity",
//                "Traveling",
//                "Beauty",
//                "Utilities",
//                "Taxi"
//            ),
//            arrayOf(
//                R.drawable.ic_car,
//                R.drawable.ic_coffee,
//                R.drawable.ic_shopping,
//                R.drawable.ic_home,
//                R.drawable.ic_heart,
//                R.drawable.ic_joystick,
//                R.drawable.ic_credit_card,
//                R.drawable.ic_document,
//                R.drawable.ic_tshirt,
//                R.drawable.ic_pets,
//                R.drawable.ic_graduation,
//                R.drawable.ic_gift,
//                R.drawable.ic_hand_heart,
//                R.drawable.ic_plane,
//                R.drawable.ic_scissors,
//                R.drawable.ic_thunder,
//                R.drawable.ic_smartphone,
//                R.drawable.cat_unknown
//            )
//        )
//        val textInputLayout2 = dialogView?.findViewById<TextInputLayout>(R.id.til_category)
//        categoryAutoCompleteTextView = (textInputLayout2?.editText as AutoCompleteTextView)
//        categoryAutoCompleteTextView.setAdapter(categoriesAdapter)
//        categoryAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener {
//                parent, view, position, id ->
//            val selectedItem = parent.getItemAtPosition(position).toString()
//            val drawable = when(selectedItem) {
//                "Car" -> R.drawable.ic_car
//                "Restaurants" -> R.drawable.ic_coffee
//                "Groceries" -> R.drawable.ic_shopping
//                "Rent" -> R.drawable.ic_home
//                "Health" -> R.drawable.ic_heart
//                "Entertainment" -> R.drawable.ic_joystick
//                "Cash" -> R.drawable.ic_credit_card
//                "Taxes" -> R.drawable.ic_document
//                "Clothes" -> R.drawable.ic_tshirt
//                "Pets" -> R.drawable.ic_pets
//                "Education" -> R.drawable.ic_graduation
//                "Gifts" -> R.drawable.ic_gift
//                "Charity" -> R.drawable.ic_hand_heart
//                "Traveling" -> R.drawable.ic_plane
//                "Beauty" -> R.drawable.ic_scissors
//                "Utilities" -> R.drawable.ic_thunder
//                "Taxi" -> R.drawable.ic_smartphone
//                else -> R.drawable.cat_unknown
//            }
//            textInputLayout2.startIconDrawable = ContextCompat.getDrawable(requireContext(), drawable)
//            Log.d("WTF", "$selectedItem")
//        }
//        categoryAutoCompleteTextView.setSelection(0)
//        categoryAutoCompleteTextView.setText("Groceries", false)
    }
}