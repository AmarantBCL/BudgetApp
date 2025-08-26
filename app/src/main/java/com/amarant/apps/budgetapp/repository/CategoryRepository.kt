package com.amarant.apps.budgetapp.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.amarant.apps.budgetapp.dataStore
import com.amarant.apps.budgetapp.entities.Category
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val SELECTED_CATEGORIES = stringSetPreferencesKey("selected_categories")

    val selectedCategories: Flow<List<Category>> =
        flowOf(Category.entries)
//        context.dataStore.data.map { prefs ->
//            prefs[SELECTED_CATEGORIES]?.map { Category.valueOf(it) } ?: emptyList()
//        }

    suspend fun saveSelected(categories: List<Category>) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_CATEGORIES] = categories.map { it.name }.toSet()
        }
    }
}