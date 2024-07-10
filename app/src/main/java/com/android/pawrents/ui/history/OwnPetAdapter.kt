package com.android.pawrents.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.pawrents.R
import com.android.pawrents.data.model.Pet
import com.android.pawrents.databinding.ItemLayoutOwnPetBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class OwnPetAdapter(private val chooseCallback : (Pet) -> Unit, private val deleteCallback: (Pet) -> Unit, private val editCallback: (Pet) -> Unit):
    RecyclerView.Adapter<OwnPetAdapter.OwnViewHolder>() {

    private var petList: List<Pet> = listOf()

    fun submitList(pets: List<Pet>) {
        petList = pets
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OwnPetAdapter.OwnViewHolder {
        val binding = ItemLayoutOwnPetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OwnPetAdapter.OwnViewHolder(binding, chooseCallback, deleteCallback, editCallback)
    }

    override fun onBindViewHolder(holder: OwnPetAdapter.OwnViewHolder, position: Int) {
        holder.bind(petList[position])
    }

    override fun getItemCount(): Int = petList.size

    class OwnViewHolder(
        private val binding: ItemLayoutOwnPetBinding, private val chooseCallback : (Pet) -> Unit, private val deleteCallback: (Pet) -> Unit, private val editCallback: (Pet) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pet: Pet) {
            Glide.with(binding.root.context).load(pet.photoLink).diskCacheStrategy(DiskCacheStrategy.DATA).override(300).into(binding.ivPet)
            val resource = if(pet.gender == "Male") R.drawable.img_pet_male else R.drawable.img_pet_female
            binding.ivPetGender.setImageResource(resource)
            binding.tvPetColor.text = pet.color
            binding.tvPetName.text = pet.name
            binding.tvPetLocation.text = pet.location
            binding.root.setOnClickListener {
                chooseCallback.invoke(pet)
            }

            binding.btnEditPet.setOnClickListener {
                editCallback.invoke(pet)
            }

            binding.btnDeletePet.setOnClickListener {
                deleteCallback.invoke(pet)
            }
        }
    }

}