package com.example.ittalk.ui.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.ittalk.databinding.LearnCodingLayoutBinding

class QuestionAdapter() : RecyclerView.Adapter<QuestionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LearnCodingLayoutBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return 10
    }

    inner class ViewHolder(private val binding: LearnCodingLayoutBinding) : RecyclerView.ViewHolder(binding.root)
    {

        fun bind()
        {

            binding.ivArrow.setOnClickListener {
                if (binding.desc.isVisible)
                {
                    binding.desc.isVisible  = false
                }
                else{
                    binding.desc.isVisible  = true

                }
            }


        }
    }
}
