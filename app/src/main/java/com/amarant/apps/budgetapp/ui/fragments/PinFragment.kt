package com.amarant.apps.budgetapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentPinBinding
import com.amarant.apps.budgetapp.ui.MainActivity
import com.amarant.apps.budgetapp.util.Constants
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_IS_PIN_ENTERED_KEY
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_NAME
import com.amarant.apps.budgetapp.util.Constants.SNACKBAR_PIN_DURATION
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar

class PinFragment : Fragment() {

    private var _binding: FragmentPinBinding? = null
    private val binding: FragmentPinBinding
        get() = _binding ?: throw RuntimeException("FragmentPinBinding == null")

    private var digitEditTexts = mutableListOf<EditText>()

    private var enteredDigits = "****"

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
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })
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
                            replaceCharAtIndex(index, enteredPin)
                            digitEditTexts[index + 1].requestFocus()
                        } else {
                            replaceCharAtIndex(index, enteredPin)
                            if (isPinCorrect(enteredDigits)) {
                                for (e in digitEditTexts) {
                                    e.isEnabled = false
                                }
                                toggleBottomNavigationMenu(false)
                                saveEnteredPin()
                                findNavController().popBackStack()
                                Snackbar.make(
                                    binding.constraintPin,
                                    getString(R.string.success),
                                    SNACKBAR_PIN_DURATION
                                ).show()
                            } else {
                                Snackbar.make(
                                    binding.constraintPin,
                                    getString(R.string.incorrect_pin),
                                    SNACKBAR_PIN_DURATION
                                ).show()
                            }
                        }
                    }
                }
            })
        }
    }

    fun replaceCharAtIndex(index: Int, newChar: String) {
        val str = enteredDigits
        enteredDigits = str.substring(0, index) + newChar + str.substring(index + 1)
    }

    private fun isPinCorrect(pin: String): Boolean {
        return pin == "1111"
    }

    private fun saveEnteredPin() {
        val sharedPrefs = requireContext().getSharedPreferences(
            PREFERENCE_NAME,
            Context.MODE_PRIVATE
        )
        sharedPrefs.edit().putBoolean(PREFERENCE_IS_PIN_ENTERED_KEY, true).apply()
    }
}