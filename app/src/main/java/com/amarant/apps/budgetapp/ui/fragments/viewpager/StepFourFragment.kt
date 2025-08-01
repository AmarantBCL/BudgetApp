package com.amarant.apps.budgetapp.ui.fragments.viewpager

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentStepFourBinding
import com.amarant.apps.budgetapp.databinding.FragmentStepThreeBinding
import com.amarant.apps.budgetapp.entities.CategoryItem
import com.amarant.apps.budgetapp.ui.adapter.CategoriesAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.OnboardingViewModel
import kotlin.random.Random

class StepFourFragment : Fragment() {

    private var _binding: FragmentStepFourBinding? = null
    private val binding: FragmentStepFourBinding
        get() = _binding ?: throw RuntimeException("FragmentStepFourBinding == null")

    private val onboardingViewModel: OnboardingViewModel by activityViewModels()

    private lateinit var categoriesAdapter: CategoriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStepFourBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        categoriesAdapter = CategoriesAdapter()
        binding.recyclerCategories.adapter = categoriesAdapter
        val categoriesArr = resources.getStringArray(R.array.categories)
        onboardingViewModel.initCategories(categoriesArr)
        categoriesAdapter.onCategoryCheckedListener = { categoryName, isChecked ->
            onboardingViewModel.updateCategorySelection(categoryName, isChecked)
        }

    }

    private fun observeViewModel() {
        onboardingViewModel.categories.observe(viewLifecycleOwner) {
            categoriesAdapter.submitList(it)
        }
    }
}