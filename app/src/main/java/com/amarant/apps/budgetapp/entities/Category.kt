package com.amarant.apps.budgetapp.entities

import android.content.Context
import androidx.annotation.DrawableRes
import com.amarant.apps.budgetapp.R

enum class Category(
    val dbName: String,
    @DrawableRes val iconRes: Int,
    @DrawableRes val rawIconRes: Int
) {

    ALL("", R.drawable.circle_all, R.drawable.ic_all),
    GROCERIES("Groceries", R.drawable.circle_shopping, R.drawable.ic_shopping),
    RESTAURANTS("Restaurants", R.drawable.circle_cafe, R.drawable.ic_coffee),
    TRANSFERS("Transfers", R.drawable.circle_transfer, R.drawable.ic_credit_card), // form. Cash
    INCOME("Income", R.drawable.circle_income, R.drawable.ic_trend), // NEW
    UTILITIES("Utilities", R.drawable.circle_utilities, R.drawable.ic_thunder),
    CLOTHING("Clothes", R.drawable.circle_clothing, R.drawable.ic_tshirt),
    HOME("Home", R.drawable.circle_housing, R.drawable.ic_home), // form. House
    TRANSPORT("Transportation", R.drawable.circle_transportation, R.drawable.ic_car), // form. Car
    CARE("Beauty", R.drawable.circle_personal_care, R.drawable.ic_scissors),
    HEALTH("Health", R.drawable.circle_health, R.drawable.ic_heart),
    PETS("Pets", R.drawable.circle_pets, R.drawable.ic_pets),
    SUBSCRIPTIONS("Subscriptions", R.drawable.circle_subscriptions, R.drawable.ic_smartphone), // form. Taxi
    ENTERTAINMENT("Entertainment", R.drawable.circle_entertainment, R.drawable.ic_joystick),
    EDUCATION("Education", R.drawable.circle_education, R.drawable.ic_graduation),
    TRAVELING("Traveling", R.drawable.circle_traveling, R.drawable.ic_plane),
    GIFTS("Gifts", R.drawable.circle_gifts, R.drawable.ic_gift),
    CHARITY("Charity", R.drawable.circle_charity, R.drawable.ic_hand_heart),
    TAXES("Taxes", R.drawable.circle_taxes, R.drawable.ic_document),
    RENT("Rent", R.drawable.circle_rental, R.drawable.ic_key),
    CHILDREN("Children", R.drawable.circle_children, R.drawable.ic_baby),
    SPORTS("Sports", R.drawable.circle_sports, R.drawable.ic_dumbbell),
    MUSIC("Music", R.drawable.circle_music, R.drawable.ic_note),
    APPLIANCES("Appliances", R.drawable.circle_appliances, R.drawable.ic_tv);

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