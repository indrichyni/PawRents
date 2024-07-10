package com.android.pawrents.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.pawrents.data.model.Adoption
import com.android.pawrents.data.model.Pet
import com.android.pawrents.data.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdoptionViewModel: ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val adoptionDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Adoption")

    private val _adoptionData = MutableLiveData<List<Adoption>>()
    val adoptionData: LiveData<List<Adoption>> get() = _adoptionData

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String> get() = _errorMsg

    fun fetchAllAdoption(currentPet: Pet) {
        _isLoading.value = true
        adoptionDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val adoptionList = mutableListOf<Adoption>()
                for (adoptionSnapshot in dataSnapshot.children) {
                    val adoption = adoptionSnapshot.getValue(Adoption::class.java)
                    if (adoption != null) {
                        adoptionList.add(adoption)
                    }
                }
                val adoptionCurrentPet = adoptionList.filter { it.petId == currentPet.id }
                _adoptionData.value = adoptionCurrentPet
                _isLoading.value = false
            }

            override fun onCancelled(databaseError: DatabaseError) {
                _isLoading.value = false
                _errorMsg.value = databaseError.message
            }
        })
    }

}