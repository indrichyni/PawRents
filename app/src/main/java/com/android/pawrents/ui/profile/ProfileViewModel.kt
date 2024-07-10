package com.android.pawrents.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.pawrents.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileViewModel: ViewModel() {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val userRef: DatabaseReference = database.getReference("Users")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> get() = _currentUser

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String> get() = _errorMsg

    fun getCurrentUser() {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != null) {
            _loading.value = true
            userRef.child(currentUserUid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if(user!=null){
                        _currentUser.value = user!!
                    }else{
                        _errorMsg.value = "User not found"
                    }
                    _loading.value = false
                }

                override fun onCancelled(error: DatabaseError) {
                    _errorMsg.value = error.message
                    _loading.value = false
                }
            })
        }
    }
}