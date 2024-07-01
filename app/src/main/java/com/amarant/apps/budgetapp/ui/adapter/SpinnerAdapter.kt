package com.amarant.apps.budgetapp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.amarant.apps.budgetapp.R

class SpinnerAdapter(
    context: Context,
    private val items: List<SpinnerItem>
) : ArrayAdapter<SpinnerItem>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, R.layout.item_spinner)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, R.layout.item_spinner)
    }

    private fun createViewFromResource(position: Int, convertView: View?, parent: ViewGroup, resource: Int): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)
        val item = getItem(position)
        val iconImageView = view.findViewById<ImageView>(R.id.img_icon)
        val textView = view.findViewById<TextView>(R.id.tv_name)
        item?.let {
            iconImageView.setImageResource(it.iconResId)
            textView.text = it.text
        }
        return view
    }
}
