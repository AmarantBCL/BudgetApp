package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.amarant.apps.budgetapp.databinding.FragmentOnboardingBinding
import com.amarant.apps.budgetapp.ui.adapter.OnboardingAdapter
import com.amarant.apps.budgetapp.ui.fragments.viewpager.StepOneFragment

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
        setClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViewPager() {
        val onboardingAdapter = OnboardingAdapter(requireActivity())
        binding.viewPagerOnboard.adapter = onboardingAdapter
        binding.viewPagerOnboard.isUserInputEnabled = false
        binding.viewPagerOnboard.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.btnPrevious.isEnabled = position > 0
                binding.tvStep.text = "Step ${position + 1} of 5"
                binding.pbarStep.setProgress(position + 1, true)
            }
        })
    }

    private fun setClickListeners() {
        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPagerOnboard.currentItem
            if (currentItem < 4) {
                binding.viewPagerOnboard.setCurrentItem(currentItem + 1, true)
            }
        }
        binding.btnPrevious.setOnClickListener {
            val currentItem = binding.viewPagerOnboard.currentItem
            if (currentItem > 0) {
                binding.viewPagerOnboard.setCurrentItem(currentItem - 1, true)
            }
        }
    }
}