package com.android.pawrents.ui.pets

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.pawrents.R
import com.android.pawrents.data.model.Favorite
import com.android.pawrents.data.model.Pet
import com.android.pawrents.databinding.FragmentPetDetailBinding
import com.android.pawrents.ui.AdoptionViewModel
import com.android.pawrents.ui.LoadingDialog
import com.android.pawrents.ui.MainActivity
import com.android.pawrents.ui.UserViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.net.URLEncoder


class PetDetailFragment : Fragment() {

    private var _binding: FragmentPetDetailBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by viewModels()
    private val adoptionViewModel: AdoptionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetDetailBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentPet = PetDetailFragmentArgs.fromBundle(arguments as Bundle).petsToBeShown
        initView(currentPet)
        initOwner(currentPet)
        if(FirebaseAuth.getInstance().currentUser!=null){
            initFavorite(currentPet.id)
        }else{
            binding.btnFavDetail.visibility = View.GONE
            binding.btnFavedDetail.visibility = View.GONE
        }
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if(FirebaseAuth.getInstance().currentUser!=null){
            if(currentUserId != currentPet.userId) initAdoption(currentPet)
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initFavorite(currentPetId: String) {
        val loadingDialog = LoadingDialog(requireContext())

        loadingDialog.startLoadingDialog()
        val query = FirebaseDatabase.getInstance().getReference("Favorite").orderByChild("userId").equalTo(FirebaseAuth.getInstance().currentUser?.uid ?: "")
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favoriteList = mutableListOf<Favorite>()
                for (childSnapshot in snapshot.children) {
                    val favorite = childSnapshot.getValue(Favorite::class.java)
                    if (favorite != null) {
                        favoriteList.add(favorite)
                    }
                }
                loadingDialog.dismissDialog()
                if(favoriteList.any { it.petId == currentPetId }){
                    binding.btnFavDetail.visibility = View.GONE
                    binding.btnFavedDetail.visibility = View.VISIBLE
                }else{
                    binding.btnFavDetail.visibility = View.VISIBLE
                    binding.btnFavedDetail.visibility = View.GONE
                }

                binding.btnFavDetail.setOnClickListener {
                    addFavorite(currentPetId)
                }

                binding.btnFavedDetail.setOnClickListener {
                    deleteFavorite(currentPetId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                loadingDialog.dismissDialog()
                makeToast(error.message)
            }
        })
    }

