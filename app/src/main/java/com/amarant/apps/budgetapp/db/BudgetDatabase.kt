package com.amarant.apps.budgetapp.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.entities.HistoryItem
import com.amarant.apps.budgetapp.entities.PiggyBank
import com.amarant.apps.budgetapp.entities.Profile

@Database(
    entities = [Budget::class, Profile::class, PiggyBank::class, HistoryItem::class],
    version = 6,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 4, to = 5)]
)
@TypeConverters(CategoryConverters::class)
abstract class BudgetDatabase : RoomDatabase() {

    abstract fun getBudgetDao(): BudgetDao

    abstract fun getProfileDao(): ProfileDao

    abstract fun getPiggyBank(): PiggyBankDao

    abstract fun getHistoryDao(): HistoryDao
}