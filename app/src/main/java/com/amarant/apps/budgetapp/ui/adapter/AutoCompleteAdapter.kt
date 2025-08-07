package com.amarant.apps.budgetapp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.amarant.apps.budgetapp.R

class AutoCompleteAdapter(
    context: Context,
    private val items: Array<String>,
    private val icons: Array<Int> // Массив ID ресурсов иконок
) : ArrayAdapter<String>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.list_item_expense_income_exposed_dropdown,
            parent,
            false
        )

        val textView = view.findViewById<TextView>(R.id.text)
        val iconView = view.findViewById<ImageView>(R.id.icon)

        textView.text = items[position]
        iconView.setImageDrawable(ContextCompat.getDrawable(context, icons[position]))

        return view
    }
}
