package com.amarant.apps.budgetapp.ui.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.ListItemQuickCategoryBinding
import com.amarant.apps.budgetapp.entities.Category
import com.amarant.apps.budgetapp.entities.QuickCategoryItem

class QuickCategoriesAdapter : ListAdapter<QuickCategoryItem, QuickCategoriesAdapter.QuickCategoryViewHolder>(QuickCategoryDiffItemCallback()) {

    var onCategoryClickListener: ((Category) -> Unit)? = null
    var onCategoryMultiClickListener: ((Category, Boolean) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuickCategoryViewHolder {
        val binding = ListItemQuickCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuickCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuickCategoryViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context
        with(holder) {
            binding.tvTitle.text = if (item.category.name.isEmpty()) context.getString(R.string.all) else item.category.getLocalizedName(context)
            binding.imgIcon.setImageResource(item.category.iconRes)
            if (item.isSelected) {
                binding.imgIcon.setBackgroundResource(R.drawable.frame_selected_category)
            } else {
                val typedValue = TypedValue()
                val attributeId = android.R.attr.selectableItemBackgroundBorderless
                itemView.context.theme.resolveAttribute(attributeId, typedValue, true)
                val resId = typedValue.resourceId
                binding.imgIcon.setBackgroundResource(resId)
            }
            binding.imgIcon.setOnClickListener {
                onCategoryClickListener?.invoke(item.category)
                onCategoryMultiClickListener?.invoke(item.category, item.isSelected)
            }
        }
    }

    class QuickCategoryViewHolder(val binding: ListItemQuickCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    class QuickCategoryDiffItemCallback : DiffUtil.ItemCallback<QuickCategoryItem>() {

        override fun areItemsTheSame(oldItem: QuickCategoryItem, newItem: QuickCategoryItem): Boolean {
            return oldItem.category == newItem.category
        }

        override fun areContentsTheSame(oldItem: QuickCategoryItem, newItem: QuickCategoryItem): Boolean {
            return oldItem == newItem
        }
    }
}
