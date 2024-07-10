package com.android.pawrents.ui.signin

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.pawrents.R
import com.android.pawrents.databinding.DialogForgetPasswordBinding
import com.android.pawrents.databinding.FragmentSignInBinding
import com.android.pawrents.ui.LoadingDialog
import com.google.firebase.auth.FirebaseAuth


class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loadingDialog = LoadingDialog(requireContext())

        binding.btnSignIn.setOnClickListener {
            binding.etEmail.error = null
            binding.etPassword.error = null
            when {
                binding.etEmail.editText?.text.isNullOrEmpty() -> {
                    binding.etEmail.error = getString(R.string.field_cant_be_empty_error)
                    binding.etEmail.requestFocus()
                }
                binding.etPassword.editText?.text.isNullOrEmpty() -> {
                    binding.etPassword.error = getString(R.string.field_cant_be_empty_error)
                    binding.etPassword.requestFocus()
                }
                !Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.editText?.text.toString()).matches() -> {
                    binding.etEmail.error = getString(R.string.email_must_valid)
                    binding.etEmail.requestFocus()
                }
                else -> {
                    loadingDialog.startLoadingDialog()
                    val email = binding.etEmail.editText?.text.toString()
                    val password = binding.etPassword.editText?.text.toString()
                    val auth = FirebaseAuth.getInstance()

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener { authResult ->
                            makeToast("Login Success")
                            loadingDialog.dismissDialog()
                            (activity as AuthActivity).goToMain()
                        }
                        .addOnFailureListener { e ->
                            loadingDialog.dismissDialog()
                            makeToast(e.message ?: "Login Failed")
                        }
                }
            }

        }

        binding.btnForgetPassword.setOnClickListener {
            val dialogView: DialogForgetPasswordBinding = DialogForgetPasswordBinding.inflate(layoutInflater)
            val builder: AlertDialog.Builder =
                AlertDialog.Builder(requireContext()).setView(dialogView.getRoot())

            val dialog = builder.create()

            dialogView.buttonChange.setOnClickListener{
                dialogView.editTextEmail.error = null
                if(dialogView.editTextEmail.text.isNullOrEmpty()){
                    dialogView.editTextEmail.error = getString(R.string.field_cant_be_empty_error)
                    dialogView.editTextEmail.requestFocus()
                }else if(!Patterns.EMAIL_ADDRESS.matcher(dialogView.editTextEmail.text.toString()).matches()){
                    dialogView.editTextEmail.error = getString(R.string.email_must_valid)
                    dialogView.editTextEmail.requestFocus()
                }else{
                    loadingDialog.startLoadingDialog()
                    val email = dialogView.editTextEmail.text.toString()
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            loadingDialog.dismissDialog()
                            if (task.isSuccessful) {
                                makeToast("Success, check your email.")
                                dialog.dismiss()
                            }else{
                                makeToast("Something went wrong, try again later")
                            }
                        }
                }
            }
            dialog.show()
            dialog.setCancelable(true)
        }

        binding.btnToSignUp.setOnClickListener {
            if((activity as AuthActivity).isFinished) {
                (activity as AuthActivity).showRegisterFragment()
            }
            else{
                (activity as AuthActivity).finish()
            }
        }
    }

    private fun makeToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}