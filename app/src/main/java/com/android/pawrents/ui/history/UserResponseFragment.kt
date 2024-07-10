package com.android.pawrents.ui.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.pawrents.R
import com.android.pawrents.data.model.Pet
import com.android.pawrents.data.model.User
import com.android.pawrents.databinding.FragmentFavoriteBinding
import com.android.pawrents.databinding.FragmentUserResponseBinding
import com.android.pawrents.ui.LoadingDialog
import com.android.pawrents.ui.favorite.FavoriteViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar


class UserResponseFragment : Fragment() {

    private var _binding: FragmentUserResponseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserResponseBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        val currentAdoption = UserResponseFragmentArgs.fromBundle(arguments as Bundle).currentAdoption
        val currentUser = UserResponseFragmentArgs.fromBundle(arguments as Bundle).candidateUser
        val currentPet = UserResponseFragmentArgs.fromBundle(arguments as Bundle).currentPet

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.apply {
            Glide.with(requireContext()).load(currentPet.photoLink).diskCacheStrategy(
                DiskCacheStrategy.NONE).override(600,600).into(ivPetDetail)
            tvPetName.text = currentPet.name
            tvPetCategory.text = "(${currentPet.category})"
            tvPetLocation.text = currentPet.location

            etName.editText?.setText(currentAdoption.adoptName)
            etAddress.editText?.setText(currentAdoption.adoptAddress)
            etCost.editText?.setText(currentAdoption.adoptCost)
            if(currentAdoption.adoptAllergy){
                radioAllergyYes.isChecked = true
                radioAllergyNo.isChecked = false
            }else{
                radioAllergyYes.isChecked = false
                radioAllergyNo.isChecked = true
            }

            if(currentAdoption.adoptOwn){
                radioOwnYes.isChecked = true
                radioOwnNo.isChecked = false
            }else{
                radioOwnYes.isChecked = false
                radioOwnNo.isChecked = true
            }

            if(currentAdoption.adoptBusy == "Very Busy"){
                radioBusyVery.isChecked = true
                radioBusyNeutral.isChecked = false
                radioBusyNo.isChecked = false
            }else if(currentAdoption.adoptBusy == "Busy"){
                radioBusyVery.isChecked = false
                radioBusyNeutral.isChecked = true
                radioBusyNo.isChecked = false
            }else{
                radioBusyVery.isChecked = false
                radioBusyNeutral.isChecked = false
                radioBusyNo.isChecked = true
            }

            if(currentAdoption.status == "Accepted"){
                binding.actionButton.visibility = View.GONE
                binding.btnDecline.visibility = View.GONE
                binding.btnAccept.visibility = View.GONE
                binding.tvStatusDone.text = "You already accepted this request.\n We already inform the user, wait until they contact you."
                binding.tvStatusDone.visibility = View.VISIBLE
            }else if(currentAdoption.status == "Declined"){
                binding.actionButton.visibility = View.GONE
                binding.btnDecline.visibility = View.GONE
                binding.btnAccept.visibility = View.GONE
                binding.tvStatusDone.text = "You already declined this request."
                binding.tvStatusDone.visibility = View.VISIBLE
            }else {
                if(currentPet.adoptUserId !=null){
                    binding.tvStatusDone.text = "You already accepted adopt request for ${currentPet.name} from someone else."
                    binding.tvStatusDone.visibility = View.VISIBLE
                    binding.actionButton.visibility = View.GONE
                    binding.btnDecline.visibility = View.GONE
                    binding.btnAccept.visibility = View.GONE
                }else{
                    binding.actionButton.visibility = View.VISIBLE
                    binding.btnDecline.visibility = View.VISIBLE
                    binding.btnAccept.visibility = View.VISIBLE
                    binding.tvStatusDone.visibility = View.GONE
                    btnDecline.setOnClickListener {
                        showAcceptDialog(currentAdoption.adoptionId, false, currentPet, currentUser)
                    }

                    btnAccept.setOnClickListener {
                        showAcceptDialog(currentAdoption.adoptionId, true, currentPet, currentUser)
                    }
                }
            }


        }

    }

    private fun showAcceptDialog(adoptionId: String, accepted: Boolean, currentPet: Pet, currentUser: User) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(if(accepted) "Accept" else "Decline")
        builder.setMessage(if(accepted) "Are you sure you want to accept this request?" else "Are you sure you want to decline this request?")

        builder.setPositiveButton("Yes") { dialog, which ->
            dialog.dismiss()
            editStatus(adoptionId, accepted, currentPet, currentUser)
        }

        builder.setNegativeButton(
            "No"
        ) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun editStatus(adoptionId: String, accepted: Boolean, currentPet: Pet, currentUser: User){
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val adoptionRef: DatabaseReference = database.getReference("Adoption")
        val adoptionRefTwo: DatabaseReference = database.getReference("Adoption")
        val petRef: DatabaseReference = database.getReference("pets")

        val loadingDialog = LoadingDialog(requireContext())

        loadingDialog.startLoadingDialog()
        adoptionRef.child(adoptionId).child("status").setValue(if(accepted) "Accepted" else "Declined")
            .addOnSuccessListener {
                if(accepted){
                    adoptionRefTwo.child(adoptionId).child("contactBefore").setValue(getOneMonthLaterMillis())
                        .addOnSuccessListener {
                            petRef.child(currentPet.id).child("adoptUserId").setValue(currentUser.uid)
                                .addOnSuccessListener {
                                    val notifMap = mapOf(
                                        "notifAdoptPet" to true)
                                    FirebaseDatabase.getInstance().getReference("Users")
                                        .child(currentUser.uid)
                                        .updateChildren(notifMap)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                loadingDialog.dismissDialog()
                                                makeToast("Accepted, we will inform the candidate.")
                                                findNavController().popBackStack()
                                            } else {
                                                loadingDialog.dismissDialog()
                                                makeToast("Error : ${task.exception?.message.toString()}")
                                            }
                                        }
                                }
                                .addOnFailureListener {
                                    loadingDialog.dismissDialog()
                                    makeToast(it.message.toString())
                                }
                        }
                        .addOnFailureListener {
                            loadingDialog.dismissDialog()
                            makeToast(it.message.toString())
                        }
                }else{
                    loadingDialog.dismissDialog()
                    makeToast("Declined")
                    findNavController().popBackStack()
                }
            }
            .addOnFailureListener {
                loadingDialog.dismissDialog()
                makeToast(it.message.toString())
            }
    }

    fun getOneMonthLaterMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.MONTH, 1)
        return calendar.timeInMillis
    }

    private fun makeToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}