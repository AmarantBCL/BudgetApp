package com.amarant.apps.budgetapp.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.ListItemTodayEntryBinding
import com.amarant.apps.budgetapp.entities.Budget

class TodayBudgetAdapter : ListAdapter<Budget, TodayBudgetAdapter.TodayBudgetViewHolder>(TodayBudgetDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodayBudgetViewHolder {
        val binding = ListItemTodayEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodayBudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodayBudgetViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context
        with(holder) {
            binding.tvTitle.text = item.purpose
            binding.tvCategory.text = item.category
            binding.tvAmount.text = item.amount.toString()
            val categoryImageResId = when (item.category) {
                "Car" -> R.drawable.circle_transportation
                "Restaurants" -> R.drawable.circle_cafe
                "Groceries" -> R.drawable.circle_shopping
                "Rent" -> R.drawable.circle_housing
                "Health" -> R.drawable.circle_health
                "Entertainment" -> R.drawable.circle_entertainment
                "Cash" -> R.drawable.circle_income
                else -> R.drawable.cat_unknown
            }
            binding.imgCategory.setImageResource(categoryImageResId)
            if (item.creditOrDebit == "Credit") {
//                binding.imgType.setImageResource(R.drawable.ic_credit)
                binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.positive_green))
                binding.tvAmount.text = "+${item.amount}"
            } else {
//                binding.imgType.setImageResource(R.drawable.ic_debit)
                binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.negative_red))
            }
        }
    }

    class TodayBudgetViewHolder(val binding: ListItemTodayEntryBinding) : RecyclerView.ViewHolder(binding.root)

    class TodayBudgetDiffItemCallback : DiffUtil.ItemCallback<Budget>() {

        override fun areItemsTheSame(oldItem: Budget, newItem: Budget): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Budget, newItem: Budget): Boolean {
            return oldItem == newItem
        }
    }
}