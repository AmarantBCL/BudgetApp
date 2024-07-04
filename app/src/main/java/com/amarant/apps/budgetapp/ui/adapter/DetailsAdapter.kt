package com.amarant.apps.budgetapp.ui.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.ItemDetailsBinding
import com.amarant.apps.budgetapp.entities.BudgetCategoryDetails

class DetailsAdapter() : RecyclerView.Adapter<DetailsAdapter.DetailsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsViewHolder {
        return DetailsViewHolder(ItemDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DetailsViewHolder, position: Int) {
        val context = holder.itemView.context
        with(holder) {
            with(differ.currentList[position]) {
                if (category != "Unknown") {
                    val resId = context.resources.getIdentifier(
                        "drawable/cat_${category.lowercase()}",
                        "drawable",
                        context.packageName
                    )
                    binding.imgIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            resId
                        )
                    )
                } else {
                    binding.imgIcon.setImageResource(R.drawable.cat_unknown)
                }
                binding.budgetCategoryName.text = "$category :"
                binding.budgetItemAmount.text = amount.toString()
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class DetailsViewHolder(val binding: ItemDetailsBinding): RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<BudgetCategoryDetails>() {
        override fun areItemsTheSame(oldItem: BudgetCategoryDetails, newItem: BudgetCategoryDetails): Boolean {
            return oldItem.category == newItem.category
        }

        override fun areContentsTheSame(oldItem: BudgetCategoryDetails, newItem: BudgetCategoryDetails): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)
}