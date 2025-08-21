package com.amarant.apps.budgetapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amarant.apps.budgetapp.entities.Category
import com.amarant.apps.budgetapp.entities.CategoryStat
import com.amarant.apps.budgetapp.entities.QuickCategoryItem
import kotlin.enums.EnumEntries

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

    private var allCategories: List<QuickCategoryItem> = listOf()

    fun getSelectedCategoryName(): Category {
        val indexOfCategory = selectedCategory.value ?: DEFAULT_CATEGORY_ID
        val element = allCategories[indexOfCategory]
        return element.category
    }

    fun initCategories(categoryEntries: EnumEntries<Category>, categoryStats: List<CategoryStat>) {
        val categoryList = categoryEntries.toMutableList()
        categoryList.removeAt(0)
        if (categories.value.isNullOrEmpty()) {
            val sortedCategories = sortCategories(categoryList, categoryStats).map {
                QuickCategoryItem(it)
            }
            allCategories = sortedCategories
            val expanded = isExpanded.value
            if (expanded == true) {
                _categories.value = sortedCategories
            } else {
                _categories.value = sortedCategories.take(DEFAULT_NUMBER_OF_DISPLAYED_CATEGORIES)
            }
            setCategorySelected(0)
        }
    }

    fun changeExpandedState() {
        val currentState = isExpanded.value
        val currentCategory = selectedCategory.value
        if (currentState == true) {
            _isExpanded.value = false
            _categories.value = allCategories.take(DEFAULT_NUMBER_OF_DISPLAYED_CATEGORIES)
        } else {
            _isExpanded.value = true
            _categories.value = allCategories
        }
        setCategorySelected(currentCategory ?: 0)
    }

    fun selectCategory(category: Category) {
        val list = allCategories.toMutableList()
        val index = list.indexOfFirst { it.category == category }
        setCategorySelected(index)
    }

    private fun sortCategories(categories: List<Category>, stats: List<CategoryStat>): List<Category> {
        val countMap = stats.associate { it.category to it.count }
        val posMap = categories.withIndex().associate { it.value to it.index }
        return categories.sortedWith(
            compareByDescending<Category> { countMap[it.dbName] ?: DEFAULT_CATEGORY_ID }
                .thenBy { posMap[it] ?: Int.MAX_VALUE }
        )
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
                _categories.value = list.take(DEFAULT_NUMBER_OF_DISPLAYED_CATEGORIES)
            }
            _selectedCategory.value = index
        }
    }

    companion object {

        private const val DEFAULT_CATEGORY_ID = 1
        private const val DEFAULT_NUMBER_OF_DISPLAYED_CATEGORIES = 8
    }
}