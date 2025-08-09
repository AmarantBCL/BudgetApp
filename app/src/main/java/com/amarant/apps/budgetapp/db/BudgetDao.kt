package com.amarant.apps.budgetapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.entities.BudgetCategoryDetails
import com.amarant.apps.budgetapp.entities.CategoryStat

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBudget(budget: Budget)

    @Query("SELECT * FROM budget ORDER BY date DESC")
    fun getAllData(): LiveData<List<Budget>>

    @Query("UPDATE budget SET amount = :amount, purpose = :purpose, category = :category WHERE id = :id")
    suspend fun updateBudget(amount: Float, purpose: String, category: String, id: Int)

    @Delete
    suspend fun deleteEntry(budget: Budget)

    @Query("SELECT SUM(amount) FROM budget WHERE creditOrDebit = 'Credit'")
    fun getTotalCredit(): LiveData<Float>

    @Query("SELECT SUM(amount) FROM budget WHERE creditOrDebit = 'Debit'")
    fun getTotalSpending(): LiveData<Float>

    @Query("SELECT SUM(amount) FROM budget WHERE creditOrDebit = 'Debit' AND date BETWEEN :startDate AND :endDate")
    fun getTotalSpendingForPeriod(startDate: Long, endDate: Long): LiveData<Float>

    @Query("SELECT SUM(amount) FROM budget WHERE creditOrDebit = 'Credit' AND date BETWEEN :startDate AND :endDate")
    fun getTotalCreditForPeriod(startDate: Long, endDate: Long): LiveData<Float>

    @Query("SELECT * FROM budget WHERE date BETWEEN :startDate AND :endDate AND category LIKE '%' || :filter || '%' ORDER BY date DESC")
    fun getReportsBetweenDates(startDate: Long, endDate: Long, filter: String): LiveData<List<Budget>>

    @Query("SELECT category, SUM(amount) AS amount FROM budget WHERE creditOrDebit = 'Debit' " +
            "AND date BETWEEN :startDate AND :endDate GROUP BY category ORDER BY amount ASC")
    fun getSpendingsByCategory(startDate: Long, endDate: Long): LiveData<List<BudgetCategoryDetails>>

    @Query("""
        SELECT category, COUNT(*) AS entries_count
        FROM budget
        GROUP BY category
        ORDER BY entries_count DESC
    """)
    fun getCategoryStats(): LiveData<List<CategoryStat>>
}