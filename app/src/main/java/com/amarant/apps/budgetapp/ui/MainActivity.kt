package com.amarant.apps.budgetapp.ui

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.ActivityMainBinding
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_IS_PIN_ENTERED_KEY
import com.amarant.apps.budgetapp.util.Constants.PREFERENCE_NAME
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

//    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.calendarFragment,
            R.id.piggyBankFragment,
            R.id.statsFragment,
            R.id.reportsFragment
        ))
        toolbar.setupWithNavController(navController, appBarConfiguration)
        setBottomNavigation()
//        checkProfileData()
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.option_menu, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
//    }

    override fun onDestroy() { // TODO Monitor the frequency of PIN prompting
        super.onDestroy()
        resetPinCode()
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

    private fun setBottomNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        binding.bottomNavBar.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.onboardingFragment,
                R.id.splashFragment -> {
                    supportActionBar?.hide()
                    binding.bottomNavBar.visibility = View.GONE
                }
                R.id.fragmentAddEntry,
                R.id.addSavingFragment -> {
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
        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottomNavBar)
        val bottomMenu = bottomNavBar.menu
        bottomMenu.findItem(R.id.piggyBankFragment).isEnabled = true
        bottomMenu.findItem(R.id.reportsFragment).isEnabled = true
        val sharedPrefs = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(PREFERENCE_IS_PIN_ENTERED_KEY, false).apply()
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