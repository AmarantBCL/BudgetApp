package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentAllCategoriesBinding

class AllCategoriesFragment : Fragment() {

    private var _binding: FragmentAllCategoriesBinding? = null
    private val binding: FragmentAllCategoriesBinding
        get() = _binding ?: throw RuntimeException("FragmentAllCategoriesBinding == null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }
}