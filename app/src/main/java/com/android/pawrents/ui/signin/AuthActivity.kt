package com.android.pawrents.ui.signin

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.android.pawrents.R
import com.android.pawrents.databinding.ActivityAuthBinding
import com.android.pawrents.ui.MainActivity
import com.android.pawrents.ui.welcome.SignUpFragment

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    var isFinished: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isFinished = intent.getBooleanExtra("isSignUpFinished", true)

        supportActionBar?.hide()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SignInFragment())
                .commit()
        }
    }

    fun showRegisterFragment() {
        replaceFragment(SignUpFragment({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        },{
            showLoginFragment()
        }), "SignUpFragment")
    }

    fun goToMain(){
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun showLoginFragment() {
        replaceFragment(SignInFragment(), "SignInFragment")
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        val fragmentManager = supportFragmentManager

        val existingFragment = fragmentManager.findFragmentByTag(tag)
        if (existingFragment != null) {
            fragmentManager.popBackStack(tag, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }
}