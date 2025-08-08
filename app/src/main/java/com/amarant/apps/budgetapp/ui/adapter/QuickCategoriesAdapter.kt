package com.amarant.apps.budgetapp.ui.adapter

import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.ListItemCategoryBinding
import com.amarant.apps.budgetapp.databinding.ListItemQuickCategoryBinding
import com.amarant.apps.budgetapp.entities.QuickCategoryItem

class QuickCategoriesAdapter : ListAdapter<QuickCategoryItem, QuickCategoriesAdapter.QuickCategoryViewHolder>(QuickCategoryDiffItemCallback()) {

    var onCategoryClickListener: ((String, Boolean) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuickCategoryViewHolder {
        val binding = ListItemQuickCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuickCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuickCategoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.tvTitle.text = item.name
        holder.binding.imgIcon.setImageResource(item.iconResId)
        if (item.isSelected) {
            holder.binding.imgIcon.setBackgroundResource(R.drawable.frame_selected_category)
        } else {
            val typedValue = TypedValue()
            val attributeId = android.R.attr.selectableItemBackgroundBorderless

            // Разрешаем атрибут и получаем его ресурс ID
            holder.itemView.context.theme.resolveAttribute(attributeId, typedValue, true)

            // Возвращаем ID ресурса drawable, который можно использовать
            val resId = typedValue.resourceId
            holder.binding.imgIcon.setBackgroundResource(resId)
        }
        holder.binding.imgIcon.setOnClickListener {
            onCategoryClickListener?.invoke(item.name, item.isSelected)
        }
    }

    class QuickCategoryViewHolder(val binding: ListItemQuickCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    class QuickCategoryDiffItemCallback : DiffUtil.ItemCallback<QuickCategoryItem>() {

        override fun areItemsTheSame(oldItem: QuickCategoryItem, newItem: QuickCategoryItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: QuickCategoryItem, newItem: QuickCategoryItem): Boolean {
            return oldItem == newItem
        }
    }
}
