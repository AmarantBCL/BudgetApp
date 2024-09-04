package com.amarant.apps.budgetapp.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.entities.PiggyBank
import com.amarant.apps.budgetapp.entities.Profile

@Database(
    entities = [Budget::class, Profile::class, PiggyBank::class],
    version = 4,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 3, to = 4)]
)
abstract class BudgetDatabase : RoomDatabase() {

    abstract fun getBudgetDao(): BudgetDao

    abstract fun getProfileDao(): ProfileDao

    abstract fun getPiggyBank(): PiggyBankDao
}