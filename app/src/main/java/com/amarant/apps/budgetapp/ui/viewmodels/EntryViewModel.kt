package com.amarant.apps.budgetapp.ui.viewmodels

import android.provider.SyncStateContract.Helpers.update
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.entities.BudgetUI
import com.amarant.apps.budgetapp.entities.Category
import com.amarant.apps.budgetapp.entities.CategoryStat
import com.amarant.apps.budgetapp.entities.QuickCategoryItem
import com.amarant.apps.budgetapp.repository.BudgetRepository
import com.amarant.apps.budgetapp.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.enums.EnumEntries

@HiltViewModel
class EntryViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

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

    val selectedCategories: LiveData<List<Category>> =
        categoryRepository.selectedCategories.asLiveData()

//    fun getCategoryStats(): LiveData<List<Category>> {
////    fun getCategoryStats(): LiveData<List<CategoryStat>> {
//        return budgetRepository.getCategoryStats().switchMap { categoryStats ->
//            selectedCategories.map { category ->
//                category
//            }
//        }
//    }

    fun getCategoryStats(): LiveData<List<Category>> {
        return budgetRepository.getCategoryStats().switchMap { categoryStats ->
            selectedCategories.map { categories ->
                val list = categories.toMutableList()
                list.removeAt(0)
                sortCategories(list, categoryStats)
            }
        }
    }

    fun getSelectedCategoryName(): Category {
        val indexOfCategory = selectedCategory.value ?: DEFAULT_CATEGORY_ID
        val element = allCategories[indexOfCategory]
        return element.category
    }

//    fun initCategories(categoryEntries: EnumEntries<Category>, categoryStats: List<CategoryStat>) {
//        val categoryList = categoryEntries.toMutableList()
//        categoryList.removeAt(0)
//        if (categories.value.isNullOrEmpty()) {
//            val sortedCategories = sortCategories(categoryList, categoryStats).map {
//                QuickCategoryItem(it)
//            }
//            allCategories = sortedCategories
//            val expanded = isExpanded.value
//            if (expanded == true) {
//                _categories.value = sortedCategories
//            } else {
//                _categories.value = sortedCategories.take(DEFAULT_NUMBER_OF_DISPLAYED_CATEGORIES)
//            }
//            setCategorySelected(0)
//        }
//    }

    fun simpleInitCategories(categoryList: List<Category>) {
        if (categories.value.isNullOrEmpty()) {
            val combinedList = categoryList.map {
                QuickCategoryItem(it)
            }
            allCategories = combinedList
            _categories.value = combinedList
        }
        setCategorySelected(0)
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
        val statsMap = stats.associate { it.category to it.count }
        val positionMap = categories.withIndex().associate { it.value.dbName to it.index }
        val sortedList = categories.sortedWith(
            compareByDescending<Category> { statsMap[it.dbName] ?: 0 }//DEFAULT_CATEGORY_ID }
                .thenBy { positionMap[it.dbName] ?: 0 }//Int.MAX_VALUE }
        )
        return sortedList
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