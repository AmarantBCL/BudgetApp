package com.amarant.apps.budgetapp.repository

import com.amarant.apps.budgetapp.db.HistoryDao
import com.amarant.apps.budgetapp.entities.HistoryItem
import javax.inject.Inject

class HistoryRepository @Inject constructor(
    val historyDao: HistoryDao
) {

    fun getAllHistory() = historyDao.getAllHistory()

    fun getHistory(categoryId: Int) = historyDao.getHistory(categoryId)

    suspend fun addHistory(history: HistoryItem) = historyDao.addHistory(history)

    suspend fun updateHistory(history: HistoryItem) = historyDao.updateHistory(history)

    suspend fun deleteFromHistory(entry: String) = historyDao.deleteFromHistory(entry)
}