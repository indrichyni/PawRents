package com.android.pawrents.ui.favorite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.pawrents.R
import com.android.pawrents.data.model.Favorite
import com.android.pawrents.data.model.Pet
import com.android.pawrents.databinding.ItemLayoutFavBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


class FavoriteAdapter(private val chooseCallback : (Pet) -> Unit, private val toDetailCallback: (Pet) -> Unit): RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    private var petList: List<Pet> = listOf()

    fun submitList(pets: List<Pet>) {
        petList = pets
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteAdapter.FavoriteViewHolder {
        val binding = ItemLayoutFavBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteAdapter.FavoriteViewHolder(binding, chooseCallback, toDetailCallback)
    }

    override fun onBindViewHolder(holder: FavoriteAdapter.FavoriteViewHolder, position: Int) {
        holder.bind(petList[position])
    }

    override fun getItemCount(): Int = petList.size

    class FavoriteViewHolder(private val binding: ItemLayoutFavBinding, private val chooseCallback : (Pet) -> Unit, private val toDetailCallback: (Pet) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pet: Pet) {
            Glide.with(binding.root.context).load(pet.photoLink).diskCacheStrategy(DiskCacheStrategy.DATA).override(300).into(binding.ivPet)
            val resource = if(pet.gender == "Male") R.drawable.img_pet_male else R.drawable.img_pet_female
            binding.ivPetGender.setImageResource(resource)
            binding.tvPetColor.text = pet.color
            binding.tvPetName.text = pet.name
            binding.tvPetLocation.text = pet.location
            binding.root.setOnClickListener {
                toDetailCallback.invoke(pet)
            }

            binding.btnFavedDetail.setOnClickListener {
                chooseCallback.invoke(pet)
            }
        }
    }

}