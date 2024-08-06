package com.amarant.apps.budgetapp.ui.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.ItemDetailsBinding
import com.amarant.apps.budgetapp.databinding.ItemDetailsDisabledBinding
import com.amarant.apps.budgetapp.entities.BudgetCategoryDetails
import com.amarant.apps.budgetapp.util.CategoryUtils

class DetailsAdapter : RecyclerView.Adapter<ViewHolder>() {

    var itemClickListener: ((Int, Boolean) -> Unit)? = null

    val sparseArray = SparseBooleanArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == ENABLED_VIEW_TYPE) {
            val binding = ItemDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            EnabledDetailsViewHolder(binding)
        } else {
            val binding = ItemDetailsDisabledBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            DisabledDetailsViewHolder(binding)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val item = differ.currentList[position]
        val drawableRes = getDrawableResource(context, item.category)
        when (holder) {
            is EnabledDetailsViewHolder -> {
                holder.binding.imgIcon.setImageDrawable(drawableRes)
                holder.binding.budgetCategoryName.text =
                    context.getString(
                        R.string.category_placeholder,
                        context.resources.getStringArray(R.array.categories)[CategoryUtils.CATEGORY_MAPPING[item.category]
                            ?: 0]
                    )
                holder.binding.budgetItemAmount.text = item.amount.toString()
                holder.itemView.setOnClickListener {
                    val state = sparseArray.get(position)
                    sparseArray.put(position, !state)
                    itemClickListener?.invoke(position, false)
//                    notifyItemChanged(position)
                }
            }
            is DisabledDetailsViewHolder -> {
                holder.binding.imgIcon.setImageDrawable(drawableRes)
                holder.binding.budgetCategoryName.text =
                    context.getString(
                        R.string.category_placeholder,
                        context.resources.getStringArray(R.array.categories)[CategoryUtils.CATEGORY_MAPPING[item.category]
                            ?: 0]
                    )
                holder.binding.budgetItemAmount.text = item.amount.toString()
                holder.itemView.setOnClickListener {
                    val state = sparseArray.get(position)
                    sparseArray.put(position, !state)
                    itemClickListener?.invoke(position, true)
//                    notifyItemChanged(position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (sparseArray.get(position)) {
            DISABLED_VIEW_TYPE
        } else {
            ENABLED_VIEW_TYPE
        }
    }

    private fun getDrawableResource(context: Context, category: String): Drawable? {
        return if (category != "Unknown") {
            val resId = context.resources.getIdentifier(
                "drawable/cat_${category.lowercase()}",
                "drawable",
                context.packageName
            )
            ContextCompat.getDrawable(
                context,
                resId
            )
        } else {
            ContextCompat.getDrawable(context, R.drawable.cat_unknown)
        }
    }

    inner class EnabledDetailsViewHolder(val binding: ItemDetailsBinding):
        ViewHolder(binding.root)

    inner class DisabledDetailsViewHolder(val binding: ItemDetailsDisabledBinding):
        ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<BudgetCategoryDetails>() {
        override fun areItemsTheSame(
            oldItem: BudgetCategoryDetails,
            newItem: BudgetCategoryDetails
        ): Boolean {
            return oldItem.category == newItem.category
        }

        override fun areContentsTheSame(
            oldItem: BudgetCategoryDetails,
            newItem: BudgetCategoryDetails
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    companion object {

        private const val ENABLED_VIEW_TYPE = 100
        private const val DISABLED_VIEW_TYPE = 101
    }
}