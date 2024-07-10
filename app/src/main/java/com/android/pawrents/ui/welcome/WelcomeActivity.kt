package com.android.pawrents.ui.welcome

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.android.pawrents.R
import com.android.pawrents.databinding.ActivityWelcomeBinding
import com.android.pawrents.ui.MainActivity
import com.android.pawrents.ui.signin.AuthActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val sectionsPagerAdapter = WelcomeSectionPagerAdapter(this,
            {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
        },
            {
            //toSignUp
            binding.welcomeViewPager.setCurrentItem(2, true)
        }, {
            //toSignIn
                val intent = Intent(this, AuthActivity::class.java)
                intent.putExtra("isSignUpFinished", it)
                startActivity(intent)
                if(it) finish()
            }
        )
        val viewPager: ViewPager2 = binding.welcomeViewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.welcomeTabLayout
        TabLayoutMediator(tabs, viewPager) { tab, _ ->
            tab.text = ""
        }.attach()
        supportActionBar?.elevation = 0f
    }
}

