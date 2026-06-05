package com.amarant.apps.budgetapp.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.amarant.apps.budgetapp.ui.fragments.viewpager.StepFiveFragment
import com.amarant.apps.budgetapp.ui.fragments.viewpager.StepFourFragment
import com.amarant.apps.budgetapp.ui.fragments.viewpager.StepOneFragment
import com.amarant.apps.budgetapp.ui.fragments.viewpager.StepThreeFragment
import com.amarant.apps.budgetapp.ui.fragments.viewpager.StepTwoFragment

class OnboardingAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return ONBOARDING_STEPS
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            1 -> StepTwoFragment()
            2 -> StepThreeFragment()
//            3 -> StepFourFragment()
            3 -> StepFiveFragment()
            else -> StepOneFragment()
        }
    }

    companion object {
        private const val ONBOARDING_STEPS = 4
    }
}