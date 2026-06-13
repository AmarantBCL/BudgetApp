package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentOnboardingBinding
import com.amarant.apps.budgetapp.entities.PiggyBank
import com.amarant.apps.budgetapp.ui.MainActivity
import com.amarant.apps.budgetapp.ui.adapter.OnboardingAdapter
import com.amarant.apps.budgetapp.ui.viewmodels.OnboardingViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.PiggyBankViewModel
import com.amarant.apps.budgetapp.ui.viewmodels.ProfileViewModel

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding: FragmentOnboardingBinding
        get() = _binding ?: throw RuntimeException("FragmentOnboardingBinding == null")

    private val onboardingViewModel: OnboardingViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val piggyBankViewModel: PiggyBankViewModel by activityViewModels()

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
        observeViewModel()
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
        binding.viewPagerOnboard.offscreenPageLimit = 3
        binding.viewPagerOnboard.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.btnPrevious.isEnabled = position > 0
                binding.tvStep.text = getString(R.string.onboarding_step_placeholder, position + 1, onboardingAdapter.itemCount)
                binding.pbarStep.setProgress(position + 1, true)
                when(position) {
                    1 -> onboardingViewModel.updateNextButtonState()
                    2 -> onboardingViewModel.updateNextButtonStateFromCurrency()
                    else -> onboardingViewModel.setNextButtonState(true)
                }
//                binding.btnNext.text = if (position == 4) "Get Started" else "Next"
            }
        })
    }

    private fun setClickListeners() {
        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPagerOnboard.currentItem
            if (currentItem < 3) {
                binding.viewPagerOnboard.setCurrentItem(currentItem + 1, true)
            } else {
                val profile = onboardingViewModel.buildAndSaveUserProfile()
                if (profile != null) {
                    profileViewModel.insertProfileData(profile)
//                    piggyBankViewModel.updatePiggyBank(
//                        PiggyBank(
//                        1,
//                        0,
//                        0,
//                        0,
//                        0)
//                    )
                    findNavController().graph.setStartDestination(R.id.calendarFragment)
                    findNavController().navigate(
                        OnboardingFragmentDirections.actionOnboardingFragmentToCalendarFragment()
                    )
                } else {
                    (requireActivity() as MainActivity).showSnackbarMessage(binding.btnNext, "Error in creating the profile.")
                }
            }
        }
        binding.btnPrevious.setOnClickListener {
            val currentItem = binding.viewPagerOnboard.currentItem
            if (currentItem > 0) {
                binding.viewPagerOnboard.setCurrentItem(currentItem - 1, true)
            }
        }
    }

    private fun observeViewModel() {
        onboardingViewModel.isNextButtonEnabled.observe(viewLifecycleOwner) {
            binding.btnNext.isEnabled = it
        }
    }
}