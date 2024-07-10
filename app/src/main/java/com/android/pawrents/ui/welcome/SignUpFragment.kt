package com.android.pawrents.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.pawrents.R
import com.android.pawrents.data.model.User
import com.android.pawrents.databinding.FragmentSignUpBinding
import com.android.pawrents.ui.LoadingDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignUpFragment(private val skipCallback : () -> Unit, private val signInCallback : (Boolean) -> Unit) : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loadingDialog = LoadingDialog(requireContext())

        binding.btnSkip.setOnClickListener {
            skipCallback.invoke()
        }

        binding.btnToSignIn.setOnClickListener {
            signInCallback.invoke(false)
        }

        binding.btnSignUp.setOnClickListener {
            binding.etName.error = null
            binding.etEmail.error = null
            binding.etPassword.error = null
            binding.etConfirmPassword.error = null
            when {
                binding.etName.editText?.text.isNullOrEmpty() -> {
                    binding.etName.error = getString(R.string.field_cant_be_empty_error)
                    binding.etName.requestFocus()
                }
                binding.etEmail.editText?.text.isNullOrEmpty() -> {
                    binding.etEmail.error = getString(R.string.field_cant_be_empty_error)
                    binding.etEmail.requestFocus()
                }
                binding.etPassword.editText?.text.isNullOrEmpty() -> {
                    binding.etPassword.error = getString(R.string.field_cant_be_empty_error)
                    binding.etPassword.requestFocus()
                }
                binding.etConfirmPassword.editText?.text.isNullOrEmpty() -> {
                    binding.etConfirmPassword.error = getString(R.string.field_cant_be_empty_error)
                    binding.etConfirmPassword.requestFocus()
                }
                !Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.editText?.text.toString()).matches() -> {
                    binding.etEmail.error = getString(R.string.email_must_valid)
                    binding.etEmail.requestFocus()
                }
                binding.etPassword.editText?.text.toString() != binding.etConfirmPassword.editText?.text.toString() -> {
                    binding.etConfirmPassword.error = getString(R.string.password_not_match)
                    binding.etConfirmPassword.requestFocus()
                }
                else -> {
                    loadingDialog.startLoadingDialog()
                    val email = binding.etEmail.editText?.text.toString()
                    val username = binding.etName.editText?.text.toString()
                    val password = binding.etPassword.editText?.text.toString()

                    val usersRef = FirebaseDatabase.getInstance().getReference("Users")
                    val auth = FirebaseAuth.getInstance()
                    usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                loadingDialog.dismissDialog()
                                makeToast("Error : Email already exist!")
                            } else {
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val uid = auth.currentUser?.uid ?: ""

                                            val user = User(
                                                profilePic = "",
                                                username = username,
                                                firstName = "",
                                                lastName = "",
                                                gender = "",
                                                birthDate = "",
                                                location = "",
                                                email = email,
                                                telephone = "",
                                                uid = uid
                                            )

                                            FirebaseDatabase.getInstance().getReference("Users")
                                                .child(uid)
                                                .setValue(user)
                                                .addOnCompleteListener { innerTask ->
                                                    if (innerTask.isSuccessful) {
                                                        loadingDialog.dismissDialog()
                                                        makeToast("Register Success")
                                                        auth.signOut()
                                                        signInCallback.invoke(true)
                                                    } else {
                                                        loadingDialog.dismissDialog()
                                                        makeToast("Register Failed")
                                                    }
                                                }
                                        } else {
                                            loadingDialog.dismissDialog()
                                            makeToast(task.exception?.toString() ?: "")
                                        }
                                    }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            loadingDialog.dismissDialog()
                            makeToast("Unexpected Error")
                        }
                    })

                }
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