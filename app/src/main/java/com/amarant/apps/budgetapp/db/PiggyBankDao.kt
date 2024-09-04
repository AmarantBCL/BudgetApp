package com.amarant.apps.budgetapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.amarant.apps.budgetapp.entities.PiggyBank

@Dao
interface PiggyBankDao {

    @Query("SELECT * FROM piggy_bank LIMIT 1")
    fun getPiggyBank(): LiveData<PiggyBank>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePiggyBank(piggyBank: PiggyBank)
}