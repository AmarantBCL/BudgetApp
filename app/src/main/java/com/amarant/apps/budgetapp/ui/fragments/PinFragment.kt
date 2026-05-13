package com.amarant.apps.budgetapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentPinBinding
import com.amarant.apps.budgetapp.ui.MainActivity
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_IS_PIN_ENTERED_KEY
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_NAME
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_PIN_VALUE_KEY
import com.amarant.apps.budgetapp.util.Constants.SNACKBAR_PIN_DURATION
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar

class PinFragment : Fragment() {

    private var _binding: FragmentPinBinding? = null
    private val binding: FragmentPinBinding
        get() = _binding ?: throw RuntimeException("FragmentPinBinding == null")

    private val args by navArgs<PinFragmentArgs>()

    private var digitEditTexts = mutableListOf<EditText>()
    private var enteredDigitsArray = arrayOfNulls<String>(4)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!args.isSettingPin) {
            toggleBottomNavigationMenu(true)
        }
        initViews()
        setClickListeners()
    }

    override fun onResume() {
        super.onResume()
        binding.editPin1.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.editPin1, InputMethodManager.SHOW_IMPLICIT)
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
        
        binding.lblPin.text = if (args.isSettingPin) {
            getString(R.string.set_pin)
        } else {
            getString(R.string.enter_your_pin)
        }

        binding.editPin1.requestFocus()
        
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (args.isSettingPin) {
                    findNavController().popBackStack()
                } else {
                    requireActivity().finish()
                }
            }
        })
    }

    private fun setClickListeners() {
        for ((index, editText) in digitEditTexts.withIndex()) {
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(editable: Editable?) {
                    val input = editable.toString()
                    if (input.isNotEmpty()) {
                        enteredDigitsArray[index] = input
                        if (index < digitEditTexts.size - 1) {
                            digitEditTexts[index + 1].requestFocus()
                        } else {
                            processPinEntry()
                        }
                    }
                }
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (editText.text.isEmpty() && index > 0) {
                        digitEditTexts[index - 1].requestFocus()
                        digitEditTexts[index - 1].text = null
                        enteredDigitsArray[index - 1] = null
                        return@setOnKeyListener true
                    } else {
                        enteredDigitsArray[index] = null
                    }
                }
                false
            }
        }
    }

    private fun processPinEntry() {
        val enteredPin = enteredDigitsArray.joinToString("")
        if (args.isSettingPin) {
            savePin(enteredPin)
            markPinAsEntered()
            showSnackbar(getString(R.string.pin_saved))
            findNavController().popBackStack()
        } else {
            if (isPinCorrect(enteredPin)) {
                for (e in digitEditTexts) e.isEnabled = false
                toggleBottomNavigationMenu(false)
                markPinAsEntered()
                findNavController().popBackStack()
                showSnackbar(getString(R.string.success))
            } else {
                resetIncorrectPin()
                showSnackbar(getString(R.string.incorrect_pin))
            }
        }
    }

    private fun isPinCorrect(pin: String): Boolean {
        val sharedPrefs = requireContext().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val savedPin = sharedPrefs.getString(PREFERENCE_PIN_VALUE_KEY, null)
        // Fallback to day/month if no PIN is set, or just return false
        return pin == savedPin
    }

    private fun savePin(pin: String) {
        val sharedPrefs = requireContext().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(PREFERENCE_PIN_VALUE_KEY, pin).apply()
    }

    private fun markPinAsEntered() {
        val sharedPrefs = requireContext().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(PREFERENCE_IS_PIN_ENTERED_KEY, true).apply()
    }

    private fun resetIncorrectPin() {
        for (editText in digitEditTexts) editText.text = null
        enteredDigitsArray = arrayOfNulls(4)
        digitEditTexts[0].requestFocus()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.constraintPin, message, SNACKBAR_PIN_DURATION).apply {
            setAction(getString(R.string.hide)) { dismiss() }
        }.show()
    }
}
