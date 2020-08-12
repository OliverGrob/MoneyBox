package com.ogrob.moneybox.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.ogrob.moneybox.R
import com.ogrob.moneybox.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        val appBarConfiguration = AppBarConfiguration.Builder(
            setOf(
                R.id.expenseYearFragment,
                R.id.categoryFragment,
                R.id.optionsFragment,
                R.id.aboutFragment)
        ).build()

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.bottomNavigationView.setupWithNavController(navController)
    }

    fun showLoadingAnimation() {
        binding.loadingRelativeLayout.visibility = View.VISIBLE
        binding.navHostFragment.visibility = View.GONE
    }

    fun hideLoadingAnimation() {
        binding.loadingRelativeLayout.visibility = View.GONE
        binding.navHostFragment.visibility = View.VISIBLE
    }

}
