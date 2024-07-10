package com.android.pawrents.ui.pets

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.pawrents.R
import com.android.pawrents.data.model.Adoption
import com.android.pawrents.data.model.Pet
import com.android.pawrents.databinding.FragmentAdoptionFormBinding
import com.android.pawrents.databinding.FragmentAllPetsBinding
import com.android.pawrents.ui.LoadingDialog
import com.android.pawrents.ui.MainActivity
import com.android.pawrents.ui.home.PetsViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.util.Locale

class AdoptionFormFragment : Fragment() {

    private var _binding: FragmentAdoptionFormBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdoptionFormBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentPet = AdoptionFormFragmentArgs.fromBundle(arguments as Bundle).petToAdopt
        initView(currentPet)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initView(currentPet: Pet) {
        val loadingDialog = LoadingDialog(requireContext())
        binding.apply {
            Glide.with(requireContext()).load(currentPet.photoLink).diskCacheStrategy(
                DiskCacheStrategy.NONE).override(600,600).into(ivPetDetail)
            tvPetName.text = currentPet.name
            tvPetCategory.text = "(${currentPet.category})"
            tvPetLocation.text = currentPet.location

//            binding.etCost.editText?.addTextChangedListener(object : TextWatcher {
//                private var current = ""
//                override fun afterTextChanged(s: Editable?) {}
//
//                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                    if (s.toString() != current) {
//                        binding.etCost.editText?.removeTextChangedListener(this)
//
//                        val cleanString = s.toString().replace("[Rp,.\\s]".toRegex(), "")
//                        val parsed = cleanString.toDoubleOrNull() ?: 0.0
//                        val formatted = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(parsed)
//
//                        current = formatted
//                        binding.etCost.editText?.setText(formatted)
//                        binding.etCost.editText?.setSelection(formatted.length)
//
//                        binding.etCost.editText?.addTextChangedListener(this)
//                    }
//                }
//            })

            btnSubmit.setOnClickListener {
                etName.error = null
                etAddress.error = null
                etCost.error = null
                when {
                    etName.editText?.text.isNullOrEmpty() -> {
                        etName.error = getString(R.string.field_cant_be_empty_error)
                        etName.requestFocus()
                    }

                    etAddress.editText?.text.isNullOrEmpty() -> {
                        etAddress.error = getString(R.string.field_cant_be_empty_error)
                        etAddress.requestFocus()
                    }

                    radioGroupAllergy.checkedRadioButtonId == -1 ->{
                        makeToast("Please answer allergy question.")
                    }
                    radioGroupBusy.checkedRadioButtonId == -1 ->{
                        makeToast("Please answer busy question.")
                    }

                    radioGroupOwnPet.checkedRadioButtonId == -1 ->{
                        makeToast("Please answer own pet question.")
                    }

                    etCost.editText?.text.isNullOrEmpty() -> {
                        etCost.error = getString(R.string.field_cant_be_empty_error)
                        etCost.requestFocus()
                    }
                    else -> {
                        loadingDialog.startLoadingDialog()
                        val adoptName = binding.etName.editText?.text.toString()
                        val adoptAddress = binding.etAddress.editText?.text.toString()
                        val adoptCost = binding.etCost.editText?.text.toString()
                        val adoptAllergy = radioGroupAllergy.checkedRadioButtonId == R.id.radioAllergyYes
                        val adoptOwn = radioGroupOwnPet.checkedRadioButtonId == R.id.radioOwnYes
                        val adoptBusy = radioGroupBusy.findViewById<RadioButton>(radioGroupBusy.checkedRadioButtonId).text.toString()

                        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Adoption")
                        val key = databaseReference.push().key ?: ""
                        val adoption = Adoption(
                            status = "Process",
                            adoptionId = key,
                            adoptUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                            adoptName = adoptName,
                            adoptAddress = adoptAddress,
                            adoptCost = adoptCost,
                            adoptAllergy = adoptAllergy,
                            adoptOwn = adoptOwn,
                            adoptBusy = adoptBusy,
                            ownerId = currentPet.userId,
                            petId = currentPet.id,
                            timestamp = System.currentTimeMillis()
                        )
                        databaseReference.child(key).setValue(adoption)
                            .addOnSuccessListener {
                                val notifMap = mapOf(
                                    "notifOwnPet" to true)
                                FirebaseDatabase.getInstance().getReference("Users")
                                    .child(currentPet.userId)
                                    .updateChildren(notifMap)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            loadingDialog.dismissDialog()
                                            makeToast("Success send adoption form, the owner will be informed.")
                                            val intent = Intent(activity, MainActivity::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                            startActivity(intent)
                                            activity?.finish()
                                        } else {
                                            loadingDialog.dismissDialog()
                                            makeToast("Error : ${task.exception?.message.toString()}")
                                        }
                                    }

                            }
                            .addOnFailureListener { exception ->
                                loadingDialog.dismissDialog()
                                makeToast("Error: ${exception.message}")
                            }
                    }
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