package com.amarant.apps.budgetapp.ui.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentProfileBinding
import com.amarant.apps.budgetapp.entities.Profile
import com.amarant.apps.budgetapp.ui.viewmodels.ProfileViewModel
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_NAME
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_PIN_VALUE_KEY
import com.amarant.apps.budgetapp.util.MessageUtils
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.content.edit

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupCurrencyDropdown()
        observeProfile()
        setupClickListeners()
        displayAppVersion()
        setupPinToggle()
    }

    private fun setupToolbar() {
//        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
//        binding.toolbar.setNavigationOnClickListener {
//            findNavController().popBackStack()
//        }
    }

    private fun setupCurrencyDropdown() {
        val currencies = resources.getStringArray(R.array.currency)
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_exposed_dropdown, currencies)
        binding.actCurrency.setAdapter(adapter)
    }

    private fun setupPinToggle() {
        val sharedPrefs = requireContext().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        
        binding.switchPin.setOnCheckedChangeListener { _, isChecked ->
            if (binding.switchPin.isPressed) { // Only trigger if user actually pressed the switch
                if (isChecked) {
                    findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToPinFragment(isSettingPin = true))
                } else {
                    sharedPrefs.edit { remove(PREFERENCE_PIN_VALUE_KEY) }
                    MessageUtils.showSnackbarMessage(binding.root,
                        getString(R.string.pin_lock_disabled), getString(R.string.hide))
                }
            }
        }
    }

    private fun observeProfile() {
        val sharedPrefs = requireContext().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        profileViewModel.profileLiveData.observe(viewLifecycleOwner) { profiles ->
            if (profiles.isNotEmpty()) {
                val profile = profiles[0]
                binding.apply {
                    etName.setText(profile.name)
                    etEmail.setText(profile.email)
                    actCurrency.setText(profile.currency, false)
                    etIncome.setText(profile.monthlyIncome.toString())
                    etGoal.setText(profile.monthlyGoal.toString())
                    switchDecimal.isChecked = profile.hideDecimal
                    
                    val hasPin = sharedPrefs.getString(PREFERENCE_PIN_VALUE_KEY, null) != null
                    switchPin.isChecked = hasPin
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            saveProfileChanges()
        }

        binding.btnPrivacyPolicy.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://amarantbcl.github.io/tracksy-privacy/")) // TODO: Replace with real URL
            startActivity(browserIntent)
        }

        binding.btnExportData.setOnClickListener {
            MessageUtils.showSnackbarMessage(binding.root,
                getString(R.string.export_feature_coming_soon), getString(R.string.hide))
        }
    }

    private fun saveProfileChanges() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val currency = binding.actCurrency.text.toString()
        val income = binding.etIncome.text.toString().toDoubleOrNull() ?: 0.0
        val goal = binding.etGoal.text.toString().toDoubleOrNull() ?: 0.0
        val hideDecimal = binding.switchDecimal.isChecked

        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.name_cannot_be_empty)
            return
        }

        profileViewModel.profileLiveData.value?.firstOrNull()?.let { currentProfile ->
            val updatedProfile = currentProfile.copy(
                name = name,
                email = email,
                currency = currency,
                monthlyIncome = income,
                monthlyGoal = goal,
                hideDecimal = hideDecimal
            )
            profileViewModel.insertProfileData(updatedProfile)
            MessageUtils.showSnackbarMessage(binding.root,
                getString(R.string.changes_saved_successfully), getString(R.string.hide))
            findNavController().popBackStack()
        }
    }

    private fun displayAppVersion() {
        try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            binding.tvVersion.text = getString(R.string.version_placeholder, pInfo.versionName)
        } catch (e: Exception) {
            binding.tvVersion.text = getString(R.string.version_1_0_0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
