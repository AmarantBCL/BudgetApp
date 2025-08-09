package com.amarant.apps.budgetapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.entities.QuickCategoryItem

class EntryViewModel : ViewModel() {

    private val _categories = MutableLiveData<List<QuickCategoryItem>>()
    val categories: LiveData<List<QuickCategoryItem>>
        get() = _categories

    private val _isExpanded = MutableLiveData(false)
    val isExpanded: LiveData<Boolean>
        get() = _isExpanded

    private val _selectedCategory = MutableLiveData<Int>()
    val selectedCategory: LiveData<Int>
        get() = _selectedCategory

//    private var selectedCategory = 0
    private var allCategories: List<QuickCategoryItem> = listOf()

    fun initCategories(array: Array<String>) {
        if (categories.value.isNullOrEmpty()) {
            val list = array.map { QuickCategoryItem(it, getCategoryDrawable(it)) }
            allCategories = list
            val expanded = isExpanded.value
            if (expanded == true) {
                _categories.value = list
            } else {
                _categories.value = list.take(8)
            }
            setCategorySelected(0)
        }
    }

    fun changeExpandedState() {
        val currentState = isExpanded.value
        val currentCategory = selectedCategory.value
        if (currentState == true) {
            _isExpanded.value = false
            _categories.value = allCategories.take(8)
        } else {
            _isExpanded.value = true
            _categories.value = allCategories
        }
        setCategorySelected(currentCategory ?: 0)
    }

    fun selectCategory(categoryName: String) {
        val list = allCategories.toMutableList()
        val index = list.indexOfFirst { it.name == categoryName }
        setCategorySelected(index)
    }

    private fun setCategorySelected(index: Int) {
        val list = allCategories.toMutableList()
        if (index != -1) {
            val element = list[index]
            list[index] = element.copy(isSelected = true)
            val currentState = isExpanded.value
            if (currentState == true) {
                _categories.value = list
            } else {
                _categories.value = list.take(8)
            }
//            selectedCategory = index
            _selectedCategory.value = index
        }
    }

    private fun getCategoryDrawable(categoryName: String): Int {
        return when(categoryName) {
            "Car" -> R.drawable.circle_transportation
            "Restaurants" -> R.drawable.circle_cafe
            "Groceries" -> R.drawable.circle_shopping
            "Rent" -> R.drawable.circle_housing
            "Health" -> R.drawable.circle_health
            "Entertainment" -> R.drawable.circle_entertainment
            "Cash" -> R.drawable.circle_transfer
            "Taxes" -> R.drawable.circle_taxes
            "Clothes" -> R.drawable.circle_clothing
            "Pets" -> R.drawable.circle_pets
            "Education" -> R.drawable.circle_education
            "Gifts" -> R.drawable.circle_gifts
            "Charity" -> R.drawable.circle_charity
            "Traveling" -> R.drawable.circle_traveling
            "Beauty" -> R.drawable.circle_personal_care
            "Utilities" -> R.drawable.circle_utilities
            "Taxi" -> R.drawable.circle_subscriptions
            "House" -> R.drawable.circle_housing
            else -> R.drawable.cat_unknown
        }
    }
}