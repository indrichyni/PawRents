package com.android.pawrents.ui.welcome

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class WelcomeSectionPagerAdapter(activity: AppCompatActivity, private val skipCallback : () -> Unit, private val toSignUp : () -> Unit, private val toSignIn : (Boolean) -> Unit) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = LogoFragment()
            1 -> fragment = OpeningFragment(skipCallback, toSignUp)
            2 -> fragment = SignUpFragment(skipCallback, toSignIn)
        }
        return fragment as Fragment
    }

}