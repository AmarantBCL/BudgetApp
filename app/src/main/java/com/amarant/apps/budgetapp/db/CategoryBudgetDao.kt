package com.amarant.apps.budgetapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.amarant.apps.budgetapp.entities.CategoryBudget

@Dao
interface CategoryBudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: CategoryBudget)

    @Query("SELECT * FROM category_budgets")
    fun getAllBudgets(): LiveData<List<CategoryBudget>>

    @Delete
    suspend fun deleteBudget(budget: CategoryBudget)
}
