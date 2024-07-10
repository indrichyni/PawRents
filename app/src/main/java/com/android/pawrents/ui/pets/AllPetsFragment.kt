package com.android.pawrents.ui.pets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.android.pawrents.R
import com.android.pawrents.data.model.Favorite
import com.android.pawrents.data.model.User
import com.android.pawrents.databinding.FragmentAllPetsBinding
import com.android.pawrents.databinding.FragmentHomeBinding
import com.android.pawrents.ui.LoadingDialog
import com.android.pawrents.ui.MainActivity
import com.android.pawrents.ui.home.HomePetsAdapter
import com.android.pawrents.ui.home.PetsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener


class AllPetsFragment : Fragment() {

    private var _binding: FragmentAllPetsBinding? = null
    private val binding get() = _binding!!
    private val petsViewModel: PetsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllPetsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loadingDialog = LoadingDialog(requireContext())

        val isSearchMode = AllPetsFragmentArgs.fromBundle(arguments as Bundle).isSearchMode

        val timePetAdapter = HomePetsAdapter({
            val go = AllPetsFragmentDirections.actionAllPetsFragmentToPetDetailFragment(it)
            findNavController().navigate(go)
        },{ pet, isFavorited ->

            if(FirebaseAuth.getInstance().currentUser == null){
                (activity as MainActivity).toLogin()
            }else{
                if(isFavorited){
                    deleteFavorite(pet.id)
                }else{
                    addFavorite(pet.id)
                }
            }

        })
        binding.rvAllPets.apply {
            layoutManager = GridLayoutManager(requireContext(),2)
            adapter = timePetAdapter
            isNestedScrollingEnabled = false
        }

        petsViewModel.petsFiltered.observe(viewLifecycleOwner) {
            timePetAdapter.submitList(it)
        }

        petsViewModel.petsFiltered.observe(viewLifecycleOwner){
            timePetAdapter.submitList(it)
        }

        petsViewModel.isLoading.observe(viewLifecycleOwner){
            if(it) loadingDialog.startLoadingDialog() else loadingDialog.dismissDialog()
        }

        petsViewModel.errorMsg.observe(viewLifecycleOwner){
            makeToast(it)
        }

        petsViewModel.filterText.observe(viewLifecycleOwner) {
            binding.tvFilterStatus.text = it
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        if(isSearchMode){
            binding.svPets.isIconified = false
            binding.svPets.requestFocus()
        }

        binding.chipGroup.setOnCheckedStateChangeListener { chipGroup, ints ->
            when {
                binding.allCategory.isChecked -> {
                    petsViewModel.filterPetsByCategory("all")
                }
                binding.catCategory.isChecked -> {
                    petsViewModel.filterPetsByCategory("Cat")
                }
                binding.dogCategory.isChecked -> {
                    petsViewModel.filterPetsByCategory("Dog")
                }
                binding.birdCategory.isChecked -> {
                    petsViewModel.filterPetsByCategory("Bird")
                }
                binding.rabbitCategory.isChecked -> {
                    petsViewModel.filterPetsByCategory("Rabbit")
                }
                binding.hamsterCategory.isChecked -> {
                    petsViewModel.filterPetsByCategory("Hamster")
                }
            }
        }

        binding.btnFilterAdopted.setOnClickListener {
            petsViewModel.filterByAdopted()
        }

        binding.svPets.setOnClickListener {
            binding.svPets.isIconified= false
            binding.svPets.requestFocus()
        }

        binding.svPets.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { petsViewModel.searchPets(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { petsViewModel.searchPets(it) }
                return true
            }
        })

        val newLoadingDialog = LoadingDialog(requireContext())
        petsViewModel.getAllFavorites(FirebaseAuth.getInstance().currentUser?.uid ?:"")

        petsViewModel.isLoadingFav.observe(viewLifecycleOwner){
            if(it) newLoadingDialog.startLoadingDialog() else newLoadingDialog.dismissDialog()
        }

        petsViewModel.errorMsg.observe(viewLifecycleOwner){
            makeToast(it)
        }

        petsViewModel.favorites.observe(viewLifecycleOwner){
            timePetAdapter.submitFavorite(it)
        }

        if(FirebaseAuth.getInstance().currentUser != null){
            getCurrentUser()
        }

    }

    private fun getCurrentUser() {
        val loadingDialog = LoadingDialog(requireContext())
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            loadingDialog.startLoadingDialog()
            FirebaseDatabase.getInstance().getReference("Users").child(currentUserUid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if(user!=null){
                        initUser(user!!)
                    }else{
                        makeToast("Current user not found")
                    }
                    loadingDialog.dismissDialog()
                }

                override fun onCancelled(error: DatabaseError) {
                    makeToast(error.message)
                    loadingDialog.dismissDialog()
                }
            })
        }
    }

    private fun showNotifDialog(user: User) {
        val loadingDialog = LoadingDialog(requireContext())
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("NOTIFICATION")
        val desc = if(user.notifAdoptPet == true && user.notifOwnPet == true) "Check history page, someone is interested to adopt your pet and your adopt request already responded by owner." else if(user.notifAdoptPet == true) "Check history page, your adopt request already responded by owner." else if(user.notifOwnPet == true) "Check history page, someone is interested to adopt your pet." else ""
        builder.setMessage(desc)

        builder.setPositiveButton("OK") { dialog, which ->
            val notifMap = mapOf(
                "notifAdoptPet" to false,
                "notifOwnPet" to false)
            FirebaseDatabase.getInstance().getReference("Users")
                .child(user.uid)
                .updateChildren(notifMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        getCurrentUser()
                        loadingDialog.dismissDialog()
                        dialog.dismiss()
                    } else {
                        dialog.dismiss()
                        loadingDialog.dismissDialog()
                        makeToast("Error : ${task.exception?.message.toString()}")
                    }
                }
        }
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun initUser(user: User) {
        if(user.notifAdoptPet == true || user.notifOwnPet == true){
            binding.notifTick.visibility = View.VISIBLE
            binding.btnNotif.setOnClickListener {
                showNotifDialog(user)
            }
        }else{
            binding.notifTick.visibility = View.INVISIBLE
            binding.btnNotif.setOnClickListener {  }
        }
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
                    petsViewModel.getAllFavorites(FirebaseAuth.getInstance().currentUser?.uid ?:"")
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
                                petsViewModel.getAllFavorites(FirebaseAuth.getInstance().currentUser?.uid ?:"")
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



    private fun makeToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}