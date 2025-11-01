package com.amarant.apps.budgetapp.ui.adapter

import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.ListItemColorPaletteBinding
import com.amarant.apps.budgetapp.entities.CircleColor
import com.amarant.apps.budgetapp.entities.ColorPalette

class ColorPaletteAdapter : ListAdapter<ColorPalette, ColorPaletteAdapter.ColorPaletteViewHolder>(ColorPaletteDiffItemCallback()) {

    var onColorClickListener: ((CircleColor) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorPaletteViewHolder {
        val binding = ListItemColorPaletteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ColorPaletteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorPaletteViewHolder, position: Int) {
        val item = getItem(position)
        val color = ContextCompat.getColor(holder.itemView.context, item.color.colorId)
        ImageViewCompat.setImageTintList(holder.binding.imgCircle, ColorStateList.valueOf(color))
        if (item.isSelected) {
            holder.binding.imgCircle.setBackgroundResource(R.drawable.frame_selected_category)
        } else {
            val typedValue = TypedValue()
            val attributeId = android.R.attr.selectableItemBackgroundBorderless
            holder.itemView.context.theme.resolveAttribute(attributeId, typedValue, true)
            val resId = typedValue.resourceId
            holder.binding.imgCircle.setBackgroundResource(resId)
        }
        holder.binding.imgCircle.setOnClickListener {
            onColorClickListener?.invoke(item.color)
        }
    }

    class ColorPaletteViewHolder(val binding: ListItemColorPaletteBinding) : RecyclerView.ViewHolder(binding.root)

    class ColorPaletteDiffItemCallback : DiffUtil.ItemCallback<ColorPalette>() {

        override fun areItemsTheSame(oldItem: ColorPalette, newItem: ColorPalette): Boolean {
            return oldItem.color == newItem.color
        }

        override fun areContentsTheSame(oldItem: ColorPalette, newItem: ColorPalette): Boolean {
            return oldItem == newItem
        }
    }
}