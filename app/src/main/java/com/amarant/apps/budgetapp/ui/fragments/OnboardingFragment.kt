package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.amarant.apps.budgetapp.databinding.FragmentOnboardingBinding
import com.amarant.apps.budgetapp.ui.adapter.OnboardingAdapter

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding: FragmentOnboardingBinding
        get() = _binding ?: throw RuntimeException("FragmentOnboardingBinding == null")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPager()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViewPager() {
        val onboardingAdapter = OnboardingAdapter(requireActivity())
        binding.viewPagerOnboard.adapter = onboardingAdapter
        binding.viewPagerOnboard.isUserInputEnabled = false
    }
}