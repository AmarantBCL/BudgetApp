package com.amarant.apps.budgetapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    val budgetRepository: BudgetRepository
) : ViewModel() {

    val allBudgetEntries: LiveData<List<Budget>> = budgetRepository.getAllBudgetEntries()

    fun insertBudget(budget: Budget) = viewModelScope.launch {
        budgetRepository.insertBudget(budget)
    }

    fun updateBudget(amount: Float, purpose: String, id: Int) = viewModelScope.launch {
        budgetRepository.updateBudget(amount, purpose, id)
    }

    fun deleteEntry(budget: Budget) = viewModelScope.launch {
        budgetRepository.deleteEntry(budget)
    }
}