package com.amarant.apps.budgetapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.amarant.apps.budgetapp.entities.HistoryItem

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history WHERE category = :categoryId")
    fun getHistory(categoryId: Int): LiveData<List<HistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHistory(history: HistoryItem)

    @Update
    suspend fun updateHistory(history: HistoryItem)
}