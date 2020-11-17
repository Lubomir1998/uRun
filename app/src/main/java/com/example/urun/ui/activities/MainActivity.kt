package com.example.urun.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.urun.R
import com.example.urun.other.Constants.ACTION_SHOW_TRACKING
import com.example.urun.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setTheme(R.style.Theme_URun)
        setContentView(view)

        goToTrackingFragment(intent)

        binding.bottomNavMenu.setupWithNavController(supportFragmentManager.findFragmentById(R.id.navHostFragment)!!.findNavController())

        // this line means if we click the current bottom nav menu icon
        // nothing will happen
        //binding.bottomNavMenu.setOnNavigationItemReselectedListener { /* NO OP */ }

        supportFragmentManager.findFragmentById(R.id.navHostFragment)!!.findNavController().addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id){
                R.id.myRunsFragment, R.id.statisticsFragment, R.id.meFragment ->
                    binding.bottomNavMenu.visibility = View.VISIBLE
                else ->
                    binding.bottomNavMenu.visibility = View.GONE
            }
        }



    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        goToTrackingFragment(intent)
    }

    private fun goToTrackingFragment(intent: Intent?){
        if(intent != null){
            if(intent.action == ACTION_SHOW_TRACKING){
                supportFragmentManager.findFragmentById(R.id.navHostFragment)!!.findNavController().navigate(R.id.action_launch_run_fragment)
            }
        }
    }


}