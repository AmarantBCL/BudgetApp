package com.amarant.apps.budgetapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.amarant.apps.budgetapp.entities.Budget

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBudget(budget: Budget)

    @Query("SELECT * FROM budget ORDER BY id DESC")
    fun getAllData(): LiveData<List<Budget>>

    @Query("UPDATE budget SET amount = :amount, purpose = :purpose WHERE id = :id")
    suspend fun updateBudget(amount: Float, purpose: String, id: Int)

    @Delete
    suspend fun deleteEntry(budget: Budget)
}