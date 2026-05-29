package com.amarant.apps.budgetapp.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.amarant.apps.budgetapp.db.BudgetDatabase
import com.amarant.apps.budgetapp.util.Constants.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBudgetDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        BudgetDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(MIGRATION_5_6).addMigrations(MIGRATION_7_8)
        .addMigrations(MIGRATION_8_9).addMigrations(MIGRATION_9_10).build()

    private val MIGRATION_5_6: Migration = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `new_history` (`entry` TEXT PRIMARY KEY NOT NULL, `category` INTEGER NOT NULL)")
            db.execSQL("DROP TABLE history")
            db.execSQL("ALTER TABLE new_history RENAME TO history")
        }
    }

    private val MIGRATION_7_8: Migration = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("UPDATE `budget` SET category = 'Transfers' WHERE category = 'Cash'")
            db.execSQL("UPDATE `budget` SET category = 'Home' WHERE category = 'House'")
            db.execSQL("UPDATE `budget` SET category = 'Transportation' WHERE category = 'Car'")
            db.execSQL("UPDATE `budget` SET category = 'Subscriptions' WHERE category = 'Taxi'")
        }
    }

    private val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `new_savings` (`id` INTEGER PRIMARY KEY NOT NULL, `title` TEXT NOT NULL, `target` DOUBLE NOT NULL, `saved` DOUBLE NOT NULL, `currency` TEXT NOT NULL, `dueTo` INTEGER NOT NULL, `circleColor` TEXT NOT NULL)")
            db.execSQL("INSERT INTO new_savings (id, title, target, saved, currency, dueTo, circleColor) SELECT id, title, target, saved, currency, dueTo, 'BLUE' FROM savings")
            db.execSQL("DROP TABLE savings")
            db.execSQL("ALTER TABLE new_savings RENAME TO savings")
        }
    }

    private val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `category_budgets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `category` TEXT NOT NULL, `amountLimit` REAL NOT NULL, `period` TEXT NOT NULL, `startDate` INTEGER NOT NULL)")
        }
    }

    @Provides
    @Singleton
    fun provideProfileDao(db: BudgetDatabase) = db.getProfileDao()

    @Provides
    @Singleton
    fun provideBudgetDao(db: BudgetDatabase) = db.getBudgetDao()

    @Provides
    @Singleton
    fun providePiggyBankDao(db: BudgetDatabase) = db.getPiggyBank()

    @Provides
    @Singleton
    fun provideHistoryDao(db: BudgetDatabase) = db.getHistoryDao()

    @Provides
    @Singleton
    fun provideCategoryBudgetDao(db: BudgetDatabase) = db.getCategoryBudgetDao()
}