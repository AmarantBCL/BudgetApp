package com.amarant.apps.budgetapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.databinding.ListItemCategoryBinding
import com.amarant.apps.budgetapp.entities.CategoryItem

class CategoriesAdapter : ListAdapter<CategoryItem, CategoriesAdapter.CategoryViewHolder>(CategoryDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ListItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.cboxCategory.text = item.name
        holder.binding.cboxCategory.isChecked = item.isChecked
    }

    class CategoryViewHolder(val binding: ListItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    class CategoryDiffItemCallback : DiffUtil.ItemCallback<CategoryItem>() {

        override fun areItemsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean {
            return oldItem == newItem
        }
    }
}
