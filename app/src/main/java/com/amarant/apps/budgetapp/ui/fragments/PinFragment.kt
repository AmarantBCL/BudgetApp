package com.amarant.apps.budgetapp.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentPinBinding
import com.amarant.apps.budgetapp.ui.MainActivity
import com.amarant.apps.budgetapp.util.Constants.SNACKBAR_PIN_DURATION
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar

class PinFragment : Fragment() {

    private var _binding: FragmentPinBinding? = null
    private val binding: FragmentPinBinding
        get() = _binding ?: throw RuntimeException("FragmentPinBinding == null")

    private var digitEditTexts = mutableListOf<EditText>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toggleBottomNavigationMenu(true)
        initViews()
        setClickListeners()
    }

    private fun toggleBottomNavigationMenu(hide: Boolean) {
        val activity = requireActivity() as MainActivity
        val bottomNavBar = activity.findViewById<BottomNavigationView>(R.id.bottomNavBar)
        val bottomMenu = bottomNavBar.menu
        bottomMenu.findItem(R.id.piggyBankFragment).isEnabled = !hide
        bottomMenu.findItem(R.id.reportsFragment).isEnabled = !hide
    }

    private fun initViews() {
        digitEditTexts.add(0, binding.editPin1)
        digitEditTexts.add(1, binding.editPin2)
        digitEditTexts.add(2, binding.editPin3)
        digitEditTexts.add(3, binding.editPin4)
        binding.editPin1.requestFocus()
    }

    private fun setClickListeners() {
        for ((index, editText) in digitEditTexts.withIndex()) {
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(editable: Editable?) {
                    val enteredPin = editable.toString()
                    if (enteredPin.isNotEmpty()) {
                        if (index < digitEditTexts.size - 1) {
                            digitEditTexts[index + 1].requestFocus()
                        } else {
                            for (e in digitEditTexts) {
                                e.isEnabled = false
                            }
                            toggleBottomNavigationMenu(false)
                            findNavController().navigate(PinFragmentDirections.actionPinFragmentToCalendarFragment(true))
                            Snackbar.make(
                                binding.constraintPin,
                                getString(R.string.success),
                                SNACKBAR_PIN_DURATION
                            ).show()
                        }
                    }
                }
            })
        }
    }
}