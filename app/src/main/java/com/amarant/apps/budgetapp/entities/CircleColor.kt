package com.amarant.apps.budgetapp.entities

import com.amarant.apps.budgetapp.R

enum class CircleColor(val colorId: Int) {
    BLUE(R.color.deep_blue),
    GREEN(R.color.light_green),
    RED(R.color.red),
    YELLOW(R.color.light_orange),
    PURPLE(R.color.accent_purple),
    CYAN(R.color.cyan),
    PINK(R.color.beauty),
    GRAY(R.color.deep_orange),
    ORANGE(R.color.orange),
    VIOLET(R.color.violet),
    LIME(R.color.lime),
    BROWN(R.color.state_gray);

    companion object {
        fun fromString(colorName: String): CircleColor? {
            return entries.find { it.toString() == colorName }
        }
    }
}