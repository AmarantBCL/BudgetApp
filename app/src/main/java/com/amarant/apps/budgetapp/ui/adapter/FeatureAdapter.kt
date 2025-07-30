package com.amarant.apps.budgetapp.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarant.apps.budgetapp.databinding.ListItemFeatureBinding
import com.amarant.apps.budgetapp.entities.Feature

class FeatureAdapter : ListAdapter<Feature, FeatureAdapter.FeatureViewHolder>(FeatureDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val binding =
            ListItemFeatureBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeatureViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        val item = getItem(position)
        with(holder) {
            binding.imgFeature.setImageResource(item.icon)
            binding.lblTitle.text = item.title
            binding.lblDescription.text = item.description
        }
    }

    inner class FeatureViewHolder(val binding: ListItemFeatureBinding) :
        RecyclerView.ViewHolder(binding.root)

    class FeatureDiffCallback : DiffUtil.ItemCallback<Feature>() {

        override fun areItemsTheSame(oldItem: Feature, newItem: Feature): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: Feature, newItem: Feature): Boolean {
            return oldItem == newItem
        }
    }
}
