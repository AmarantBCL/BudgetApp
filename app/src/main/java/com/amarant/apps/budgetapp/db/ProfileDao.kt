package com.amarant.apps.budgetapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.amarant.apps.budgetapp.entities.Profile

@Dao
interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfileData(profile: Profile)

    @Query("SELECT * FROM profile ORDER BY id DESC")
    fun getProfileData(): LiveData<List<Profile>>
}