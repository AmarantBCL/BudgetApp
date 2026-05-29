package com.amarant.apps.budgetapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.databinding.ListItemBudgetProgressBinding
import com.amarant.apps.budgetapp.entities.BudgetWithProgress
import com.amarant.apps.budgetapp.entities.CategoryBudget
import com.amarant.apps.budgetapp.util.NumberUtils.formatNumberWithThousandsSeparator
import com.amarant.apps.budgetapp.R
import java.util.Locale

class BudgetPlanningAdapter(
    private val onDeleteClick: (CategoryBudget) -> Unit,
    private val onEditClick: (CategoryBudget) -> Unit
) : ListAdapter<BudgetWithProgress, BudgetPlanningAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemBudgetProgressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ListItemBudgetProgressBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BudgetWithProgress) {
            val context = binding.root.context
            binding.apply {
                ivCategoryIcon.setImageResource(item.budget.category.iconRes)
                tvCategoryName.text = item.budget.category.getLocalizedName(context)
//                tvPeriod.text = item.budget.period.lowercase()
                val periodIndex = if (item.budget.period == "Weekly") 0 else 1
                chipPeriod.text = context.resources.getStringArray(R.array.budget_periods)[periodIndex]
                
                tvProgressPercent.text = String.format(Locale.getDefault(), "%.1f%%", item.progress)
                pbProgress.progress = item.progress.toInt()
                
//                tvSpentAmount.text = String.format(Locale.getDefault(), "USD %.2f", item.spent)
//                tvLimitAmount.text = String.format(Locale.getDefault(), "USD %.2f", item.budget.amountLimit)
//                tvRemainingAmount.text = String.format(Locale.getDefault(), "USD %.2f", item.remaining)
                val spent = formatNumberWithThousandsSeparator(item.spent)// String.format(Locale.getDefault(), "%.0f", item.spent)
                val limit = formatNumberWithThousandsSeparator(item.budget.amountLimit) //String.format(Locale.getDefault(), "%.0f", item.budget.amountLimit)
                tvProgress.text = "$spent / $limit"
                tvRemaining.text = context.getString(
                    R.string.remaining,
                    formatNumberWithThousandsSeparator(item.remaining)
                )//"${String.format(Locale.getDefault(), "%.0f", item.remaining)} remaining"
                
                btnDelete.setOnClickListener { onDeleteClick(item.budget) }
                btnEdit.setOnClickListener { onEditClick(item.budget) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<BudgetWithProgress>() {
        override fun areItemsTheSame(oldItem: BudgetWithProgress, newItem: BudgetWithProgress): Boolean {
            return oldItem.budget.id == newItem.budget.id
        }

        override fun areContentsTheSame(oldItem: BudgetWithProgress, newItem: BudgetWithProgress): Boolean {
            return oldItem == newItem
        }
    }
}
