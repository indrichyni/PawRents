package com.android.pawrents.ui.history

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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.pawrents.R
import com.android.pawrents.data.model.Pet
import com.android.pawrents.databinding.FragmentEditBinding
import com.android.pawrents.databinding.FragmentHistoryBinding
import com.android.pawrents.ui.LoadingDialog
import com.android.pawrents.ui.MainActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage


class EditFragment : Fragment() {


    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!
    private var petPhotoUri : Uri? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentPet = EditFragmentArgs.fromBundle(arguments as Bundle).currentPet

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        initPhotoPicker()
        initSpinner(currentPet)
        initButton()
        Glide.with(requireContext()).load(currentPet.photoLink).skipMemoryCache(true).diskCacheStrategy(
            DiskCacheStrategy.NONE).override(500).into(binding.ivPhotoUploaded)
        binding.etPetName.editText?.setText(currentPet.name)
        binding.etLocation.editText?.setText(currentPet.location)
        binding.etPetAge.editText?.setText(currentPet.age.toString())
        binding.etPetWeight.editText?.setText(currentPet.weight.toString())
        binding.etBreed.editText?.setText(currentPet.breed)
        binding.etColor.editText?.setText(currentPet.color)
        binding.etDesc.editText?.setText(currentPet.description)

        initButtonSubmit(currentPet)

    }

    private fun initButtonSubmit(currentPet: Pet) {
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

            if(binding.etPetName.editText?.text.isNullOrEmpty()){
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
                val petId = currentPet.id
                val adopted = currentPet.adoptUserId

                val database = FirebaseDatabase.getInstance()
                val petsRef = database.getReference("pets")

                if(petPhotoUri == null){
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
                        photoLink = currentPet.photoLink,
                        timestamp = currentPet.timestamp,
                        userId = currentUser?.uid ?:"",
                        adoptUserId = adopted
                    )

                    petsRef.child(petId).setValue(pet).addOnCompleteListener { task ->
                        loadingDialog.dismissDialog()
                        if (task.isSuccessful) {
                            makeToast("Pet edited successfully")
                            findNavController().popBackStack()
                        } else {
                            makeToast("Failed to edit pet")
                        }
                    }
                }else{
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
                                    timestamp = currentPet.timestamp,
                                    userId = currentUser?.uid ?:"",
                                    adoptUserId = adopted
                                )

                                petsRef.child(petId).setValue(pet).addOnCompleteListener { task ->
                                    loadingDialog.dismissDialog()
                                    if (task.isSuccessful) {
                                        makeToast("Pet edited successfully")
                                        findNavController().popBackStack()
                                    } else {
                                        makeToast("Failed to edit pet")
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
    }

    private fun initSpinner(currentPet: Pet) {
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

        when(currentPet.category){
            "Cat" -> binding.categorySpinner.setSelection(0)
            "Dog" -> binding.categorySpinner.setSelection(1)
            "Bird" -> binding.categorySpinner.setSelection(2)
            "Rabbit" -> binding.categorySpinner.setSelection(3)
            else -> binding.categorySpinner.setSelection(5)
        }

        when(currentPet.gender){
            "Male" ->binding.genderSpinner.setSelection(0)
            else -> binding.genderSpinner.setSelection(1)
        }

        when(currentPet.vaccine){
            "Yes" ->binding.vacchineSpinner.setSelection(0)
            else -> binding.vacchineSpinner.setSelection(1)
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

    private fun makeToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}