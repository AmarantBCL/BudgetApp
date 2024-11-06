package com.amarant.apps.budgetapp.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.entities.HistoryItem
import com.amarant.apps.budgetapp.entities.PiggyBank
import com.amarant.apps.budgetapp.entities.Profile

@Database(
    entities = [Budget::class, Profile::class, PiggyBank::class, HistoryItem::class],
    version = 5,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 4, to = 5)]
)
abstract class BudgetDatabase : RoomDatabase() {

    abstract fun getBudgetDao(): BudgetDao

    abstract fun getProfileDao(): ProfileDao

    abstract fun getPiggyBank(): PiggyBankDao

    abstract fun getHistoryDao(): HistoryDao
}