package com.amarant.apps.budgetapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.amarant.apps.budgetapp.entities.BudgetHistory
import com.amarant.apps.budgetapp.entities.CategoryBudget

@Dao
interface BudgetHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: BudgetHistory)

    @Query("SELECT * FROM budget_history ORDER BY dateStamp DESC")
    fun getAllHistory(): LiveData<List<BudgetHistory>>

    @Delete
    suspend fun deleteHistory(history: BudgetHistory)
}
