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
            val categoryImageResId = item.budget.category.iconRes
            val whiteColor = ContextCompat.getColor(context, R.color.primary_white)
            val grayColor = ContextCompat.getColor(context, R.color.secondary_gray)
            val greenColor = ContextCompat.getColor(context, R.color.positive_green)
            val redColor = ContextCompat.getColor(context, R.color.negative_red)
            val formattedAmount = NumberUtils.formatNumberWithThousandsSeparator(item.budget.amount.toDouble())
            binding.imgCategory.setImageResource(categoryImageResId)
            binding.tvTitle.text = item.budget.purpose
            binding.tvCategory.text = item.budget.category.getLocalizedName(context)
            binding.tvAmount.text = formattedAmount
            if (item.budget.creditOrDebit == Constants.CREDIT) {
                binding.tvAmount.setTextColor(greenColor)
                binding.tvAmount.text = context.resources.getString(
                    R.string.plus_placeholder,
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