    private fun addFavorite(petId: String) {
        val loadingDialog = LoadingDialog(requireContext())
        loadingDialog.startLoadingDialog()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val favoriteId = FirebaseDatabase.getInstance().getReference("Favorite").push().key ?: ""
            val favorite = Favorite(favoriteId, currentUser.uid, petId)
            FirebaseDatabase.getInstance().getReference("Favorite").child(favoriteId).setValue(favorite)
                .addOnSuccessListener {
                    loadingDialog.dismissDialog()
                    initFavorite(petId)
                    makeToast("Added To Favorite")
                }
                .addOnFailureListener {
                    loadingDialog.dismissDialog()
                    makeToast("Error: ${it.message}")
                }
        }
    }

    private fun deleteFavorite(petId: String) {
        val loadingDialog = LoadingDialog(requireContext())
        loadingDialog.startLoadingDialog()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val query: Query = FirebaseDatabase.getInstance().getReference("Favorite").orderByChild("userId").equalTo(currentUser.uid)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var favoriteToDelete: Favorite? = null
                    for (childSnapshot in snapshot.children) {
                        val favorite = childSnapshot.getValue(Favorite::class.java)
                        if (favorite != null && favorite.petId == petId) {
                            favoriteToDelete = favorite
                            break
                        }
                    }
                    if(favoriteToDelete!=null){
                        FirebaseDatabase.getInstance().getReference("Favorite").child(favoriteToDelete.favoriteId).removeValue()
                            .addOnSuccessListener {
                                initFavorite(petId)
                                loadingDialog.dismissDialog()
                                makeToast("Removed From Favorite")
                            }
                            .addOnFailureListener {
                                loadingDialog.dismissDialog()
                                makeToast("Error: ${it.message}")
                            }
                    }else{
                        loadingDialog.dismissDialog()
                        makeToast("Error: No Fav Recorded")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Optionally handle error
                }
            })
        }
    }

    private fun openWhatsApp(phone: String, petName: String) {
        val phoneNumber = "+62 $phone"
        val message = "Halo Kak! Saya user dari PawRents tertarik untuk bertanya mengenai $petName."
        val url = "https://api.whatsapp.com/send?phone=$phoneNumber"+"&text=" + URLEncoder.encode(message, "UTF-8")
        val i = Intent(Intent.ACTION_VIEW)
        i.setData(Uri.parse(url))
        startActivity(i)
    }

    private fun initAdoption(currentPet: Pet) {
        val loadingDialog = LoadingDialog(requireContext())

        adoptionViewModel.fetchAllAdoption(currentPet)

        adoptionViewModel.isLoading.observe(viewLifecycleOwner){
            if(it) loadingDialog.startLoadingDialog() else loadingDialog.dismissDialog()
        }

        adoptionViewModel.errorMsg.observe(viewLifecycleOwner){
            makeToast(it)
        }

        adoptionViewModel.adoptionData.observe(viewLifecycleOwner){
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val isCurrentUserInList = it.any { adoption -> adoption.adoptUserId == currentUserId }
            val currentAdoption = it.firstOrNull{ adopt->
                adopt.adoptUserId == currentUserId
            }
            if(isCurrentUserInList){
                if(currentAdoption!= null){
                    if(currentAdoption.status == "Accepted"){
                        binding.btnAdopt.visibility = View.GONE
                        binding.tvAdoptionSent.text = "Accepted Request, Check History Menu."
                        binding.tvAdoptionSent.visibility = View.VISIBLE
                    }else if(currentAdoption.status == "Declined"){
                        binding.btnAdopt.visibility = View.GONE
                        binding.tvAdoptionSent.text = "Your Request is Declined."
                        binding.tvAdoptionSent.visibility = View.VISIBLE
                    }else{
                        binding.btnAdopt.visibility = View.GONE
                        binding.tvAdoptionSent.visibility = View.VISIBLE
                    }
                }else{
                    binding.btnAdopt.visibility = View.GONE
                    binding.tvAdoptionSent.visibility = View.VISIBLE
                }
            }else{
                if(currentPet.adoptUserId !=null){
                    binding.btnAdopt.visibility = View.GONE
                    binding.tvAdoptionSent.text = "Pet Already Adopted."
                    binding.tvAdoptionSent.visibility = View.VISIBLE
                }else{
                    binding.tvAdoptionSent.visibility = View.GONE
                    binding.btnAdopt.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun initOwner(currentPet: Pet) {
        val loadingDialog = LoadingDialog(requireContext())

        userViewModel.isLoading.observe(viewLifecycleOwner){
            if(it) loadingDialog.startLoadingDialog() else loadingDialog.dismissDialog()
        }

        userViewModel.errorMsg.observe(viewLifecycleOwner){
            makeToast(it)
        }

        userViewModel.fetchAllUsers(currentPet)
        userViewModel.petCreator.observe(viewLifecycleOwner){
            Log.d("PetViewModel", it.toString())
            if(it!=null){
                binding.btnCall.setOnClickListener { view->
                    if(it.telephone.isNotEmpty() && it.telephone.isNotBlank()){
                        callNumber(it.telephone)
                    }else{
                        makeToast("User has not added a phone number")
                    }
                }
                binding.btnChat.setOnClickListener { view ->
                    if(it.telephone.isNotEmpty() && it.telephone.isNotBlank()){
                        openWhatsApp(it.telephone,currentPet.name)
                    }else{
                        makeToast("User has not added a phone number")
                    }
                }
                binding.tvUserName.text = it.username
                Glide.with(requireContext()).load(it.profilePic).override(150,150).diskCacheStrategy(
                    DiskCacheStrategy.NONE).placeholder(R.drawable.img_default_user).centerCrop().skipMemoryCache(true).into(binding.ivUser)
            }
        }
    }

    private fun callNumber(phone: String){
        val phoneIntent = Intent(
            Intent.ACTION_DIAL, Uri.fromParts(
                "tel", phone, null
            )
        )
        startActivity(phoneIntent)
    }



    private fun initView(currentPet: Pet) {
        binding.apply {
            Glide.with(requireContext()).load(currentPet.photoLink).diskCacheStrategy(
                DiskCacheStrategy.NONE).override(600,600).into(ivPetDetail)
            tvPetName.text = currentPet.name
            tvPetCategory.text = "(${currentPet.category})"
            tvPetLocation.text = currentPet.location
            tvPetGender.text = currentPet.gender
            tvPetAge.text = "${currentPet.age} Month"
            tvPetWeight.text = "${currentPet.weight} Kg"
            tvPetBreed.text = currentPet.breed
            tvPetColor.text = currentPet.color
            tvPetVaccine.text = currentPet.vaccine
            tvOwner.text = "${currentPet.name} owner"
            tvPetDesc.text = currentPet.description

//            if(FirebaseAuth.getInstance().currentUser == null){
//                binding.btnAdopt.visibility = View.GONE
//            }else{
//
//            }

            if(currentPet.userId == FirebaseAuth.getInstance().currentUser?.uid ?:""){
                btnAdopt.visibility = View.INVISIBLE
                contactLayout.visibility = View.GONE
            }else{
                btnAdopt.visibility = View.VISIBLE
                contactLayout.visibility = View.VISIBLE
            }

            btnAdopt.setOnClickListener {
                if(FirebaseAuth.getInstance().currentUser == null){
                    (activity as MainActivity).toLogin()
                }else{
                    val go = PetDetailFragmentDirections.actionPetDetailFragmentToAdoptionFormFragment(currentPet)
                    findNavController().navigate(go)
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