package com.amarant.apps.budgetapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amarant.apps.budgetapp.entities.PiggyBank
import com.amarant.apps.budgetapp.repository.PiggyBankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PiggyBankViewModel @Inject constructor(
    private val piggyBankRepository: PiggyBankRepository
): ViewModel() {

    fun getPiggyBank() = piggyBankRepository.getPiggyBank()

    fun updatePiggyBank(piggyBank: PiggyBank) = viewModelScope.launch {
        piggyBankRepository.updatePiggyBank(piggyBank)
    }
}