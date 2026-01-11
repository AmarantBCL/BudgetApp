package com.amarant.apps.budgetapp.repository

import androidx.lifecycle.map
import com.amarant.apps.budgetapp.db.PiggyBankDao
import com.amarant.apps.budgetapp.entities.PiggyBank
import com.amarant.apps.budgetapp.entities.Saving
import javax.inject.Inject

class PiggyBankRepository @Inject constructor(
    val piggyBankDao: PiggyBankDao
) {

    suspend fun updatePiggyBank(piggyBank: PiggyBank) = piggyBankDao.updatePiggyBank(piggyBank)

    fun getPiggyBank() = piggyBankDao.getPiggyBank()

    fun getAllSavings() = piggyBankDao.getAllSavings().map { savings ->
        savings.sortedWith(
            compareBy<Saving> {
                val isAtBottom = it.target <= it.saved
                if (isAtBottom) 0 else 1
            }.thenBy { it.id }
        )
    }

    suspend fun addSaving(saving: Saving) = piggyBankDao.addSaving(saving)

    suspend fun deleteSaving(id: Int) = piggyBankDao.deleteSaving(id)
}