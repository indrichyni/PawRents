package com.android.pawrents.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.pawrents.R
import com.android.pawrents.data.model.Adoption
import com.android.pawrents.data.model.Pet
import com.android.pawrents.databinding.ItemLayoutWantAdoptBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class WantAdoptAdapter(
    private val chooseCallback : (Pet, Adoption) -> Unit, private val deleteCallback : (Adoption) -> Unit):
    RecyclerView.Adapter<WantAdoptAdapter.WantViewHolder>() {

    private var petList: List<Pet> = listOf()

    fun submitPetList(pets: List<Pet>) {
        petList = pets
        notifyDataSetChanged()
    }

    private var adoptionList: MutableList<Adoption> = mutableListOf()

    fun submitAdoptionList(adoptions: List<Adoption>) {
        adoptionList = adoptions.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WantAdoptAdapter.WantViewHolder {
        val binding = ItemLayoutWantAdoptBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WantAdoptAdapter.WantViewHolder(binding, chooseCallback, deleteCallback)
    }

    override fun onBindViewHolder(holder: WantAdoptAdapter.WantViewHolder, position: Int) {
        holder.bind(adoptionList[position], petList, position)
    }

    override fun getItemCount(): Int = adoptionList.size

    fun removeItemAdoption(adoption: Adoption){
        adoptionList.remove(adoption)
        notifyDataSetChanged()
    }

    class WantViewHolder(
        private val binding: ItemLayoutWantAdoptBinding, private val chooseCallback : (Pet, Adoption) -> Unit,
        private val deleteCallback : (Adoption) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(adoption: Adoption, petList: List<Pet>, position:Int) {
            val wantToBeAdoptedPet = petList.firstOrNull { it.id == adoption.petId }
            if(wantToBeAdoptedPet!=null) {
                binding.rootLayout.visibility = View.VISIBLE
                val paramNow = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                paramNow.leftMargin = binding.root.context.resources.getDimensionPixelSize(R.dimen._4dp)
                paramNow.rightMargin = binding.root.context.resources.getDimensionPixelSize(R.dimen._4dp)
                paramNow.bottomMargin = binding.root.context.resources.getDimensionPixelSize(R.dimen._8dp)
                paramNow.topMargin = binding.root.context.resources.getDimensionPixelSize(R.dimen._8dp)

                binding.rootLayout.layoutParams = paramNow

                binding.root.visibility = View.VISIBLE
                Glide.with(binding.root.context).load(wantToBeAdoptedPet.photoLink)
                    .diskCacheStrategy(
                        DiskCacheStrategy.DATA
                    ).override(300).into(binding.ivPet)
                val resource =
                    if (wantToBeAdoptedPet.gender == "Male") R.drawable.img_pet_male else R.drawable.img_pet_female
                binding.ivPetGender.setImageResource(resource)
                binding.tvPetColor.text = wantToBeAdoptedPet.color
                binding.tvPetName.text = wantToBeAdoptedPet.name
                binding.tvPetLocation.text = wantToBeAdoptedPet.location
                binding.root.setOnClickListener {
                    chooseCallback.invoke(wantToBeAdoptedPet, adoption)
                }
                binding.tvAdoptionStatus.text = adoption.status
            }else {
                binding.rootLayout.visibility = View.GONE
                binding.rootLayout.layoutParams = RecyclerView.LayoutParams(0,  0)
            }
        }
    }
    }