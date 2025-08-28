package com.amarant.apps.budgetapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.entities.ColorPalette
import com.amarant.apps.budgetapp.entities.PiggyBank
import com.amarant.apps.budgetapp.entities.Saving
import com.amarant.apps.budgetapp.repository.PiggyBankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PiggyBankViewModel @Inject constructor(
    private val piggyBankRepository: PiggyBankRepository
): ViewModel() {

    private val _colorPalette = MutableLiveData<List<ColorPalette>>()
    val colorPalette: LiveData<List<ColorPalette>>
        get() = _colorPalette

    fun getPiggyBank() = piggyBankRepository.getPiggyBank()

    fun updatePiggyBank(piggyBank: PiggyBank) = viewModelScope.launch {
        piggyBankRepository.updatePiggyBank(piggyBank)
    }

    fun getAllSavings() = piggyBankRepository.getAllSavings().map {
        it.reversed()
    }

    fun addSaving(saving: Saving) {
        viewModelScope.launch {
            piggyBankRepository.addSaving(saving)
        }
    }

    fun tryToAddSaving(goalName: String, amount: String, currency: String, date: Long, color: Int, id: Int): Boolean {
        if (goalName.isNotEmpty() && amount.isNotEmpty() && currency.isNotEmpty()) {
            addSaving(
                Saving(
                    id, goalName, amount.toFloat(), 0f, currency, date, color
                )
            )
            return true
        }
        return false
    }

    fun deleteSaving(id: Int) {
        viewModelScope.launch {
            piggyBankRepository.deleteSaving(id)
        }
    }

    fun initColorPalette() {
        if (colorPalette.value == null) {
            val colors = listOf(
                ColorPalette(R.color.positive_green, true),
                ColorPalette(R.color.negative_red),
                ColorPalette(R.color.blue),
                ColorPalette(R.color.amber),
                ColorPalette(R.color.accent_purple),
                ColorPalette(R.color.sky),
                ColorPalette(R.color.violet),
                ColorPalette(R.color.teal),
                ColorPalette(R.color.pink),
                ColorPalette(R.color.state_gray),
                ColorPalette(R.color.orange),
                ColorPalette(R.color.white)
            )
            _colorPalette.value = colors
        }
    }

    fun selectColorPalette(color: Int) {
        colorPalette.value?.let { colors ->
            val updatedList = colors.map {
                if (it.color == color) {
                    it.copy(isSelected = true)
                } else if (it.isSelected) {
                    it.copy(isSelected = false)
                } else {
                    it
                }
            }
            _colorPalette.value = updatedList
        }
    }
}