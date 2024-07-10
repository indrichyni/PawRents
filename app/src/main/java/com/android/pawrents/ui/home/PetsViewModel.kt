package com.android.pawrents.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.android.pawrents.data.model.Favorite
import com.android.pawrents.data.model.Pet
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class PetsViewModel : ViewModel() {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("pets")
    private val favoriteRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Favorite")

    private val _petsSortedByTimestamp = MutableLiveData<List<Pet>>()
    val petsSortedByTimestamp: LiveData<List<Pet>> get() = _petsSortedByTimestamp

    private val _petsRandomOrder = MutableLiveData<List<Pet>>()
    val petsRandomOrder: LiveData<List<Pet>> get() = _petsRandomOrder

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isLoadingFav = MutableLiveData<Boolean>()
    val isLoadingFav: LiveData<Boolean> get() = _isLoadingFav

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String> get() = _errorMsg

    private val _petsFiltered = MutableLiveData<List<Pet>>()
    val petsFiltered: LiveData<List<Pet>> get() = _petsFiltered

    private val _favorites = MutableLiveData<List<Favorite>>()
    val favorites: LiveData<List<Favorite>> get() = _favorites

    private val _filterText = MutableLiveData<String>()
    val filterText: LiveData<String> get() = _filterText

    private var currentCategory: String = "all"
    private var searchText: String = ""
    private var adopted: Boolean? = null

    init {
        fetchPetsSortedByTimestamp()
        fetchPetsRandomOrder()
        updateFilterText()
    }

    fun getAllFavorites(currentUserId: String) {
        _isLoadingFav.value = true
        val query: Query = favoriteRef.orderByChild("userId").equalTo(currentUserId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favoriteList = mutableListOf<Favorite>()
                for (childSnapshot in snapshot.children) {
                    val favorite = childSnapshot.getValue(Favorite::class.java)
                    if (favorite != null) {
                        favoriteList.add(favorite)
                    }
                }
                _favorites.value = favoriteList
                _isLoadingFav.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                _isLoadingFav.value = false
                _errorMsg.value = error.message
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
                _errorMsg.value = databaseError.message
            }
        })
    }

    fun filterPetsByCategory(category: String) {
        currentCategory = category
        applyFilters()
    }

    fun searchPets(query: String) {
        searchText = query
        applyFilters()
    }

    fun filterByAdopted() {
        adopted = when (adopted) {
            null -> false
            false -> true
            else -> null
        }
        applyFilters()
        updateFilterText()
    }

    private fun applyFilters() {
        val filteredList = _petsSortedByTimestamp.value?.filter { pet ->
            val matchesCategory = if (currentCategory == "all") true else pet.category == currentCategory
            val matchesSearch = pet.name.contains(searchText, ignoreCase = true)
            val adoptedPet = if (adopted != null) {
                if (adopted == true) pet.adoptUserId != null else pet.adoptUserId == null
            } else true
            matchesCategory && matchesSearch && adoptedPet
        }
        _petsFiltered.value = filteredList ?: listOf()
    }

    private fun updateFilterText() {
        _filterText.value = when (adopted) {
            null -> "List of All Animals Added by Users"
            true -> "List of Adopted Animals"
            false -> "List of Animals Available for Adoption"
        }
    }

    fun fetchPetsRandomOrder() {
        _isLoading.value = true
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val petsList = mutableListOf<Pet>()
                for (petSnapshot in dataSnapshot.children) {
                    val pet = petSnapshot.getValue(Pet::class.java)
                    if (pet != null) {
                        petsList.add(pet)
                    }
                }
                petsList.shuffle()
                _isLoading.value = false
                _petsRandomOrder.value = petsList
            }

            override fun onCancelled(databaseError: DatabaseError) {
                _isLoading.value = false
                _errorMsg.value = databaseError.message
            }
        })
    }
}