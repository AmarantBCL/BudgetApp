package com.amarant.apps.budgetapp.repository

import com.amarant.apps.budgetapp.db.PiggyBankDao
import com.amarant.apps.budgetapp.entities.PiggyBank
import com.amarant.apps.budgetapp.entities.Saving
import javax.inject.Inject

class PiggyBankRepository @Inject constructor(
    val piggyBankDao: PiggyBankDao
) {

    suspend fun updatePiggyBank(piggyBank: PiggyBank) = piggyBankDao.updatePiggyBank(piggyBank)

    fun getPiggyBank() = piggyBankDao.getPiggyBank()

    fun getAllSavings() = piggyBankDao.getAllSavings()

    suspend fun addSaving(saving: Saving) = piggyBankDao.addSaving(saving)

    fun deleteSaving(id: Int) = piggyBankDao.deleteSaving(id)
}