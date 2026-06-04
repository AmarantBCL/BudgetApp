package com.amarant.apps.budgetapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.amarant.apps.budgetapp.entities.BudgetHistory

@Dao
interface BudgetHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: BudgetHistory)

    @Query("SELECT * FROM budget_history ORDER BY dateStamp DESC")
    fun getAllHistory(): LiveData<List<BudgetHistory>>
}
