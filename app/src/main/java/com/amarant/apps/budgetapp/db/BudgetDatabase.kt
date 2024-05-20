package com.amarant.apps.budgetapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.entities.Profile

@Database(entities = [Budget::class, Profile::class], version = 1, exportSchema = false)
abstract class BudgetDatabase : RoomDatabase() {

    abstract fun getBudgetDao(): BudgetDao
    abstract fun getProfileDao(): ProfileDao


}