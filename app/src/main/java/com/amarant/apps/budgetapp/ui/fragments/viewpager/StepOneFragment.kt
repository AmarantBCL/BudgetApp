package com.amarant.apps.budgetapp.ui.fragments.viewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentStepOneBinding
import com.amarant.apps.budgetapp.entities.Feature
import com.amarant.apps.budgetapp.ui.adapter.FeatureAdapter

class StepOneFragment : Fragment() {

    private var _binding: FragmentStepOneBinding? = null
    private val binding: FragmentStepOneBinding
        get() = _binding ?: throw RuntimeException("FragmentStepOneBinding == null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStepOneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val data = listOf(
            Feature(
                resources.getStringArray(R.array.feature_titles)[0],
                resources.getStringArray(R.array.feature_descriptions)[0],
                R.drawable.ic_trend
            ),
            Feature(
                resources.getStringArray(R.array.feature_titles)[1],
                resources.getStringArray(R.array.feature_descriptions)[1],
                R.drawable.ic_piggy_bank
            ),
            Feature(
                resources.getStringArray(R.array.feature_titles)[2],
                resources.getStringArray(R.array.feature_descriptions)[2],
                R.drawable.ic_shield
            )
        )
        val featureAdapter = FeatureAdapter()
        binding.recyclerFeaturesList.adapter = featureAdapter
        featureAdapter.submitList(data)
    }
}