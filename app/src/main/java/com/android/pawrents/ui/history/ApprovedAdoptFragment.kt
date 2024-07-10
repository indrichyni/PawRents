package com.android.pawrents.ui.history

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.pawrents.R
import com.android.pawrents.data.model.Favorite
import com.android.pawrents.data.model.Pet
import com.android.pawrents.databinding.FragmentApprovedAdoptBinding
import com.android.pawrents.databinding.FragmentOwnPetDetailBinding
import com.android.pawrents.ui.LoadingDialog
import com.android.pawrents.ui.UserViewModel
import com.android.pawrents.ui.pets.PetDetailFragmentArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ApprovedAdoptFragment : Fragment() {


    private var _binding: FragmentApprovedAdoptBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApprovedAdoptBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentPet = ApprovedAdoptFragmentArgs.fromBundle(arguments as Bundle).currentPet
        val currentAdoption = ApprovedAdoptFragmentArgs.fromBundle(arguments as Bundle).currentAdoption

        initFavorite(currentPet.id)
        initOwner(currentPet)
        
        binding.apply {
            Glide.with(requireContext()).load(currentPet.photoLink).diskCacheStrategy(
                DiskCacheStrategy.NONE).override(600,600).into(ivPetDetail)
            tvPetName.text = currentPet.name
            tvPetCategory.text = "(${currentPet.category})"
            tvPetLocation.text = currentPet.location
            val timeLong = currentAdoption.contactBefore ?: System.currentTimeMillis()
            tvLastResponse.text = formatDate(getOneMonthLaterMillis(timeLong))
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
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
                binding.btnContact.setOnClickListener {view->
                    if(it.telephone.isNotEmpty() && it.telephone.isNotBlank()){
                        openWhatsApp(it.telephone,currentPet.name)
                    }else{
                        makeToast("User has not added a phone number")
                    }
                }
            }
        }
    }

    private fun openWhatsApp(phone: String, petName: String) {
        val phoneNumber = "+62 $phone"
        val message = "Halo Kak! saya user dari PawRents yang telah diterima untuk mengadopsi $petName."
        val url = "https://api.whatsapp.com/send?phone=$phoneNumber"+"&text=" + URLEncoder.encode(message, "UTF-8")
        val i = Intent(Intent.ACTION_VIEW)
        i.setData(Uri.parse(url))
        startActivity(i)
    }


    private fun initFavorite(currentPetId: String) {
        val loadingDialog = LoadingDialog(requireContext())

        loadingDialog.startLoadingDialog()
        val query = FirebaseDatabase.getInstance().getReference("Favorite").orderByChild("userId").equalTo(
            FirebaseAuth.getInstance().currentUser?.uid ?: "")
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

    fun getOneMonthLaterMillis(timeLong: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeLong
        calendar.add(Calendar.MONTH, 1)
        return calendar.timeInMillis
    }

    fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val date = Date(timestamp)
        return dateFormat.format(date)
    }


    private fun makeToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}