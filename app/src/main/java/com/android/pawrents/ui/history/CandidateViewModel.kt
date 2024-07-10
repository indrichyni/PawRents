package com.android.pawrents.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.pawrents.data.model.Adoption
import com.android.pawrents.data.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CandidateViewModel: ViewModel() {

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String> get() = _errorMsg

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _adoptions = MutableLiveData<List<Adoption>>()
    val adoptions: LiveData<List<Adoption>> get() = _adoptions

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val adoptionRef: DatabaseReference = database.getReference("Adoption")
    private val userRef: DatabaseReference = database.getReference("Users")

    fun getAdoptionsByPetId(petId: String) {
        _isLoading.value = true
        adoptionRef.orderByChild("petId").equalTo(petId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val adoptionList = mutableListOf<Adoption>()
                    for (childSnapshot in snapshot.children) {
                        val adoption = childSnapshot.getValue(Adoption::class.java)
                        if (adoption != null) {
                            adoptionList.add(adoption)
                        }
                    }
                    _adoptions.value = adoptionList
                    fetchUsers(adoptionList)
                }

                override fun onCancelled(error: DatabaseError) {
                    _isLoading.value = false
                    _errorMsg.value = error.message
                }
            })
    }

    private fun fetchUsers(adoptionList: List<Adoption>) {
        val userIds = adoptionList.map { it.adoptUserId }.distinct()
        val usersList = mutableListOf<User>()

        val userQuery = userRef.orderByKey()
        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val user = childSnapshot.getValue(User::class.java)
                    if (user != null && user.uid in userIds) {
                        usersList.add(user)
                    }
                }
                _users.value = usersList
                _isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                _isLoading.value = false
                _errorMsg.value = error.message
            }
        })
    }

}