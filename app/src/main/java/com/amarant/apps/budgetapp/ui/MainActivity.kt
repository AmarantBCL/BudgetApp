package com.amarant.apps.budgetapp.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.amarant.apps.budgetapp.R
import com.amarant.apps.budgetapp.databinding.ActivityMainBinding
import com.amarant.apps.budgetapp.ui.viewmodels.ProfileViewModel
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
                else -> {
                    supportActionBar?.show()
                    binding.bottomNavBar.visibility = View.VISIBLE
                    if (destination.id == R.id.calendarFragment) {
                        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
                        val button = toolbar.findViewById<Button>(R.id.btn_action)
                        button.visibility = View.VISIBLE
                    } else {
                        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
                        val button = toolbar.findViewById<Button>(R.id.btn_action)
                        button.visibility = View.GONE
                    }
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
}