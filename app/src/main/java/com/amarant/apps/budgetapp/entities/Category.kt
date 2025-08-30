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
    TRANSFERS("Transfers", R.drawable.circle_transfer), // form. Cash
    INCOME("Income", R.drawable.circle_income), // NEW
    UTILITIES("Utilities", R.drawable.circle_utilities),
    CLOTHING("Clothes", R.drawable.circle_clothing),
    HOME("Home", R.drawable.circle_housing), // form. House
    TRANSPORT("Transportation", R.drawable.circle_transportation), // form. Car
    CARE("Beauty", R.drawable.circle_personal_care),
    HEALTH("Health", R.drawable.circle_health),
    PETS("Pets", R.drawable.circle_pets),
    SUBSCRIPTIONS("Subscriptions", R.drawable.circle_subscriptions), // form. Taxi
    ENTERTAINMENT("Entertainment", R.drawable.circle_entertainment),
    EDUCATION("Education", R.drawable.circle_education),
    TRAVELING("Traveling", R.drawable.circle_traveling),
    GIFTS("Gifts", R.drawable.circle_gifts),
    CHARITY("Charity", R.drawable.circle_charity),
    TAXES("Taxes", R.drawable.circle_taxes),
    RENT("Rent", R.drawable.circle_rental),
    CHILDREN("Children", R.drawable.circle_children);

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