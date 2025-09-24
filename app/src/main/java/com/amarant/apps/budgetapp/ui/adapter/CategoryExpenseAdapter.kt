package com.amarant.apps.budgetapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.ListItemCategoryExpenseBinding
import com.amarant.apps.budgetapp.entities.Category
import com.amarant.apps.budgetapp.entities.CategoryExpense
import com.amarant.apps.budgetapp.util.NumberUtils

class CategoryExpenseAdapter : ListAdapter<CategoryExpense, CategoryExpenseAdapter.CategoryExpenseViewHolder>(CategoryExpenseDiffItemCallback()) {

    var onCategoryExpenseClickListener: ((Category) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryExpenseViewHolder {
        val binding = ListItemCategoryExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryExpenseViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context
        with(holder.binding) {
            val entriesString = context.resources.getQuantityString(R.plurals.entries_count,
                item.entries, item.entries)
            val formattedAmount = NumberUtils.formatNumberWithThousandsSeparator(item.amount.toDouble())
            imgCategory.setImageResource(item.category.iconRes)
            tvCategory.text = item.category.getLocalizedName(context)
            tvEntries.text = entriesString
            tvPercent.text = context.resources.getString(R.string.percent_placeholder,
                NumberUtils.formatDecimal(item.percent))
            if (item.amount > 0) {
                tvAmount.text = context.resources.getString(
                    R.string.plus_placeholder,
                    formattedAmount
                )
                tvAmount.setTextColor(ContextCompat.getColor(context, R.color.positive_green))
            } else {
                tvAmount.text = formattedAmount
                tvAmount.setTextColor(ContextCompat.getColor(context, R.color.negative_red))
            }
            root.setOnClickListener {
                onCategoryExpenseClickListener?.invoke(item.category)
            }
        }
    }

    class CategoryExpenseViewHolder(val binding: ListItemCategoryExpenseBinding) : RecyclerView.ViewHolder(binding.root)

    class CategoryExpenseDiffItemCallback : DiffUtil.ItemCallback<CategoryExpense>() {

        override fun areItemsTheSame(oldItem: CategoryExpense, newItem: CategoryExpense): Boolean {
            return oldItem.category == newItem.category
        }

        override fun areContentsTheSame(oldItem: CategoryExpense, newItem: CategoryExpense): Boolean {
            return oldItem == newItem
        }
    }
}