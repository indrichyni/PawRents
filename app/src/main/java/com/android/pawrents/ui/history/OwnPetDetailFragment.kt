package com.android.pawrents.ui.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.pawrents.R
import com.android.pawrents.data.model.Pet
import com.android.pawrents.databinding.FragmentHistoryBinding
import com.android.pawrents.databinding.FragmentOwnPetDetailBinding
import com.android.pawrents.ui.LoadingDialog
import com.android.pawrents.ui.pets.PetDetailFragmentDirections
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth


class OwnPetDetailFragment : Fragment() {


    private var _binding: FragmentOwnPetDetailBinding? = null
    private val binding get() = _binding!!
    private val ownViewModel: CandidateViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwnPetDetailBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentPet = OwnPetDetailFragmentArgs.fromBundle(arguments as Bundle).currentPet

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        initView(currentPet)
        initCandidate(currentPet)

    }

    private fun initCandidate(currentPet: Pet) {

        val loadingDialog = LoadingDialog(requireContext())

        val candidateAdapter = CandidateAdapter { user, adoption ->
            val go = OwnPetDetailFragmentDirections.actionOwnPetDetailFragmentToUserResponseFragment(currentPet, adoption, user)
            findNavController().navigate(go)
        }

        ownViewModel.getAdoptionsByPetId(currentPet.id)
        binding.rvCandidate.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = candidateAdapter
            isNestedScrollingEnabled = false
        }

        ownViewModel.adoptions.observe(viewLifecycleOwner){adoptions ->
            candidateAdapter.submitAdoptionList(adoptions)
        }

        ownViewModel.users.observe(viewLifecycleOwner) { users ->
           candidateAdapter.submitUserList(users)
            if(currentPet.adoptUserId != null){
                val currentUser = users.firstOrNull { it.uid == currentPet.adoptUserId}
                if(currentUser!=null){
                    binding.tvAccepted.text = "You already accepted ${currentUser.username}'s request"
                    binding.tvAccepted.visibility = View.VISIBLE
                }else{
                    binding.tvAccepted.text = "You already accepted someone's request"
                    binding.tvAccepted.visibility = View.VISIBLE
                }
            }else{
                binding.tvAccepted.visibility = View.GONE
            }
        }

        ownViewModel.errorMsg.observe(viewLifecycleOwner){
            makeToast(it)
        }

        ownViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) loadingDialog.startLoadingDialog() else loadingDialog.dismissDialog()
        }


    }

    private fun initView(currentPet: Pet) {
        binding.apply {
            Glide.with(requireContext()).load(currentPet.photoLink).diskCacheStrategy(
                DiskCacheStrategy.NONE).override(600,600).into(ivPetDetail)
            tvPetName.text = currentPet.name
            tvPetCategory.text = "(${currentPet.category})"
            tvPetLocation.text = currentPet.location
            tvPetColor.text = currentPet.color
            val resource = if(currentPet.gender == "Male") R.drawable.img_pet_male else R.drawable.img_pet_female
            binding.ivPetGender.setImageResource(resource)
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