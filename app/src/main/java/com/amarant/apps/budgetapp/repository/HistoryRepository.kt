package com.amarant.apps.budgetapp.repository

import androidx.lifecycle.LiveData
import com.amarant.apps.budgetapp.db.HistoryDao
import com.amarant.apps.budgetapp.entities.HistoryItem
import javax.inject.Inject

class HistoryRepository @Inject constructor(
    val historyDao: HistoryDao
) {

    fun getHistory(categoryId: Int) = historyDao.getHistory(categoryId)

    suspend fun addHistory(history: HistoryItem) = historyDao.addHistory(history)

    suspend fun updateHistory(history: HistoryItem) = historyDao.updateHistory(history)
}