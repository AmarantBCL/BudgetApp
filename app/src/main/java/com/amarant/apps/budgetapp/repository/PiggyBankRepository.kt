package com.amarant.apps.budgetapp.repository

import androidx.lifecycle.LiveData
import com.amarant.apps.budgetapp.db.PiggyBankDao
import com.amarant.apps.budgetapp.entities.PiggyBank
import javax.inject.Inject

class PiggyBankRepository @Inject constructor(
    val piggyBankDao: PiggyBankDao
) {

    suspend fun updatePiggyBank(piggyBank: PiggyBank) = piggyBankDao.updatePiggyBank(piggyBank)

    fun getPiggyBank() = piggyBankDao.getPiggyBank()
}