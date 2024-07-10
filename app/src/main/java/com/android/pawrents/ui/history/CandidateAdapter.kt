package com.android.pawrents.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.pawrents.R
import com.android.pawrents.data.model.Adoption
import com.android.pawrents.data.model.Pet
import com.android.pawrents.data.model.User
import com.android.pawrents.databinding.ItemLayoutCandidateBinding
import com.android.pawrents.databinding.ItemLayoutWantAdoptBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class CandidateAdapter(private val chooseCallback : (User, Adoption) -> Unit):
    RecyclerView.Adapter<CandidateAdapter.CandidateViewHolder>() {

    private var userList: List<User> = listOf()

    fun submitUserList(users: List<User>) {
        userList = users
        notifyDataSetChanged()
    }

    private var adoptionList: List<Adoption> = listOf()

    fun submitAdoptionList(adoptions: List<Adoption>) {
        adoptionList = adoptions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CandidateAdapter.CandidateViewHolder {
        val binding = ItemLayoutCandidateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CandidateAdapter.CandidateViewHolder(binding, chooseCallback)
    }

    override fun onBindViewHolder(holder: CandidateAdapter.CandidateViewHolder, position: Int) {
        holder.bind(adoptionList[position], userList)
    }

    override fun getItemCount(): Int = adoptionList.size

    class CandidateViewHolder(
        private val binding: ItemLayoutCandidateBinding, private val chooseCallback : (User, Adoption) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(adoption: Adoption, userList: List<User>) {
            val adoptCandidate = userList.firstOrNull { it.uid == adoption.adoptUserId }
            if(adoptCandidate!=null){
                Glide.with(binding.root.context).load(adoptCandidate.profilePic).skipMemoryCache(true).placeholder(R.drawable.img_default_user)
                    .diskCacheStrategy(
                        DiskCacheStrategy.NONE
                    ).centerCrop().
                    override(200,200).into(binding.ivCandidate)
                binding.tvCandidate.text = adoptCandidate.username
                binding.root.setOnClickListener {
                    chooseCallback.invoke(adoptCandidate, adoption)
                }
            }
        }
    }

}