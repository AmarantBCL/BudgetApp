package com.amarant.apps.budgetapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.amarant.apps.budgetapp.entities.Category
import com.amarant.apps.budgetapp.entities.CategoryStat
import com.amarant.apps.budgetapp.entities.QuickCategoryItem
import com.amarant.apps.budgetapp.repository.BudgetRepository
import com.amarant.apps.budgetapp.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

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

    fun getCategoryStats(): LiveData<List<Category>> {
        return budgetRepository.getCategoryStats().switchMap { categoryStats ->
            selectedCategories.map { categories ->
                val list = categories.toMutableList()
                if (list.isNotEmpty()) {
                    list.removeAt(0) // Remove ALL category
                }
                sortCategories(list, categoryStats)
            }
        }
    }

    fun getSelectedCategoryName(): Category {
        val indexOfCategory = selectedCategory.value ?: DEFAULT_CATEGORY_ID
        return if (indexOfCategory in allCategories.indices) {
            allCategories[indexOfCategory].category
        } else {
            Category.OTHER
        }
    }

    fun simpleInitCategories(categoryList: List<Category>) {
        val combinedList = categoryList.map {
            QuickCategoryItem(it)
        }
        allCategories = combinedList

        if (_selectedCategory.value == null) {
            setCategorySelected(0)
        } else {
            setCategorySelected(_selectedCategory.value ?: 0)
        }
    }

    fun selectCategory(category: Category) {
        val index = allCategories.indexOfFirst { it.category == category }
        if (index != -1) {
            setCategorySelected(index)
        }
    }

    private fun sortCategories(categories: List<Category>, stats: List<CategoryStat>): List<Category> {
        val statsMap = stats.associate { it.category to it.count }
        val positionMap = categories.withIndex().associate { it.value.dbName to it.index }
        return categories.sortedWith(
            compareByDescending<Category> { statsMap[it.dbName] ?: 0 }
                .thenBy { positionMap[it.dbName] ?: 0 }
        )
    }

    private fun setCategorySelected(index: Int) {
        if (index !in allCategories.indices) return

        val listWithSelection = allCategories.mapIndexed { i, item ->
            item.copy(isSelected = i == index)
        }

        allCategories = listWithSelection // Update the master list

        // Always show only a subset in the main fragment
        val displayedList = if (index < DEFAULT_NUMBER_OF_DISPLAYED_CATEGORIES) {
            listWithSelection.take(DEFAULT_NUMBER_OF_DISPLAYED_CATEGORIES)
        } else {
            // If the selected category is not in the top N,
            // we show top N-1 and the selected category as the last one.
            listWithSelection.take(DEFAULT_NUMBER_OF_DISPLAYED_CATEGORIES - 1) + listWithSelection[index]
        }

        _categories.value = displayedList
        _selectedCategory.value = index
    }

    companion object {
        private const val DEFAULT_CATEGORY_ID = 0
        private const val DEFAULT_NUMBER_OF_DISPLAYED_CATEGORIES = 8
    }
}