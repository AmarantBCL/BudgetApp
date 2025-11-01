package com.amarant.apps.budgetapp.db

import androidx.room.TypeConverter
import com.amarant.apps.budgetapp.entities.CircleColor

class CircleColorConverters {

    @TypeConverter
    fun fromCircleColor(color: CircleColor): String {
        return color.toString()
    }

    @TypeConverter
    fun toCircleColor(colorName: String): CircleColor {
        return CircleColor.fromString(colorName) ?: CircleColor.BLUE
    }
}
