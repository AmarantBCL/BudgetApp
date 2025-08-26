package com.amarant.apps.budgetapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.amarant.apps.budgetapp.entities.Category
import com.amarant.apps.budgetapp.entities.Profile
import com.amarant.apps.budgetapp.entities.QuickCategoryItem
import com.amarant.apps.budgetapp.entities.TempProfile
import com.amarant.apps.budgetapp.repository.CategoryRepository
import com.amarant.apps.budgetapp.repository.PiggyBankRepository
import com.amarant.apps.budgetapp.util.EmailUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: CategoryRepository
): ViewModel() {

    private val _tempProfile = MutableLiveData(TempProfile(
        fullName = "",
        email = "",
        currency = "",
        monthlyIncome = null,
        savingGoal = null,
        categories = listOf()
    ))
    val tempProfile: LiveData<TempProfile>
        get() = _tempProfile

    private val _isNextButtonEnabled = MutableLiveData<Boolean>()
    val isNextButtonEnabled: LiveData<Boolean>
        get() = _isNextButtonEnabled

    val fullName: LiveData<String> = tempProfile.map { it.fullName }
    val emailAddress: LiveData<String> = tempProfile.map { it.email }
    val currency: LiveData<String> = tempProfile.map { it.currency }
    val monthlyIncome: LiveData<String> = tempProfile.map { it.monthlyIncome.toString() }
    val savingGoal: LiveData<String> = tempProfile.map { it.savingGoal.toString() }
    val categories: LiveData<List<QuickCategoryItem>> = tempProfile.map { it.categories }
//    val categories: LiveData<List<CategoryItem>> = tempProfile.map { it.categories }

    fun updateNextButtonState() {
        val profile = _tempProfile.value ?: return
        _isNextButtonEnabled.value = profile.fullName.isNotBlank() &&
                EmailUtils.isValidEmail(profile.email)
    }

    fun setNextButtonState(isEnabled: Boolean) {
        _isNextButtonEnabled.value = isEnabled
    }

    fun setFullName(name: String) {
        updateProfile { it.copy(fullName = name) }
        updateNextButtonState()
    }

    fun setEmailAddress(email: String) {
        updateProfile { it.copy(email = email) }
        updateNextButtonState()
    }

    fun setCurrency(currency: String) {
        updateProfile { it.copy(currency = currency) }
        updateNextButtonStateFromCurrency()
    }

    fun setMonthlyIncome(income: String) {
        val incomeAsInt = income.toIntOrNull()
        if (incomeAsInt != null && incomeAsInt > 0) {
            updateProfile { it.copy(monthlyIncome = incomeAsInt.toString()) }
        } else {
            updateProfile { it.copy(monthlyIncome = null) }
        }
        updateNextButtonStateFromCurrency()
    }

    fun updateNextButtonStateFromCurrency() {
        val profile = _tempProfile.value ?: return
        val income = profile.monthlyIncome ?: ""
        _isNextButtonEnabled.value = profile.currency.isNotEmpty() && income.isNotEmpty()
    }

    fun setSavingGoal(goal: String) {
        val savingGoalAsInt = goal.toIntOrNull()
        if (savingGoalAsInt != null && savingGoalAsInt > 0) {
            updateProfile { it.copy(savingGoal = savingGoalAsInt.toString()) }
        }
    }

//    fun initCategories(array: Array<String>) {
//        if (categories.value.isNullOrEmpty()) {
//            val list = array.map { CategoryItem(it, false) }
//            updateProfile { it.copy(categories = list) }
//        }
//    }

    fun initNewCategories() {
        if (categories.value.isNullOrEmpty()) {
            val allCategories = Category.entries.map { QuickCategoryItem(it) }.toMutableList()
            allCategories.removeAt(0)
            updateProfile { it.copy(categories = allCategories) }
        }
    }

//    fun updateCategorySelection(categoryName: String, isChecked: Boolean) {
//        val currentList = categories.value.orEmpty().toMutableList()
//        val index = currentList.indexOfFirst { it.name == categoryName }
//        if (index != -1) {
//            val oldItem = currentList[index]
//            currentList[index] = oldItem.copy(isChecked = isChecked)
//            updateProfile { it.copy(categories = currentList.toList()) }
//        }
//    }

    fun updateCategorySelection(category: Category, isSelected: Boolean) {
        val currentList = categories.value.orEmpty().toMutableList()
        val index = currentList.indexOfFirst { it.category == category }
        if (index != -1) {
            val oldItem = currentList[index]
            currentList[index] = oldItem.copy(isSelected = isSelected)
            updateProfile { it.copy(categories = currentList.toList()) }
            val selectedCategories = currentList.filter { it.isSelected }.map { it.category }
            saveCategories(selectedCategories)
        }
    }

    private fun saveCategories(selected: List<Category>) {
        viewModelScope.launch {
            repository.saveSelected(selected)
        }
    }

    fun buildAndSaveUserProfile(): Profile? {
        val temp = _tempProfile.value ?: return null
        if (temp.fullName.isBlank() ||
            temp.email.isBlank() ||
            temp.currency.isBlank() ||
            temp.monthlyIncome == null
        ) {
            return null
        }
//        val selectedCategoryNames = temp.categories.filter { it.isChecked }.map { it.name }
        return Profile(
            name = temp.fullName,
            email = temp.email,
            profileImageFilePath = "",
            bankName = "",
            currentBalance = 0f,
            initialBalance = 0f,
            primaryBank = true
        )
    }

    private fun updateProfile(update: (TempProfile) -> TempProfile) {
        val current = _tempProfile.value ?: return
        _tempProfile.value = update(current)
    }
}