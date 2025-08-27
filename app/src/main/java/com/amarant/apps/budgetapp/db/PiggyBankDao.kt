package com.amarant.apps.budgetapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.amarant.apps.budgetapp.entities.PiggyBank
import com.amarant.apps.budgetapp.entities.Saving

@Dao
interface PiggyBankDao {

    @Query("SELECT * FROM piggy_bank LIMIT 1")
    fun getPiggyBank(): LiveData<PiggyBank>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePiggyBank(piggyBank: PiggyBank)

    @Query("SELECT * FROM savings")
    fun getAllSavings(): LiveData<List<Saving>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSaving(saving: Saving)

    @Query("DELETE FROM savings WHERE id = :id")
    fun deleteSaving(id: Int)
}