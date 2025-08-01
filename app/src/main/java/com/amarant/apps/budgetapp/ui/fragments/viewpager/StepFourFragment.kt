package com.amarant.apps.budgetapp.ui.fragments.viewpager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentStepFourBinding
import com.amarant.apps.budgetapp.databinding.FragmentStepThreeBinding
import com.amarant.apps.budgetapp.entities.CategoryItem
import com.amarant.apps.budgetapp.ui.adapter.CategoriesAdapter

class StepFourFragment : Fragment() {

    private var _binding: FragmentStepFourBinding? = null
    private val binding: FragmentStepFourBinding
        get() = _binding ?: throw RuntimeException("FragmentStepFourBinding == null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStepFourBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() {
        val categoriesAdapter = CategoriesAdapter()
        binding.recyclerCategories.adapter = categoriesAdapter
        val data = listOf(
            CategoryItem("Food & Dining", true),
            CategoryItem("Transportation", true),
            CategoryItem("Housing", true),
            CategoryItem("Shopping", true),
            CategoryItem("Entertainment", false),
            CategoryItem("Healthcare", false),
            CategoryItem("Education", false),
            CategoryItem("Utilities", false),
            CategoryItem("Taxes", false),
            CategoryItem("Rent", false),
            CategoryItem("Automobile", false),
            CategoryItem("Pets", false),
            CategoryItem("Clothing", false),
            CategoryItem("Traveling", false),
            CategoryItem("Entertainment", false),
            CategoryItem("Cafes & Restaurants", false)
        )
        categoriesAdapter.submitList(data)
    }
}