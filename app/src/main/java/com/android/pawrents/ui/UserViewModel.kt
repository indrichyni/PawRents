package com.android.pawrents.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.pawrents.data.model.Pet
import com.android.pawrents.data.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserViewModel: ViewModel() {

    private val usersDatabase: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")

    private val _petCreator = MutableLiveData<User?>()
    val petCreator: LiveData<User?> get() = _petCreator

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String> get() = _errorMsg


    fun fetchAllUsers(currentPet: Pet) {
        _isLoading.value = true
        usersDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val usersList = mutableListOf<User>()
                for (userSnapshot in dataSnapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null) {
                        usersList.add(user)
                    }
                }
                _users.value = usersList
                getPetCreator(currentPet)
                _isLoading.value = false
            }

            override fun onCancelled(databaseError: DatabaseError) {
                _isLoading.value = false
                _errorMsg.value = databaseError.message
            }
        })
    }

    fun getPetCreator(pet: Pet) {
        val creator = _users.value?.find { it.uid == pet.userId }
        _petCreator.value = creator
    }
}