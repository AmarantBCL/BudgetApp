package com.amarant.apps.budgetapp.ui.fragments

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.FragmentProfileBinding
import com.amarant.apps.budgetapp.entities.Profile
import com.amarant.apps.budgetapp.ui.viewmodels.ProfileViewModel
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_NAME
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_PROFILE_EXISTENCE_KEY
import com.amarant.apps.budgetapp.util.InternalStoragePhoto
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding
        get() = _binding ?: throw RuntimeException("FragmentProfileBinding == null")

    private val profileViewModel: ProfileViewModel by viewModels()
    private var filePath: Uri? = null
    private lateinit var bitmap: Bitmap
    private lateinit var myPref: SharedPreferences

    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
            result?.let {
                filePath = it
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source =
                        ImageDecoder.createSource(requireContext().contentResolver!!, it)
                    bitmap = ImageDecoder.decodeBitmap(source)
                }
                saveImageToInternalStorage("profile", bitmap)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myPref = requireContext().getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE)
        if (myPref.contains(PREFERENCE_PROFILE_EXISTENCE_KEY)) {
            changeViewVisibilityPostRegistration()
        } else {
            changeViewVisibilityForRegistration()
        }
        binding.profileImage.setOnClickListener {
            takePhoto.launch("image/*")
        }
        profileViewModel.profileLiveData.observe(viewLifecycleOwner) { profile ->
            if (profile!!.size >= 1) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val listOfImage = loadImageFromInternalStorage()
                    for (i in listOfImage) {
                        if (i.name.contains("profile")) {
                            Glide.with(requireContext()).load(i.bitmap).circleCrop()
                                .into(binding.profileImage)
                        }
                    }
                    binding.bankName.setText(profile!![0].bankName)
                    binding.initialBalance.setText(profile!![0].initialBalance.toString())
                    binding.currentBalance.setText(profile!![0].currentBalance.toString())
                    binding.materialCheckBox.isChecked = profile!![0].primaryBank
                    binding.profileName.setText(profile!![0].name)
                    binding.profileEmail.setText(profile!![0].email)
                }
            } else {
                Toast.makeText(requireContext(), "Complete Profile", Toast.LENGTH_SHORT).show()
            }
        }
        binding.submitProfile.setOnClickListener {
            submitData(
                binding.profileName.text.toString().trim(),
                binding.profileEmail.text.toString().trim(),
                binding.bankName.text.toString().trim(),
                binding.initialBalance.text.toString().trim(),
                binding.materialCheckBox.isChecked
            )
        }
    }

    private fun submitData(
        profileName: String,
        profileEmail: String,
        bankName: String,
        initialBalance: String,
        checked: Boolean
    ) {
        if (profileName.isNullOrBlank() || profileEmail.isNullOrBlank() || bankName.isNullOrBlank() || initialBalance.isNullOrBlank()) {
            Snackbar.make(
                binding.profileConstrain,
                "The fields must not be empty",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        if (filePath == null) {
            Snackbar.make(
                binding.profileConstrain,
                "Select the profile picture",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        profileViewModel.insertProfileData(
            Profile(
                name = profileName,
                email = profileEmail,
                profileImageFilePath = filePath.toString(),
                bankName = bankName,
                currentBalance = initialBalance.toFloat(),
                initialBalance = initialBalance.toFloat(),
                primaryBank = checked
            )
        )
        val editor = myPref.edit()
        editor.putBoolean(PREFERENCE_PROFILE_EXISTENCE_KEY, true)
        editor.apply()
        findNavController().navigate(R.id.action_profileFragment_to_calendarFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun changeViewVisibilityForRegistration() {
        binding.submitProfile.visibility = View.VISIBLE
        binding.updateCurrentBalance.visibility = View.GONE
        binding.balanceLayout.visibility = View.GONE
    }

    private fun changeViewVisibilityPostRegistration() {
        binding.submitProfile.visibility = View.GONE
        binding.updateCurrentBalance.visibility = View.VISIBLE
        binding.balanceLayout.visibility = View.VISIBLE
    }

    private fun saveImageToInternalStorage(fileName: String, bitmap: Bitmap): Boolean {
        return try {
            requireContext().openFileOutput("$fileName.jpg", MODE_PRIVATE).use { outputStream ->
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                    throw IOException("Could not save Bitmap")
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun loadImageFromInternalStorage(): List<InternalStoragePhoto> {
        return withContext(Dispatchers.IO) {
            val files = requireContext().filesDir.listFiles()
            files.filter {
                it.canRead() && it.isFile && it.name.endsWith(".jpg")
            }.map {
                val bytes = it.readBytes()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                InternalStoragePhoto(it.name, bitmap)
            }
        }
    }
}