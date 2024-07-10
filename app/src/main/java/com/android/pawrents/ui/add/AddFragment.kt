package com.android.pawrents.ui.add

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.android.pawrents.R
import com.android.pawrents.data.model.Pet
import com.android.pawrents.databinding.FragmentAddBinding
import com.android.pawrents.databinding.FragmentSignInBinding
import com.android.pawrents.ui.LoadingDialog
import com.android.pawrents.ui.MainActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage


class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private var petPhotoUri : Uri? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSpinner()
        initButton()
        initPhotoPicker()
        initSubmitButton()
    }

    private fun initSubmitButton() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val loadingDialog = LoadingDialog(requireContext())
        binding.btnSubmit.setOnClickListener {
            binding.etPetPhoto.error = null
            binding.etPetName.error = null
            binding.etLocation.error = null
            binding.etPetAge.error = null
            binding.etPetWeight.error = null
            binding.etBreed.error = null
            binding.etColor.error = null
            binding.etDesc.error = null

            if(petPhotoUri == null){
                binding.etPetPhoto.error = getString(R.string.field_cant_be_empty_error)
                binding.etPetPhoto.requestFocus()
            }else if(binding.etPetName.editText?.text.isNullOrEmpty()){
                binding.etPetName.error = getString(R.string.field_cant_be_empty_error)
                binding.etPetName.requestFocus()
            }else if(binding.etLocation.editText?.text.isNullOrEmpty()){
                binding.etLocation.error = getString(R.string.field_cant_be_empty_error)
                binding.etLocation.requestFocus()
            }else if(binding.etPetAge.editText?.text.isNullOrEmpty()){
                binding.etPetAge.error = getString(R.string.field_cant_be_empty_error)
                binding.etPetAge.requestFocus()
            }else if(binding.etPetWeight.editText?.text.isNullOrEmpty()){
                binding.etPetWeight.error = getString(R.string.field_cant_be_empty_error)
                binding.etPetWeight.requestFocus()
            }else if(binding.etBreed.editText?.text.isNullOrEmpty()){
                binding.etBreed.error = getString(R.string.field_cant_be_empty_error)
                binding.etBreed.requestFocus()
            }else if(binding.etColor.editText?.text.isNullOrEmpty()){
                binding.etColor.error = getString(R.string.field_cant_be_empty_error)
                binding.etColor.requestFocus()
            }else if(binding.etDesc.editText?.text.isNullOrEmpty()){
                binding.etDesc.error = getString(R.string.field_cant_be_empty_error)
                binding.etDesc.requestFocus()
            }else{
                loadingDialog.startLoadingDialog()
                val petPhoto = petPhotoUri
                val petName = binding.etPetName.editText?.text.toString()
                val petLocation = binding.etLocation.editText?.text.toString()
                val petAge = binding.etPetAge.editText?.text.toString()
                val petWeight = binding.etPetWeight.editText?.text.toString()
                val petBreed = binding.etBreed.editText?.text.toString()
                val petColor = binding.etColor.editText?.text.toString()
                val petDesc = binding.etDesc.editText?.text.toString()
                val petCategory = binding.categorySpinner.selectedItem.toString()
                val petVaccine = binding.vacchineSpinner.selectedItem.toString()
                val petGender = binding.genderSpinner.selectedItem.toString()

                val database = FirebaseDatabase.getInstance()
                val petsRef = database.getReference("pets")
                val newPetRef = petsRef.push()
                val petId = newPetRef.key ?: ""
                val petTimestamp = System.currentTimeMillis()


                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.getReference("uploadedPetPhotos/$petId")
                storageRef.putFile(petPhotoUri!!)
                    .addOnSuccessListener { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                            makeToast("Pet photo uploaded")
                            val petPhotoLink = uri.toString()
                            val pet = Pet(
                                id = petId,
                                name = petName,
                                location = petLocation,
                                age = petAge.toInt(),
                                weight = petWeight.toFloat(),
                                breed = petBreed,
                                color = petColor,
                                description = petDesc,
                                category = petCategory,
                                vaccine = petVaccine,
                                gender = petGender,
                                photoLink = petPhotoLink,
                                timestamp = petTimestamp,
                                userId = currentUser?.uid ?:""
                            )

                            newPetRef.setValue(pet).addOnCompleteListener { task ->
                                loadingDialog.dismissDialog()
                                if (task.isSuccessful) {
                                    makeToast("Pet added successfully")
                                    val intent = Intent(activity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    activity?.finish()
                                } else {
                                    makeToast("Failed to add pet")
                                }
                            }
                        }.addOnFailureListener { e ->
                            loadingDialog.dismissDialog()
                            makeToast(e.message.toString())
                        }
                    }
                    .addOnFailureListener { e ->
                        loadingDialog.dismissDialog()
                        makeToast(e.message.toString())
                    }


            }
        }
    }

    private fun initPhotoPicker() {
        binding.btnUpload.setOnClickListener {
            ImagePicker.with(this)
                .compress(1024)
                .crop(3f,2f)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }

        binding.btnUploadImageUploaded.setOnClickListener {
            ImagePicker.with(this)
                .compress(1024)
                .crop(3f,2f)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            if (resultCode == Activity.RESULT_OK) {
                val fileUri = data?.data!!

                binding.layoutImageNotUploaded.visibility = View.GONE
                binding.layoutImageUploaded.visibility = View.VISIBLE

                binding.ivPhotoUploaded.setImageURI(fileUri)
                petPhotoUri = fileUri

            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                makeToast(ImagePicker.getError(data))
            } else {

            }
        }

    private fun initButton() {
        binding.btnMinAge.setOnClickListener {
            if(binding.etPetAge.editText?.text.isNullOrEmpty() || binding.etPetAge.editText?.text.toString() == "0"){
                binding.etPetAge.editText?.setText("0")
            }else{
                val currentAge = binding.etPetAge.editText?.text.toString().toInt()
                binding.etPetAge.editText?.setText("${currentAge-1}")
            }
        }

        binding.btnMinWeight.setOnClickListener {
            if(binding.etPetWeight.editText?.text.isNullOrEmpty() || binding.etPetWeight.editText?.text.toString() == "0"){
                binding.etPetWeight.editText?.setText("0")
            }else{
                val currentWeight = binding.etPetWeight.editText?.text.toString().toFloat()
                binding.etPetWeight.editText?.setText("${currentWeight-1}")
            }
        }

        binding.btnPlusAge.setOnClickListener {
            if(binding.etPetAge.editText?.text.isNullOrEmpty()){
                binding.etPetAge.editText?.setText("1")
            }else{
                val currentAge = binding.etPetAge.editText?.text.toString().toInt()
                binding.etPetAge.editText?.setText("${currentAge+1}")
            }
        }

        binding.btnPlusWeight.setOnClickListener {
            if(binding.etPetWeight.editText?.text.isNullOrEmpty()){
                binding.etPetWeight.editText?.setText("1")
            }else{
                val currentWeight = binding.etPetWeight.editText?.text.toString().toFloat()
                binding.etPetWeight.editText?.setText("${currentWeight+1}")
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.gender_array,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.genderSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.vacchine_array,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.vacchineSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.categories_array,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.categorySpinner.adapter = adapter
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