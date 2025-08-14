package com.amarant.apps.budgetapp.di

import android.content.Context
import androidx.room.Room
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.amarant.apps.budgetapp.db.BudgetDatabase
import com.amarant.apps.budgetapp.db.CategoryConverters
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
    ).addMigrations(MIGRATION_5_6).build()

    private val MIGRATION_5_6: Migration = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `new_history` (`entry` TEXT PRIMARY KEY NOT NULL, `category` INTEGER NOT NULL)")
            database.execSQL("DROP TABLE history")
            database.execSQL("ALTER TABLE new_history RENAME TO history")
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
}