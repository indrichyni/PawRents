package com.android.pawrents.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.android.pawrents.R
import com.android.pawrents.databinding.ActivityMainBinding
import com.android.pawrents.ui.signin.AuthActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.historyFragment, R.id.addFragment,
                R.id.favoriteFragment, R.id.profileFragment
            )
        )

        binding.navView.visibility = View.VISIBLE

        binding.navView.menu.findItem(R.id.historyFragment)?.setOnMenuItemClickListener {
            if(FirebaseAuth.getInstance().currentUser != null) {
                false
            }
            else {
                toLogin()
                true
            }
        }

        binding.navView.menu.findItem(R.id.addFragment)?.setOnMenuItemClickListener {
            if(FirebaseAuth.getInstance().currentUser != null) {
                false
            }
            else {
                toLogin()
                true
            }
        }

        binding.navView.menu.findItem(R.id.favoriteFragment)?.setOnMenuItemClickListener {
            if(FirebaseAuth.getInstance().currentUser != null) {
                false
            }
            else {
                toLogin()
                true
            }
        }

        binding.navView.menu.findItem(R.id.profileFragment)?.setOnMenuItemClickListener {
            if(FirebaseAuth.getInstance().currentUser != null) {
                false
            }
            else {
                toLogin()
                true
            }
        }


        if(FirebaseAuth.getInstance().currentUser == null){
            Toast.makeText(this@MainActivity, "Login to Access More Feature", Toast.LENGTH_SHORT).show()
        }


        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    fun toLogin(){
        Toast.makeText(this@MainActivity, "Login to Access This Feature", Toast.LENGTH_SHORT).show()
        val intent = Intent(this@MainActivity, AuthActivity::class.java)
        startActivity(intent)
    }


}