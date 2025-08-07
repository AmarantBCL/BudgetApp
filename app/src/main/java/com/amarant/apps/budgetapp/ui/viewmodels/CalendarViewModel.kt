package com.amarant.apps.budgetapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CalendarViewModel : ViewModel() {
    private val _selectedDate = MutableLiveData<Long>()
    val selectedDate: LiveData<Long>
        get() = _selectedDate

    init {
        _selectedDate.value = System.currentTimeMillis()
    }

    fun setSelectedDate(date: Long) {
        _selectedDate.value = date
    }
}