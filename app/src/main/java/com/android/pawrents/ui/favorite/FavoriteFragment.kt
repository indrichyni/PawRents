package com.android.pawrents.ui.favorite

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.pawrents.R
import com.android.pawrents.data.model.Favorite
import com.android.pawrents.databinding.FragmentFavoriteBinding
import com.android.pawrents.databinding.FragmentPetDetailBinding
import com.android.pawrents.ui.AdoptionViewModel
import com.android.pawrents.ui.LoadingDialog
import com.android.pawrents.ui.UserViewModel
import com.android.pawrents.ui.home.HomeFragmentDirections
import com.android.pawrents.ui.home.HomePetsAdapter
import com.android.pawrents.ui.home.PetsViewModel
import com.android.pawrents.ui.pets.AllPetsFragmentDirections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener


class FavoriteFragment : Fragment() {


    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    private val favoriteViewModel: FavoriteViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loadingDialog = LoadingDialog(requireContext())
        val favLoadingDialog = LoadingDialog(requireContext())

        favoriteViewModel.fetchPetsSortedByTimestamp()

        favoriteViewModel.isLoading.observe(viewLifecycleOwner){
            if(it) loadingDialog.startLoadingDialog() else loadingDialog.dismissDialog()
        }

        favoriteViewModel.isLoadingFav.observe(viewLifecycleOwner){
            if(it) favLoadingDialog.startLoadingDialog() else favLoadingDialog.dismissDialog()
        }

        favoriteViewModel.errorMsg.observe(viewLifecycleOwner){
            makeToast(it)
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val favPetAdapter = FavoriteAdapter({ pet ->
            deleteFavorite(pet.id)
        }, { pet ->
            val go = FavoriteFragmentDirections.actionFavoriteFragmentToPetDetailFragment(pet)
            findNavController().navigate(go)
        })
        binding.rvFavPets.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = favPetAdapter
            isNestedScrollingEnabled = false
        }

        favoriteViewModel.petsFiltered.observe(viewLifecycleOwner){
            favPetAdapter.submitList(it)
        }

        binding.chipGroup.setOnCheckedStateChangeListener { chipGroup, ints ->
            when {
                binding.allCategory.isChecked -> {
                    favoriteViewModel.filterPetsByCategory("all")
                }
                binding.catCategory.isChecked -> {
                    favoriteViewModel.filterPetsByCategory("Cat")
                }
                binding.dogCategory.isChecked -> {
                    favoriteViewModel.filterPetsByCategory("Dog")
                }
                binding.birdCategory.isChecked -> {
                    favoriteViewModel.filterPetsByCategory("Bird")
                }
                binding.rabbitCategory.isChecked -> {
                    favoriteViewModel.filterPetsByCategory("Rabbit")
                }
                binding.hamsterCategory.isChecked -> {
                    favoriteViewModel.filterPetsByCategory("Hamster")
                }
            }
        }

        binding.btnFilterAdopted.setOnClickListener {
            favoriteViewModel.filterByAdopted()
        }

        binding.svPets.setOnClickListener {
            binding.svPets.isIconified= false
            binding.svPets.requestFocus()
        }

        binding.svPets.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { favoriteViewModel.searchPets(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { favoriteViewModel.searchPets(it) }
                return true
            }
        })


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
                                favoriteViewModel.fetchPetsSortedByTimestamp()
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