package com.amarant.apps.budgetapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.ListItemCategoryExpenseBinding
import com.amarant.apps.budgetapp.entities.BarChartItem
import com.amarant.apps.budgetapp.util.NumberUtils
import kotlin.math.absoluteValue

class BarChartItemAdapter : ListAdapter<BarChartItem, BarChartItemAdapter.ViewHolder>(BarChartItemDiffCallback()) {

    var onBarChartItemClickListener: ((BarChartItem) -> Unit)? = null
    var isIncome: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemCategoryExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ListItemCategoryExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BarChartItem) {
            val context = binding.root.context
            binding.apply {
                // Reusing the same layout, but adapting for time periods
                imgCategory.setImageResource(R.drawable.ic_period)
                imgCategory.imageTintList = ContextCompat.getColorStateList(context, R.color.accent_purple)
                
                tvCategory.text = item.label
                
                val entriesText = context.resources.getQuantityString(R.plurals.entries_count, item.entries, item.entries)
                tvEntries.text = entriesText
                
                val formattedAmount = NumberUtils.formatNumberWithThousandsSeparator(item.amount.toDouble())
                if (isIncome) {
                    tvAmount.text = context.getString(R.string.plus_placeholder, formattedAmount)
                    tvAmount.setTextColor(ContextCompat.getColor(context, R.color.positive_green))
                } else {
                    tvAmount.text = context.getString(R.string.minus_placeholder, NumberUtils.formatNumberWithThousandsSeparator(item.amount.toDouble().absoluteValue))
                    tvAmount.setTextColor(ContextCompat.getColor(context, R.color.negative_red))
                }
                
                // Percent doesn't really apply here as much as categories, but we can hide it or keep it
                tvPercent.visibility = android.view.View.GONE
                
                root.setOnClickListener {
                    onBarChartItemClickListener?.invoke(item)
                }
            }
        }
    }

    class BarChartItemDiffCallback : DiffUtil.ItemCallback<BarChartItem>() {
        override fun areItemsTheSame(oldItem: BarChartItem, newItem: BarChartItem) = oldItem.label == newItem.label
        override fun areContentsTheSame(oldItem: BarChartItem, newItem: BarChartItem) = oldItem == newItem
    }
}
