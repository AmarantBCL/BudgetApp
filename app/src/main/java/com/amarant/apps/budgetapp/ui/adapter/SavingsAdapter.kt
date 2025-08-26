package com.amarant.apps.budgetapp.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.ListItemTargetBinding
import com.amarant.apps.budgetapp.entities.Saving
import com.amarant.apps.budgetapp.util.NumberUtils

class SavingsAdapter : ListAdapter<Saving, SavingsAdapter.SavingViewHolder>(SavingDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavingViewHolder {
        val binding = ListItemTargetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SavingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SavingViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            val percent = item.saved / item.target * 100.0
            val toGo = NumberUtils.formatNumberWithThousandsSeparator((item.target - item.saved).toDouble())
            val currentTimestamp = System.currentTimeMillis()
            val dueTo = (item.dueTo - currentTimestamp).toFloat() / 1000 / 60 / 60 / 24
            val drawable = ContextCompat.getDrawable(holder.itemView.context, R.drawable.shape_color_circle)
            drawable?.setTint(ContextCompat.getColor(holder.itemView.context, item.circleColor))
            tvTargetName.text = item.title
            chipCurrency.text = item.currency
            tvProgress.text = "${NumberUtils.formatNumberWithThousandsSeparator(item.saved.toDouble())} / ${NumberUtils.formatNumberWithThousandsSeparator(item.target.toDouble())}"
            tvPercent.text = NumberUtils.formatDecimal(percent) + "% complete"
            tvToGo.text = toGo + " to go"
            tvDaysLeft.text = Math.round(dueTo).toString() + " days left"
            pbProgress.progress = percent.toInt()
            imgColorCircle.setImageDrawable(drawable)
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
