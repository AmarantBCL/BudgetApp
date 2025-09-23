package com.amarant.apps.budgetapp.entities

import android.content.Context
import android.util.Log
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.amarant.apps.budgetapp.R

enum class Category(
    val dbName: String,
    @DrawableRes val iconRes: Int,
    @DrawableRes val rawIconRes: Int,
    @ColorRes val color: Int
) {

    ALL("", R.drawable.circle_all, R.drawable.ic_all, R.color.primary_white),
    GROCERIES("Groceries", R.drawable.circle_shopping, R.drawable.ic_shopping, R.color.light_green),
    RESTAURANTS("Restaurants", R.drawable.circle_cafe, R.drawable.ic_coffee, R.color.orange),
    TRANSFERS("Transfers", R.drawable.circle_transfer, R.drawable.ic_credit_card, R.color.black_white), // form. Cash
    INCOME("Income", R.drawable.circle_income, R.drawable.ic_income, R.color.income), // NEW
    UTILITIES("Utilities", R.drawable.circle_utilities, R.drawable.ic_thunder, R.color.amber),
    CLOTHING("Clothes", R.drawable.circle_clothing, R.drawable.ic_tshirt, R.color.violet),
    HOME("Home", R.drawable.circle_housing, R.drawable.ic_home, R.color.forest_green), // form. House
    TRANSPORTATION("Transportation", R.drawable.circle_transportation, R.drawable.ic_car, R.color.blue), // form. Car
    BEAUTY("Beauty", R.drawable.circle_personal_care, R.drawable.ic_scissors, R.color.beauty),
    HEALTH("Health", R.drawable.circle_health, R.drawable.ic_heart, R.color.red),
    PETS("Pets", R.drawable.circle_pets, R.drawable.ic_pets, R.color.light_orange),
    SUBSCRIPTIONS("Subscriptions", R.drawable.circle_subscriptions, R.drawable.ic_smartphone, R.color.cyan), // form. Taxi
    ENTERTAINMENT("Entertainment", R.drawable.circle_entertainment, R.drawable.ic_joystick, R.color.pink),
    EDUCATION("Education", R.drawable.circle_education, R.drawable.ic_graduation, R.color.deep_blue),
    TRAVELING("Traveling", R.drawable.circle_traveling, R.drawable.ic_plane, R.color.sky),
    GIFTS("Gifts", R.drawable.circle_gifts, R.drawable.ic_gift, R.color.rose),
    CHARITY("Charity", R.drawable.circle_charity, R.drawable.ic_hand_heart, R.color.teal),
    TAXES("Taxes", R.drawable.circle_taxes, R.drawable.ic_document, R.color.state_gray),
    RENT("Rent", R.drawable.circle_rental, R.drawable.ic_key, R.color.deep_orange),
    CHILDREN("Children", R.drawable.circle_children, R.drawable.ic_baby, R.color.body),
    SPORTS("Sports", R.drawable.circle_sports, R.drawable.ic_dumbbell, R.color.lime),
    MUSIC("Music", R.drawable.circle_music, R.drawable.ic_note, R.color.indigo),
    APPLIANCES("Appliances", R.drawable.circle_appliances, R.drawable.ic_tv, R.color.gray_hound),
    RENOVATION("Renovation", R.drawable.circle_renovation, R.drawable.ic_hammer, R.color.brown),
    FLOWERS("Flowers", R.drawable.circle_flowers, R.drawable.ic_flower, R.color.flowers),
    LOANS("Loans", R.drawable.circle_loans, R.drawable.ic_loan, R.color.loan),
    FURNITURE("Furniture", R.drawable.circle_furniture, R.drawable.ic_sofa, R.color.furniture),
    WORK_BUSINESS("Work_Business", R.drawable.circle_business, R.drawable.ic_briefcase, R.color.business),
    SAVINGS("Savings", R.drawable.circle_savings, R.drawable.ic_savings, R.color.savings),
    EVENTS("Events", R.drawable.circle_events, R.drawable.ic_event, R.color.event),
    OTHER("Other", R.drawable.circle_other, R.drawable.ic_other, R.color.primary_white);

    fun getLocalizedName(context: Context): String {
        val array = context.resources.getStringArray(R.array.categories)
        val index = this.ordinal
        Log.e("WTF", this.toString())
        return array[index]
    }

    companion object {

        fun fromDbName(dbName: String): Category? {
            return entries.find { it.dbName == dbName }
        }
    }
}