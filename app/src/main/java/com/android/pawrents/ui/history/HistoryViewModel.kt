package com.android.pawrents.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.pawrents.data.model.Adoption
import com.android.pawrents.data.model.Pet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class HistoryViewModel: ViewModel() {

    private val adoptionDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Adoption")
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("pets")


    private val _isAdoptionLoading = MutableLiveData<Boolean>()
    val isAdoptionLoading: LiveData<Boolean> get() = _isAdoptionLoading

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String> get() = _errorMsg

    private val _adoptionData = MutableLiveData<List<Adoption>>()
    val adoptionData: LiveData<List<Adoption>> get() = _adoptionData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isOwnLoading = MutableLiveData<Boolean>()
    val isOwnLoading: LiveData<Boolean> get() = _isOwnLoading

    private val _petsFiltered = MutableLiveData<List<Pet>>()
    val petsFiltered: LiveData<List<Pet>> get() = _petsFiltered

    private val _ownPetsFiltered = MutableLiveData<List<Pet>>()
    val ownPetsFiltered: LiveData<List<Pet>> get() = _ownPetsFiltered

    private val _petsSortedByTimestamp = MutableLiveData<List<Pet>>()
    val petsSortedByTimestamp: LiveData<List<Pet>> get() = _petsSortedByTimestamp

    private val _currentPetOwned = MutableLiveData<List<Pet>>()
    val currentPetOwnedList: LiveData<List<Pet>> get() = _currentPetOwned

    private var searchText: String = ""
    private var adopted:Boolean? = null

    fun searchPets(query: String) {
        searchText = query
        applyFilters()
    }

    fun filterByAdopted(){
        adopted = when (adopted) {
            null -> {
                false
            }
            false -> {
                true
            }
            else -> {
                null
            }
        }

        applyFilters()
    }

    private fun applyFilters() {
        val filteredList = _petsSortedByTimestamp.value?.filter {pet ->
            val matchesSearch = pet.name.contains(searchText, ignoreCase = true)
            val adoptedPet = if(adopted!=null) { if(adopted == true) pet.adoptUserId!=null else pet.adoptUserId==null} else true
            matchesSearch && adoptedPet }
        _petsFiltered.value = filteredList ?: listOf()

        val ownFilteredList = _currentPetOwned.value?.filter {pet ->
            val matchesSearch = pet.name.contains(searchText, ignoreCase = true)
            val adoptedPet = if(adopted!=null) { if(adopted == true) pet.adoptUserId!=null else pet.adoptUserId==null} else true
            matchesSearch && adoptedPet }
        _ownPetsFiltered.value = ownFilteredList ?: listOf()
    }


    fun fetchPetOwned(){
        _isOwnLoading.value = true
        val query: Query = database.orderByChild("userId").equalTo(FirebaseAuth.getInstance().currentUser?.uid ?: "")
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val petsList = mutableListOf<Pet>()
                for (petSnapshot in dataSnapshot.children) {
                    val pet = petSnapshot.getValue(Pet::class.java)
                    if (pet != null) {
                        petsList.add(pet)
                    }
                }
                petsList.sortByDescending { it.timestamp }
                _isOwnLoading.value = false
                _currentPetOwned.value = petsList
                applyFilters()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                _isOwnLoading.value = false
                _errorMsg.value  =databaseError.message
            }
        })
    }

    fun fetchPetsSortedByTimestamp() {
        _isLoading.value = true
        database.orderByChild("timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val petsList = mutableListOf<Pet>()
                for (petSnapshot in dataSnapshot.children) {
                    val pet = petSnapshot.getValue(Pet::class.java)
                    if (pet != null) {
                        petsList.add(pet)
                    }
                }
                petsList.sortByDescending { it.timestamp }
                _isLoading.value = false
                _petsSortedByTimestamp.value = petsList
                applyFilters()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                _isLoading.value = false
                _errorMsg.value  =databaseError.message
            }
        })
    }

    fun fetchAllAdoption() {
        _isAdoptionLoading.value = true
        val query: Query = adoptionDatabaseReference.orderByChild("adoptUserId").equalTo(FirebaseAuth.getInstance().currentUser?.uid ?: "")
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val adoptionList = mutableListOf<Adoption>()
                for (adoptionSnapshot in dataSnapshot.children) {
                    val adoption = adoptionSnapshot.getValue(Adoption::class.java)
                    if (adoption != null) {
                        adoptionList.add(adoption)
                    }
                }
                _adoptionData.value = adoptionList
                _isAdoptionLoading.value = false
            }

            override fun onCancelled(databaseError: DatabaseError) {
                _isAdoptionLoading.value = false
                _errorMsg.value = databaseError.message
            }
        })
    }

}