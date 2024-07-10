package com.android.pawrents.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.android.pawrents.R
import com.android.pawrents.data.model.Favorite
import com.android.pawrents.data.model.Knowledge
import com.android.pawrents.data.model.User
import com.android.pawrents.databinding.FragmentHomeBinding
import com.android.pawrents.ui.LoadingDialog
import com.android.pawrents.ui.MainActivity
import com.android.pawrents.ui.SplashActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val petsViewModel: PetsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loadingDialog = LoadingDialog(requireContext())

        val knowledgeList = ArrayList<Knowledge>()
        knowledgeList.add(Knowledge(1, "Pet Nutrition 101", R.drawable.img_knowledge_one))
        knowledgeList.add(Knowledge(2, "Regular Vet Check-Ups", R.drawable.img_knowledge_two))
        knowledgeList.add(Knowledge(3, "Recognizing Pet Illness", R.drawable.img_knowledge_three))
        knowledgeList.add(Knowledge(4, "Essential Pet Vaccinations", R.drawable.img_knowledge_four))

        petsViewModel.fetchPetsRandomOrder()
        petsViewModel.fetchPetsSortedByTimestamp()
      
        petsViewModel.isLoading.observe(viewLifecycleOwner){
            if(it){
                loadingDialog.startLoadingDialog()
            } else{
                loadingDialog.dismissDialog()
            }
        }

        val timePetAdapter = HomePetsAdapter({ pet ->
            val go = HomeFragmentDirections.actionHomeFragmentToPetDetailFragment(pet)
            findNavController().navigate(go)
        }, { pet, isFavorited ->
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
        val randomPetAdapter = HomePetsAdapter({pet ->
            val go = HomeFragmentDirections.actionHomeFragmentToPetDetailFragment(pet)
            findNavController().navigate(go)
        }, { pet, isFavorited ->
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
        binding.rvNewPets.apply {
            layoutManager = GridLayoutManager(requireContext(),2)
            adapter = timePetAdapter
            isNestedScrollingEnabled = false
        }

        binding.rvRecommendedPets.apply {
            layoutManager = GridLayoutManager(requireContext(),2)
            adapter = randomPetAdapter
            isNestedScrollingEnabled = false
        }


        val knowledgeAdapter = HomeKnowledgeAdapter {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            startActivity(browserIntent)
        }

        binding.btnViewAllKnowledge.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.hillspet.com/about-us/press-releases"))
            startActivity(browserIntent)
        }
        binding.rvKnowledge.apply {
            layoutManager = GridLayoutManager(requireContext(),2)
            adapter = knowledgeAdapter
            isNestedScrollingEnabled = false
        }

        knowledgeAdapter.submitList(knowledgeList)

        petsViewModel.petsSortedByTimestamp.observe(viewLifecycleOwner){
            if(it.size >= 2){
                val onlyTwo = it.take(2)
                timePetAdapter.submitList(onlyTwo)
            }else{
                timePetAdapter.submitList(it)
            }

        }

        petsViewModel.errorMsg.observe(viewLifecycleOwner){
            makeToast(it)
        }

        petsViewModel.petsRandomOrder.observe(viewLifecycleOwner){
            if(it.size >= 2){
                val onlyTwo = it.take(2)
                randomPetAdapter.submitList(onlyTwo)
            }else{
                randomPetAdapter.submitList(it)
            }
        }

        binding.btnViewAllNew.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_allPetsFragment)
        }

        binding.btnViewAllRecom.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_allPetsFragment)
        }

        binding.chipToClick.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_allPetsFragment)
        }

//        binding.catCategory.isEnabled = false
//        binding.dogCategory.isEnabled = false
//        binding.birdCategory.isEnabled = false
//        binding.rabbitCategory.isEnabled = false
//        binding.hamsterCategory.isEnabled = false
//        binding.allCategory.isEnabled = false
//
//        binding.chipScroll.setOnClickListener{
//            findNavController().navigate(R.id.action_homeFragment_to_allPetsFragment)
//        }
//
//        binding.horizontallScroll.setOnClickListener{
//            findNavController().navigate(R.id.action_homeFragment_to_allPetsFragment)
//        }

        binding.chipGroup.setOnCheckedStateChangeListener { chipGroup, ints ->
            binding.allCategory.isChecked = true
        }

        binding.svToClick.setOnClickListener {
            val toAllPets = HomeFragmentDirections.actionHomeFragmentToAllPetsFragment()
            toAllPets.isSearchMode = true
            findNavController().navigate(toAllPets)

        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.svButton.setOnClickListener{
            val toAllPets = HomeFragmentDirections.actionHomeFragmentToAllPetsFragment()
            toAllPets.isSearchMode = true
            findNavController().navigate(toAllPets)
        }

        val newLoadingDialog = LoadingDialog(requireContext())
        petsViewModel.getAllFavorites(FirebaseAuth.getInstance().currentUser?.uid ?:"")

        petsViewModel.isLoadingFav.observe(viewLifecycleOwner){
            if(it) newLoadingDialog.startLoadingDialog() else newLoadingDialog.dismissDialog()
        }

        petsViewModel.errorMsg.observe(viewLifecycleOwner){
            makeToast(it)
        }

        petsViewModel.favorites.observe(viewLifecycleOwner){
            randomPetAdapter.submitFavorite(it)
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