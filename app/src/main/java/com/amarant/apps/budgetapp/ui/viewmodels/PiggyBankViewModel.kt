package com.amarant.apps.budgetapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.entities.ColorPalette
import com.amarant.apps.budgetapp.entities.PiggyBank
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

    fun getAllSavings() = piggyBankRepository.getAllSavings()

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
                if (it.isSelected) {
                    it.copy(isSelected = false)
                } else if (it.color == color) {
                    it.copy(isSelected = true)
                } else {
                    it
                }
            }
            _colorPalette.value = updatedList
        }
    }
}