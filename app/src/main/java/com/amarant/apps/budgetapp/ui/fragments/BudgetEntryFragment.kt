package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.amarant.apps.budgetapp.databinding.FragmentBudgetEntryBinding

class BudgetEntryFragment : Fragment() {

    private var _binding: FragmentBudgetEntryBinding? = null
    private val binding: FragmentBudgetEntryBinding
        get() = _binding ?: throw RuntimeException("FragmentBudgetEntryBinding == null")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetEntryBinding.inflate(inflater, container, false)
        return binding.root
    }
}