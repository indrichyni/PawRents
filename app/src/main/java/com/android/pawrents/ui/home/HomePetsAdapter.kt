package com.android.pawrents.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.pawrents.R
import com.android.pawrents.data.model.Favorite
import com.android.pawrents.data.model.Pet
import com.android.pawrents.databinding.ItemLayoutPetBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth

class HomePetsAdapter(private val chooseCallback : (Pet) -> Unit, private val favoriteCallback : (Pet, Boolean) -> Unit): RecyclerView.Adapter<HomePetsAdapter.PetViewHolder>() {

    private var petList: List<Pet> = listOf()
    private var favList: List<Favorite> = listOf()

    fun submitList(pets: List<Pet>) {
        petList = pets
        notifyDataSetChanged()
    }

    fun submitFavorite(fav : List<Favorite>){
        favList = fav
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val binding = ItemLayoutPetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PetViewHolder(binding, chooseCallback, favoriteCallback)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(petList[position], favList)
    }

    override fun getItemCount(): Int = petList.size

    class PetViewHolder(private val binding: ItemLayoutPetBinding, private val chooseCallback : (Pet) -> Unit, private val favoriteCallback : (Pet, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pet: Pet, fav : List<Favorite>) {
            binding.tvPetName.text = pet.name
            Glide.with(binding.root.context).load(pet.photoLink).diskCacheStrategy(DiskCacheStrategy.DATA).override(300).into(binding.ivPetsImage)
            val resource = if(pet.gender == "Male") R.drawable.img_pet_male else R.drawable.img_pet_female
            binding.ivPetGender.setImageResource(resource)
            binding.tvPetColor.text = pet.color
            binding.tvPetLocation.text = pet.location
            binding.root.setOnClickListener {
                chooseCallback.invoke(pet)
            }

//            if(FirebaseAuth.getInstance().currentUser == null){
//                binding.btnFavorite.visibility = View.GONE
//                binding.btnFavorited.visibility = View.GONE
//            }else{
//
//            }
            if(fav.any { currentFav -> currentFav.petId == pet.id }){
                binding.btnFavorite.visibility = View.GONE
                binding.btnFavorited.visibility = View.VISIBLE
            }else{
                binding.btnFavorite.visibility = View.VISIBLE
                binding.btnFavorited.visibility = View.GONE
            }


            binding.btnFavorite.setOnClickListener {
                favoriteCallback.invoke(pet, false)
            }

            binding.btnFavorited.setOnClickListener {
                favoriteCallback.invoke(pet, true)
            }
        }
    }
}