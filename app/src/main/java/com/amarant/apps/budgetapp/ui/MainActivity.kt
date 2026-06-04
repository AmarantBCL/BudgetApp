package com.amarant.apps.budgetapp.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.ActivityMainBinding
import com.amarant.apps.budgetapp.ui.viewmodels.ProfileViewModel
import com.amarant.apps.budgetapp.util.Constants.PIN_LOCK_TIMEOUT_MILLIS
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_IS_PIN_ENTERED_KEY
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_LAST_ACTIVE_TIME_KEY
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_NAME
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_PIN_VALUE_KEY
import com.amarant.apps.budgetapp.util.NumberUtils
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        resetPinCode()
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.calendarFragment,
            R.id.piggyBankFragment,
            R.id.statsFragment,
            R.id.reportsFragment,
            R.id.budgetPlanningFragment
        ))
        toolbar.setupWithNavController(navController, appBarConfiguration)
        setBottomNavigation()
        observeProfileSettings()
    }

    private fun observeProfileSettings() {
        profileViewModel.profileLiveData.observe(this) { profiles ->
            if (profiles.isNotEmpty()) {
                val profile = profiles[0]
                NumberUtils.isHideDecimal = profile.hideDecimal
            }
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.option_menu, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
//    }

    override fun onStop() {
        super.onStop()
        val sharedPrefs = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putLong(PREFERENCE_LAST_ACTIVE_TIME_KEY, System.currentTimeMillis()).apply()
    }

    override fun onResume() {
        super.onResume()
        if (::navController.isInitialized) {
            val sharedPrefs = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            val lastActiveTime = sharedPrefs.getLong(PREFERENCE_LAST_ACTIVE_TIME_KEY, 0)
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastActiveTime > PIN_LOCK_TIMEOUT_MILLIS) {
                resetPinCode()
            }
            checkPin()
        }
    }

    fun showSnackbarMessage(view: View, message: String) {
        val snackbar = Snackbar.make(
            view,
            message,
            Snackbar.LENGTH_SHORT
        )
        snackbar.setAction(getString(R.string.hide)) {
            snackbar.dismiss()
        }
        snackbar.show()
    }

    fun setActionBarTitle(text: String) {
        supportActionBar?.title = text
    }

    private fun setBottomNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        binding.bottomNavBar.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            checkPin(destination.id)
            if (destination.id != R.id.reportsFragment) {
                supportActionBar?.title = destination.label
            }
            when (destination.id) {
                R.id.onboardingFragment,
                R.id.splashFragment -> {
                    supportActionBar?.hide()
                    binding.bottomNavBar.visibility = View.GONE
                }
                R.id.fragmentAddEntry,
                R.id.addSavingFragment,
                R.id.addBudgetFragment,
                R.id.budgetHistoryFragment,
                R.id.profileFragment,
                R.id.pinFragment,
                R.id.expensesFragment -> {
                    binding.bottomNavBar.visibility = View.GONE
                }
                else -> {
                    supportActionBar?.show()
                    binding.bottomNavBar.visibility = View.VISIBLE
                }
            }
        }
    }

//    private fun checkProfileData() {
//        profileViewModel.profileLiveData.observe(this) {
//            if (it.isEmpty()) {
//                navController.navigate(R.id.action_global_profileFragment, null, navOptions {
//                    popUpTo(R.id.calendarFragment) {
//                        inclusive = true
//                    }
//                })
//            }
//        }
//    }

    private fun resetPinCode() {
        val bottomMenu = binding.bottomNavBar.menu
        bottomMenu.findItem(R.id.piggyBankFragment).isEnabled = true
        bottomMenu.findItem(R.id.reportsFragment).isEnabled = true
        val sharedPrefs = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(PREFERENCE_IS_PIN_ENTERED_KEY, false).apply()
    }

    private fun checkPin(destinationId: Int? = null) {
        val sharedPrefs = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val savedPin = sharedPrefs.getString(PREFERENCE_PIN_VALUE_KEY, null)
        val isPinEntered = sharedPrefs.getBoolean(PREFERENCE_IS_PIN_ENTERED_KEY, false)
        if (savedPin != null && !isPinEntered) {
            val currentDest = destinationId ?: navController.currentDestination?.id
            if (currentDest != R.id.pinFragment &&
                currentDest != R.id.splashFragment &&
                currentDest != R.id.onboardingFragment) {
                navController.navigate(R.id.pinFragment)
            }
        }
    }

    // TODO Just for testing, needs to be reworked for specific EditText elements.
    // TODO HOWEVER! This seems to be a good solution to focusing issue and is considered optimal.
//    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
//        if (ev?.action == MotionEvent.ACTION_DOWN) {
//            val v = currentFocus
//            if (v is EditText) {
//                val outRect = Rect()
//                v.getGlobalVisibleRect(outRect)
//                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
//                    v.hideKeyboard()
//                    v.clearFocus()
//                }
//            }
//        }
//        return super.dispatchTouchEvent(ev)
//    }
//
//    private fun View.hideKeyboard() {
//        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
//    }
}