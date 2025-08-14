package com.amarant.apps.budgetapp.entities

import android.content.Context
import androidx.annotation.DrawableRes
import com.amarant.apps.budgetapp.R

enum class Category(
    val dbName: String,
    @DrawableRes val iconRes: Int
) {

    ALL("", R.drawable.circle_all),
    GROCERIES("Groceries", R.drawable.circle_shopping),
    RESTAURANTS("Restaurants", R.drawable.circle_cafe),
    TRANSFERS("Cash", R.drawable.circle_transfer),
    UTILITIES("Utilities", R.drawable.circle_utilities),
    CLOTHING("Clothes", R.drawable.circle_clothing),
    HOME("House", R.drawable.circle_housing),
    TRANSPORT("Car", R.drawable.circle_transportation),
    CARE("Beauty", R.drawable.circle_personal_care),
    HEALTH("Health", R.drawable.circle_health),
    PETS("Pets", R.drawable.circle_pets),
    SUBSCRIPTIONS("Taxi", R.drawable.circle_subscriptions),
    ENTERTAINMENT("Entertainment", R.drawable.circle_entertainment),
    EDUCATION("Education", R.drawable.circle_education),
    TRAVELING("Traveling", R.drawable.circle_traveling),
    GIFTS("Gifts", R.drawable.circle_gifts),
    CHARITY("Charity", R.drawable.circle_charity),
    TAXES("Taxes", R.drawable.circle_taxes),
    RENT("Rent", R.drawable.circle_housing);

    fun getLocalizedName(context: Context): String {
        val array = context.resources.getStringArray(R.array.categories)
        val index = this.ordinal
        return array[index]
    }

    companion object {

        fun fromDbName(dbName: String): Category? {
            return entries.find { it.dbName == dbName }
        }
    }
}