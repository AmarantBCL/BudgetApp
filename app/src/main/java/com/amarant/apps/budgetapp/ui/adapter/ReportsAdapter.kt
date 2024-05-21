package com.amarant.apps.budgetapp.ui.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.ItemBudgetBinding
import com.amarant.apps.budgetapp.entities.Budget
import com.amarant.apps.budgetapp.util.UtilityFunctions.dateMillisToString

class ReportsAdapter(
    val listener: MyOnClickListener
) : RecyclerView.Adapter<ReportsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        with(holder) {
            with(differ.currentList[position]) {
                binding.budgetItemAmount.text = amount.toString()
                binding.budgetItemDate.text = dateMillisToString(date.toLong())
                binding.budgetItemPurpose.text = purpose
                binding.budgetItemPurpose.tooltipText = purpose
                if (creditOrDebit.equals("Credit")) {
                    binding.budgetItemType.setImageResource(R.drawable.ic_credit)
                } else {
                    binding.budgetItemType.setImageResource(R.drawable.ic_debit)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class MyViewHolder(val binding: ItemBudgetBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnLongClickListener {
                val position = adapterPosition
                listener.onClick(position)
                true
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<Budget>() {
        override fun areItemsTheSame(oldItem: Budget, newItem: Budget): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Budget, newItem: Budget): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    interface MyOnClickListener {

        fun onClick(position: Int)
    }
}