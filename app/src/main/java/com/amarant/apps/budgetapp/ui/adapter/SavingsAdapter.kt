package com.amarant.apps.budgetapp.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.ListItemTargetBinding
import com.amarant.apps.budgetapp.entities.Saving
import com.amarant.apps.budgetapp.util.NumberUtils.formatDecimal
import com.amarant.apps.budgetapp.util.NumberUtils.formatNumberWithThousandsSeparator

class SavingsAdapter : ListAdapter<Saving, SavingsAdapter.SavingViewHolder>(SavingDiffItemCallback()) {

    var onSavingLongClickListener: ((Saving) -> Unit)? = null
    var onSavingAddClickListener: ((Saving) -> Unit)? = null
    var onSavingSubtractClickListener: ((Saving) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavingViewHolder {
        val binding = ListItemTargetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SavingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SavingViewHolder, position: Int) {
        val item = getItem(position)
        val currentSum = formatNumberWithThousandsSeparator(item.saved.toDouble())
        val targetSum = formatNumberWithThousandsSeparator(item.target.toDouble())
        val percent = item.saved / item.target * 100.0
        val currencySymbol = item.currency.first()
        val toGo = formatNumberWithThousandsSeparator((item.target - item.saved).toDouble())
        val currentTimestamp = System.currentTimeMillis()
        val dueTo = (item.dueTo - currentTimestamp).toFloat() / 1000 / 60 / 60 / 24
        val color = ContextCompat.getColor(holder.itemView.context, item.circleColor)
        with(holder.binding) {
            val context = holder.itemView.context
            ImageViewCompat.setImageTintList(imgColorCircle, ColorStateList.valueOf(color))
            tvTargetName.text = item.title
            chipCurrency.text = item.currency
            tvProgress.text = context.getString(R.string.saving_progress_placeholder, currentSum, targetSum)
            tvPercent.text = context.getString(R.string.percent_placeholder, formatDecimal(percent))
            tvToGo.text = context.getString(R.string.currency_and_amount_to_go_placeholder, currencySymbol, toGo)
            tvDaysLeft.text = context.getString(R.string.days_left_placeholder, Math.round(dueTo).toString())
            pbProgress.progress = percent.toInt()
//            if (item.saved >= item.target) {
//                tvProgress.setTextColor(ContextCompat.getColor(context, R.color.positive_green))
//            } else if (item.saved > 0) {
//                tvProgress.setTextColor(ContextCompat.getColor(context, R.color.amber))
//            } else {
//                tvProgress.setTextColor(ContextCompat.getColor(context, R.color.primary_white))
//            }
            cardSaving.setOnLongClickListener {
                onSavingLongClickListener?.invoke(item)
                true
            }
            btnAdd.setOnClickListener {
                onSavingAddClickListener?.invoke(item)
            }
            btnSubtract.setOnClickListener {
                onSavingSubtractClickListener?.invoke(item)
            }
        }
    }

    class SavingViewHolder(val binding: ListItemTargetBinding) : RecyclerView.ViewHolder(binding.root)

    class SavingDiffItemCallback : DiffUtil.ItemCallback<Saving>() {

        override fun areItemsTheSame(oldItem: Saving, newItem: Saving): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Saving, newItem: Saving): Boolean {
            return oldItem == newItem
        }
    }
}
