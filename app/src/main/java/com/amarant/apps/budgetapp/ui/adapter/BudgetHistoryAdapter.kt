package com.amarant.apps.budgetapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.ListItemBudgetHistoryBinding
import com.amarant.apps.budgetapp.entities.BudgetHistory
import com.amarant.apps.budgetapp.util.NumberUtils.formatNumberWithThousandsSeparator
import java.util.Locale

class BudgetHistoryAdapter : ListAdapter<BudgetHistory, BudgetHistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemBudgetHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ListItemBudgetHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BudgetHistory) {
            val context = binding.root.context
            binding.apply {
                ivCategoryIcon.setImageResource(item.category.iconRes)
                tvCategoryName.text = item.category.getLocalizedName(context)
                tvPeriodName.text = item.periodName
                
                tvSpentAmount.text = String.format(Locale.getDefault(), "USD %s", formatNumberWithThousandsSeparator(item.spentAmount))
                tvLimitAmount.text = String.format(Locale.getDefault(), "USD %s", formatNumberWithThousandsSeparator(item.amountLimit))
                
                val isSuccess = item.spentAmount <= item.amountLimit
                tvStatus.text = if (isSuccess) "KEPT" else "EXCEEDED"
                tvStatus.setBackgroundResource(R.drawable.bg_period_chip)
                tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(context, if (isSuccess) R.color.positive_green else R.color.negative_red)
                )
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<BudgetHistory>() {
        override fun areItemsTheSame(oldItem: BudgetHistory, newItem: BudgetHistory) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: BudgetHistory, newItem: BudgetHistory) = oldItem == newItem
    }
}
