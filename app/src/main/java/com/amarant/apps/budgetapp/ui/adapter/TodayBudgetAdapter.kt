package com.amarant.apps.budgetapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.ListItemTodayEntryBinding
import com.amarant.apps.budgetapp.entities.BudgetUI
import com.amarant.apps.budgetapp.util.Constants
import com.amarant.apps.budgetapp.util.NumberUtils

class TodayBudgetAdapter :
    ListAdapter<BudgetUI, TodayBudgetAdapter.TodayBudgetViewHolder>(TodayBudgetDiffItemCallback()) {

    var onItemClickListener: ((BudgetUI) -> Unit)? = null
    var onItemLongClickListener: ((BudgetUI) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodayBudgetViewHolder {
        val binding =
            ListItemTodayEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodayBudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodayBudgetViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context
        with(holder) {
            val categoryImageResId = when (item.budget.category) {
                "Car" -> R.drawable.circle_transportation
                "Restaurants" -> R.drawable.circle_cafe
                "Groceries" -> R.drawable.circle_shopping
                "Rent" -> R.drawable.circle_housing
                "Health" -> R.drawable.circle_health
                "Entertainment" -> R.drawable.circle_entertainment
                "Cash" -> R.drawable.circle_transfer
                "Taxes" -> R.drawable.circle_taxes
                "Clothes" -> R.drawable.circle_clothing
                "Pets" -> R.drawable.circle_pets
                "Education" -> R.drawable.circle_education
                "Gifts" -> R.drawable.circle_gifts
                "Charity" -> R.drawable.circle_charity
                "Traveling" -> R.drawable.circle_traveling
                "Beauty" -> R.drawable.circle_personal_care
                "Utilities" -> R.drawable.circle_utilities
                "Taxi" -> R.drawable.circle_subscriptions
                "House" -> R.drawable.circle_housing
                else -> R.drawable.circle_all
            }
            val whiteColor = ContextCompat.getColor(context, R.color.primary_white)
            val grayColor = ContextCompat.getColor(context, R.color.secondary_gray)
            val greenColor = ContextCompat.getColor(context, R.color.positive_green)
            val redColor = ContextCompat.getColor(context, R.color.negative_red)
            val formattedAmount = NumberUtils.formatNumberWithThousandsSeparator(item.budget.amount.toDouble())
            binding.imgCategory.setImageResource(categoryImageResId)
            binding.tvTitle.text = item.budget.purpose
            binding.tvCategory.text = item.budget.category
            binding.tvAmount.text = formattedAmount
            if (item.budget.creditOrDebit == Constants.CREDIT) {
                binding.tvAmount.setTextColor(greenColor)
                binding.tvAmount.text = context.resources.getString(
                    R.string.placeholder_plus,
                    formattedAmount
                )
            } else {
                binding.tvAmount.setTextColor(redColor)
                binding.tvAmount.text = formattedAmount
            }
            if (!item.isHidden) {
                binding.tvTitle.setTextColor(whiteColor)
                binding.tvTitle.alpha = 1.0f
                binding.tvCategory.alpha = 1.0f
                binding.tvAmount.alpha = 1.0f
                binding.imgCategory.alpha = 1.0f
            } else {
                binding.tvTitle.setTextColor(grayColor)
                binding.tvTitle.alpha = 0.5f
                binding.tvCategory.alpha = 0.5f
                binding.tvAmount.setTextColor(grayColor)
                binding.tvAmount.alpha = 0.5f
                binding.imgCategory.alpha = 0.5f
            }
            binding.root.setOnClickListener {
                onItemClickListener?.invoke(item)
            }
            binding.root.setOnLongClickListener {
                onItemLongClickListener?.invoke(item)
                true
            }
        }
    }

    class TodayBudgetViewHolder(val binding: ListItemTodayEntryBinding) :
        RecyclerView.ViewHolder(binding.root)

    class TodayBudgetDiffItemCallback : DiffUtil.ItemCallback<BudgetUI>() {

        override fun areItemsTheSame(oldItem: BudgetUI, newItem: BudgetUI): Boolean {
            return oldItem.budget.id == newItem.budget.id
        }

        override fun areContentsTheSame(oldItem: BudgetUI, newItem: BudgetUI): Boolean {
            return oldItem == newItem
        }
    }
}