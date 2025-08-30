package com.amarant.apps.budgetapp.db

import android.util.Log
import androidx.room.TypeConverter
import com.amarant.apps.budgetapp.entities.Category

class CategoryConverters {

    @TypeConverter
    fun fromDbName(dbName: String?): Category? {
        Log.e("WTF", "$dbName")
        return dbName?.let { Category.fromDbName(it) }
    }

    @TypeConverter
    fun toDbName(category: Category?): String? {
        return category?.dbName
    }
}
