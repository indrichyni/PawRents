package com.android.pawrents.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.pawrents.R
import com.android.pawrents.data.model.Knowledge
import com.android.pawrents.data.model.Pet
import com.android.pawrents.databinding.ItemLayoutKnowledgeBinding
import com.android.pawrents.databinding.ItemLayoutPetBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class HomeKnowledgeAdapter(private val chooseCallback : (String) -> Unit): RecyclerView.Adapter<HomeKnowledgeAdapter.KnowledgeViewHolder>() {

    private var knowledgeList: List<Knowledge> = listOf()

    fun submitList(knowledge: List<Knowledge>) {
        knowledgeList = knowledge
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KnowledgeViewHolder {
        val binding = ItemLayoutKnowledgeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KnowledgeViewHolder(binding, chooseCallback)
    }

    override fun onBindViewHolder(holder: KnowledgeViewHolder, position: Int) {
        holder.bind(knowledgeList[position])
    }

    override fun getItemCount(): Int = knowledgeList.size

    class KnowledgeViewHolder(private val binding: ItemLayoutKnowledgeBinding, private val chooseCallback : (String) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(knowledge: Knowledge) {
            binding.tvKnowledge.text = knowledge.title
            Glide.with(binding.root.context).load(knowledge.resourceId).skipMemoryCache(true).diskCacheStrategy(
                DiskCacheStrategy.NONE).centerCrop().into(binding.ivKnowledge)
            binding.root.setOnClickListener {
                chooseCallback.invoke(knowledge.link)
            }
        }
    }
}