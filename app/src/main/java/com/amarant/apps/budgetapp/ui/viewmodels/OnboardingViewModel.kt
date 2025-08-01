package com.amarant.apps.budgetapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OnboardingViewModel: ViewModel() {

    private val _isNextButtonEnabled = MutableLiveData<Boolean>()
    val isNextButtonEnabled: LiveData<Boolean>
        get() = _isNextButtonEnabled

    private val _fullName = MutableLiveData<String>()
    val fullName: LiveData<String>
        get() = _fullName

    private val _emailAddress = MutableLiveData<String>()
    val emailAddress: LiveData<String>
        get() = _emailAddress

    private val _currency = MutableLiveData<String>()
    val currency: LiveData<String>
        get() = _currency

    private val _monthlyIncome = MutableLiveData("")
    val monthlyIncome: LiveData<String>
        get() = _monthlyIncome

    private val _savingGoal = MutableLiveData("")
    val savingGoal: LiveData<String>
        get() = _savingGoal

    fun setNextButtonState(isEnabled: Boolean) {
        _isNextButtonEnabled.value = isEnabled
    }

    fun setFullName(name: String) {
        _fullName.value = name
        updateNextButtonState()
    }

    fun setEmailAddress(email: String) {
        _emailAddress.value = email
        updateNextButtonState()
    }

    fun updateNextButtonState() {
        val currentFullName = _fullName.value ?: ""
        val currentEmail = _emailAddress.value ?: ""
        _isNextButtonEnabled.value = currentFullName.isNotEmpty() && currentEmail.isNotEmpty()
    }

    fun setCurrency(currency: String) {
        _currency.value = currency
        updateNextButtonStateFromCurrency()
    }

    fun updateNextButtonStateFromCurrency() {
        val currency = _currency.value ?: ""
        _isNextButtonEnabled.value = currency.isNotEmpty()
    }

    fun setMonthlyIncome(income: String) {
        val incomeAsInt = income.toIntOrNull()
        if (incomeAsInt != null && incomeAsInt > 0) {
            _monthlyIncome.value = incomeAsInt.toString()
        }
    }

    fun setSavingGoal(goal: String) {
        val savingGoalAsInt = goal.toIntOrNull()
        if (savingGoalAsInt != null && savingGoalAsInt > 0) {
            _savingGoal.value = savingGoalAsInt.toString()
        }
    }
}