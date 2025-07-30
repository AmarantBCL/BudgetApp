package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentProfileBinding
import com.amarant.apps.budgetapp.databinding.FragmentSplashBinding
import com.amarant.apps.budgetapp.ui.viewmodels.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding: FragmentSplashBinding
        get() = _binding ?: throw RuntimeException("FragmentSplashBinding == null")

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        Log.d("WTF", "SplashFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        profileViewModel.profileLiveData.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                findNavController().navigate(
                    R.id.action_splashFragment_to_profileFragment,
                    null,
                    navOptions {
                        popUpTo(R.id.splashFragment) {
                            inclusive = true
                        }
                    })
            } else {

            }
        }
    }
}