package com.android.pawrents.ui.history

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.pawrents.R
import com.android.pawrents.data.model.Pet
import com.android.pawrents.databinding.FragmentFavoriteBinding
import com.android.pawrents.databinding.FragmentHistoryBinding
import com.android.pawrents.ui.LoadingDialog
import com.android.pawrents.ui.favorite.FavoriteViewModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private var ownMode: Boolean = true
    private val historyViewModel: HistoryViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loadingDialog = LoadingDialog(requireContext())
        val adoptionLoadingDialog = LoadingDialog(requireContext())
        val ownedLoadingDialog = LoadingDialog(requireContext())

        ownMode = true
        handleSwitchMode()

        historyViewModel.fetchAllAdoption()
        historyViewModel.fetchPetsSortedByTimestamp()
        historyViewModel.fetchPetOwned()

        historyViewModel.isLoading.observe(viewLifecycleOwner){
            if(it) loadingDialog.startLoadingDialog() else loadingDialog.dismissDialog()
        }

        historyViewModel.isOwnLoading.observe(viewLifecycleOwner){
            if(it) ownedLoadingDialog.startLoadingDialog() else ownedLoadingDialog.dismissDialog()
        }

        historyViewModel.isAdoptionLoading.observe(viewLifecycleOwner){
            if(it) adoptionLoadingDialog.startLoadingDialog() else adoptionLoadingDialog.dismissDialog()
        }

        historyViewModel.errorMsg.observe(viewLifecycleOwner){
            makeToast(it)
        }

        val ownPetAdapter = OwnPetAdapter({
            val go = HistoryFragmentDirections.actionHistoryFragmentToOwnPetDetailFragment(it)
            findNavController().navigate(go)
        },{
          showDeleteDialog(it)
        },{
           val go = HistoryFragmentDirections.actionHistoryFragmentToEditFragment(it)
            findNavController().navigate(go)
        })

        val wantToAdoptAdapter = WantAdoptAdapter({ pet, adoption ->
             if(adoption.status == "Accepted"){
                val go = HistoryFragmentDirections.actionHistoryFragmentToApprovedAdoptFragment(pet, adoption)
                 findNavController().navigate(go)
             }else{
                 val go = HistoryFragmentDirections.actionHistoryFragmentToPetDetailFragment(pet)
                 findNavController().navigate(go)
             }
        },{
            (binding.rvWantAdopt.adapter as WantAdoptAdapter).removeItemAdoption(it)
        })

        binding.rvOwnPet.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ownPetAdapter
            isNestedScrollingEnabled = false
        }

        binding.rvWantAdopt.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = wantToAdoptAdapter
            isNestedScrollingEnabled = false
        }

        historyViewModel.adoptionData.observe(viewLifecycleOwner){
            wantToAdoptAdapter.submitAdoptionList(it)
        }

        historyViewModel.ownPetsFiltered.observe(viewLifecycleOwner){
            ownPetAdapter.submitList(it)
        }

        historyViewModel.petsFiltered.observe(viewLifecycleOwner){
            wantToAdoptAdapter.submitPetList(it)
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnWantAdopt.setOnClickListener {
            if(ownMode){
                ownMode = false
                handleSwitchMode()
            }
        }

        binding.btnOwnPet.setOnClickListener {
            if(!ownMode){
                ownMode = true
                handleSwitchMode()
            }
        }

        binding.btnFilterAdopted.setOnClickListener {
            historyViewModel.filterByAdopted()
        }

        binding.svPets.setOnClickListener {
            binding.svPets.isIconified= false
            binding.svPets.requestFocus()
        }

        binding.svPets.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { historyViewModel.searchPets(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { historyViewModel.searchPets(it) }
                return true
            }
        })

    }

    private fun showDeleteDialog(currentPet: Pet) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("DELETE")
        builder.setMessage("Are you sure you want to delete ${currentPet.name}?")

        builder.setPositiveButton("Yes") { dialog, which ->
            dialog.dismiss()
            deletePet(currentPet)
        }

        builder.setNegativeButton(
            "No"
        ) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun deletePet(currentPet: Pet) {
        val petRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("pets")
        val deleteLoadingDialog = LoadingDialog(requireContext())
        deleteLoadingDialog.startLoadingDialog()
        petRef.child(currentPet.id).removeValue().addOnSuccessListener {
            deleteLoadingDialog.dismissDialog()
            makeToast("Pet deleted.")
            historyViewModel.fetchPetOwned()
        }.addOnFailureListener {
            deleteLoadingDialog.dismissDialog()
            makeToast("Error : ${it.message.toString()}")
        }
    }

    private fun handleSwitchMode(){
        if(ownMode){
            binding.btnWantAdopt.setBackgroundResource(R.drawable.bluetrans_box_10)
            binding.btnWantAdopt.setTextColor(requireContext().getColor(R.color.new_gray))
            binding.btnOwnPet.setBackgroundResource(R.drawable.deepblue_box_10)
            binding.btnOwnPet.setTextColor(requireContext().getColor(R.color.white))

            binding.rvOwnPet.visibility = View.VISIBLE
            binding.rvWantAdopt.visibility = View.GONE
        }else{
            binding.btnOwnPet.setBackgroundResource(R.drawable.bluetrans_box_10)
            binding.btnOwnPet.setTextColor(requireContext().getColor(R.color.new_gray))
            binding.btnWantAdopt.setBackgroundResource(R.drawable.deepblue_box_10)
            binding.btnWantAdopt.setTextColor(requireContext().getColor(R.color.white))

            binding.rvOwnPet.visibility = View.GONE
            binding.rvWantAdopt.visibility = View.VISIBLE
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