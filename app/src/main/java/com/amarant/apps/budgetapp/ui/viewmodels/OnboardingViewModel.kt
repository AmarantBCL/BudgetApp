package com.amarant.apps.budgetapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.amarant.apps.budgetapp.entities.CategoryItem
import com.amarant.apps.budgetapp.entities.TempProfile
import com.amarant.apps.budgetapp.util.EmailUtils

class OnboardingViewModel: ViewModel() {

    private val _isNextButtonEnabled = MutableLiveData<Boolean>()
    val isNextButtonEnabled: LiveData<Boolean>
        get() = _isNextButtonEnabled

//    private val _fullName = MutableLiveData<String>()
//    val fullName: LiveData<String>
//        get() = _fullName
//
//    private val _emailAddress = MutableLiveData<String>()
//    val emailAddress: LiveData<String>
//        get() = _emailAddress
//
//    private val _currency = MutableLiveData<String>()
//    val currency: LiveData<String>
//        get() = _currency
//
//    private val _monthlyIncome = MutableLiveData("")
//    val monthlyIncome: LiveData<String>
//        get() = _monthlyIncome
//
//    private val _savingGoal = MutableLiveData("")
//    val savingGoal: LiveData<String>
//        get() = _savingGoal
//
//    private val _categories = MutableLiveData<List<CategoryItem>>()
//    val categories: LiveData<List<CategoryItem>>
//        get() = _categories

    private val _tempProfile = MutableLiveData<TempProfile>(TempProfile(
        fullName = "",
        email = "",
        currency = "",
        monthlyIncome = null,
        savingGoal = null,
        categories = listOf()
    ))
    val tempProfile: LiveData<TempProfile>
        get() = _tempProfile

    val fullName: LiveData<String> = tempProfile.map { it.fullName }
    val emailAddress: LiveData<String> = tempProfile.map { it.email }
    val currency: LiveData<String> = tempProfile.map { it.currency }
    val monthlyIncome: LiveData<String> = tempProfile.map { it.monthlyIncome.toString() }
    val savingGoal: LiveData<String> = tempProfile.map { it.savingGoal.toString() }
    val categories: LiveData<List<CategoryItem>> = tempProfile.map { it.categories }

    private fun updateProfile(update: (TempProfile) -> TempProfile) {
        val current = _tempProfile.value ?: return
        _tempProfile.value = update(current)
    }

    fun updateNextButtonState() {
        val profile = _tempProfile.value ?: return
        _isNextButtonEnabled.value = profile.fullName.isNotBlank() &&
                EmailUtils.isValidEmail(profile.email)
    }

    fun setNextButtonState(isEnabled: Boolean) {
        _isNextButtonEnabled.value = isEnabled
    }

    fun setFullName(name: String) {
//        _fullName.value = name
        updateProfile { it.copy(fullName = name) }
        updateNextButtonState()
    }

    fun setEmailAddress(email: String) {
//        _emailAddress.value = email
        updateProfile { it.copy(email = email) }
        updateNextButtonState()
    }

//    fun updateNextButtonState() {
//        val currentFullName = _fullName.value ?: ""
//        val currentEmail = _emailAddress.value ?: ""
//        if (currentFullName.isNotEmpty() && currentEmail.isNotEmpty()) {
//            _isNextButtonEnabled.value = EmailUtils.isValidEmail(currentEmail)
//        } else {
//            _isNextButtonEnabled.value = false
//        }
//    }

    fun setCurrency(currency: String) {
//        _currency.value = currency
        updateProfile { it.copy(currency = currency) }
        updateNextButtonStateFromCurrency()
    }

    fun setMonthlyIncome(income: String) {
        val incomeAsInt = income.toIntOrNull()
        if (incomeAsInt != null && incomeAsInt > 0) {
//            _monthlyIncome.value = incomeAsInt.toString()
            updateProfile { it.copy(monthlyIncome = incomeAsInt.toString()) }
        } else {
//            _monthlyIncome.value = null
            updateProfile { it.copy(monthlyIncome = null) }
        }
        updateNextButtonStateFromCurrency()
    }

    fun updateNextButtonStateFromCurrency() {
        val profile = _tempProfile.value ?: return
        val income = profile.monthlyIncome ?: ""
        _isNextButtonEnabled.value = profile.currency.isNotEmpty() && income.isNotEmpty()
//        val currency = _currency.value ?: ""
//        val income = _monthlyIncome.value ?: ""
//        _isNextButtonEnabled.value = currency.isNotEmpty() && income.isNotEmpty()
    }

    fun setSavingGoal(goal: String) {
        val savingGoalAsInt = goal.toIntOrNull()
        if (savingGoalAsInt != null && savingGoalAsInt > 0) {
            updateProfile { it.copy(savingGoal = savingGoalAsInt.toString()) }
//            _savingGoal.value = savingGoalAsInt.toString()
        }
    }

    fun initCategories(array: Array<String>) {
        if (categories.value.isNullOrEmpty()) {
            val list = array.map { CategoryItem(it, false) }
            updateProfile { it.copy(categories = list) }
//            _categories.value = list
        }
    }

    fun updateCategorySelection(categoryName: String, isChecked: Boolean) {
        val currentList = categories.value.orEmpty().toMutableList()
        val index = currentList.indexOfFirst { it.name == categoryName }
        if (index != -1) {
            val oldItem = currentList[index]
            currentList[index] = oldItem.copy(isChecked = isChecked)
            updateProfile { it.copy(categories = currentList.toList()) }
//            _categories.value = currentList.toList()
        }
    }
}