package com.amarant.apps.budgetapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.ListItemDateBinding
import com.amarant.apps.budgetapp.databinding.ListItemTodayEntryBinding
import com.amarant.apps.budgetapp.databinding.ListItemTodayEntryHiddenBinding
import com.amarant.apps.budgetapp.entities.BudgetUI
import com.amarant.apps.budgetapp.entities.ReportsItem
import com.amarant.apps.budgetapp.util.Constants
import com.amarant.apps.budgetapp.util.NumberUtils

class ReportsAdapter : ListAdapter<ReportsItem, RecyclerView.ViewHolder>(ReportsDiffItemCallback()) {

    var onItemClickListener: ((BudgetUI) -> Unit)? = null
    var onItemLongClickListener: ((BudgetUI) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when(viewType) {
            VIEW_TYPE_DATE -> {
                val binding = ListItemDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return DateViewHolder(binding)
            }
            VIEW_TYPE_HIDDEN_ENTRY -> {
                val binding = ListItemTodayEntryHiddenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ReportsHiddenViewHolder(binding)
            }
            else -> {
                val binding = ListItemTodayEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ReportsViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ReportsItem.Entry -> {
                val budgetItem = item.entry
                val context = holder.itemView.context
                val categoryImageResId = budgetItem.budget.category.iconRes
                val greenColor = ContextCompat.getColor(context, R.color.positive_green)
                val redColor = ContextCompat.getColor(context, R.color.negative_red)
                val formattedAmount = NumberUtils.formatNumberWithThousandsSeparator(budgetItem.budget.amount.toDouble())
                if (item.entry.isHidden) {
                    with(holder as ReportsHiddenViewHolder) {
                        binding.imgCategory.setImageResource(categoryImageResId)
                        binding.tvTitle.text = budgetItem.budget.purpose
                        binding.tvCategory.text = budgetItem.budget.category.getLocalizedName(context)
                        binding.tvAmount.text = formattedAmount
                        if (budgetItem.budget.creditOrDebit == Constants.CREDIT) {
                            binding.tvAmount.text = context.resources.getString(
                                R.string.plus_placeholder,
                                formattedAmount
                            )
                        } else {
                            binding.tvAmount.text = formattedAmount
                        }
                        binding.root.setOnClickListener {
                            onItemClickListener?.invoke(item.entry)
                        }
                        binding.root.setOnLongClickListener {
                            onItemLongClickListener?.invoke(item.entry)
                            true
                        }
                    }
                } else {
                    with(holder as ReportsViewHolder) {
                        binding.imgCategory.setImageResource(categoryImageResId)
                        binding.tvTitle.text = budgetItem.budget.purpose
                        binding.tvCategory.text = budgetItem.budget.category.getLocalizedName(context)
                        binding.tvAmount.text = formattedAmount
                        if (budgetItem.budget.creditOrDebit == Constants.CREDIT) {
                            binding.tvAmount.setTextColor(greenColor)
                            binding.tvAmount.text = context.resources.getString(
                                R.string.plus_placeholder,
                                formattedAmount
                            )
                        } else {
                            binding.tvAmount.setTextColor(redColor)
                            binding.tvAmount.text = formattedAmount
                        }
                        binding.root.setOnClickListener {
                            onItemClickListener?.invoke(item.entry)
                        }
                        binding.root.setOnLongClickListener {
                            onItemLongClickListener?.invoke(item.entry)
                            true
                        }
                    }
                }
            }
            is ReportsItem.DateHeader -> {
                with(holder as DateViewHolder) {
                    binding.tvDate.text = item.date
                }
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is ReportsItem.DateHeader -> VIEW_TYPE_DATE
            is ReportsItem.Entry -> {
                if (item.entry.isHidden) {
                    VIEW_TYPE_HIDDEN_ENTRY
                } else {
                    VIEW_TYPE_ENTRY
                }
            }
        }
    }

    class ReportsViewHolder(val binding: ListItemTodayEntryBinding) :
        RecyclerView.ViewHolder(binding.root)

    class ReportsHiddenViewHolder(val binding: ListItemTodayEntryHiddenBinding) :
        RecyclerView.ViewHolder(binding.root)

    class DateViewHolder(val binding: ListItemDateBinding) :
        RecyclerView.ViewHolder(binding.root)

    class ReportsDiffItemCallback : DiffUtil.ItemCallback<ReportsItem>() {

        override fun areItemsTheSame(oldItem: ReportsItem, newItem: ReportsItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ReportsItem, newItem: ReportsItem): Boolean {
            return oldItem == newItem
        }
    }

    companion object {

        const val VIEW_TYPE_ENTRY = 100
        const val VIEW_TYPE_DATE = 101
        const val VIEW_TYPE_HIDDEN_ENTRY = 102
    }
}
