package com.amarant.apps.budgetapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.amarant.apps.budgetapp.entities.HistoryItem
import com.amarant.apps.budgetapp.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _categoryId = MutableLiveData<Int>()
    private val categoryId: LiveData<Int>
        get() = _categoryId

    fun getHistory() = categoryId.switchMap {
        historyRepository.getHistory(it)
    }

    fun addHistory(history: HistoryItem) = viewModelScope.launch {
        historyRepository.addHistory(history)
    }

    fun updateHistory(history: HistoryItem) = viewModelScope.launch {
        historyRepository.updateHistory(history)
    }

    fun switchHistoryCategory(categoryId: Int) {
        _categoryId.value = categoryId
    }
}