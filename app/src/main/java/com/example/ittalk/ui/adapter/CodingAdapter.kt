package com.example.ittalk.ui.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ittalk.databinding.ProgrammingLayoutBinding
import com.example.ittalk.ui.activeCall.ActiveCallActivity
import com.example.ittalk.ui.learnCourse.LearnCourseActivity

class CodingAdapter(var context: Context, val data: List<Int>) :
    RecyclerView.Adapter<CodingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ProgrammingLayoutBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(private val binding: ProgrammingLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Int) {
            when (position)
            {
                0 -> { binding.mAndroid.text = "Android" }
                1 -> { binding.mAndroid.text = "IOS" }
                2 -> { binding.mAndroid.text = "WEBSITE" }
            }
            binding.imageView.setImageResource(item)
            binding.imageView.setOnClickListener {
                val intent = Intent(context, ActiveCallActivity::class.java)
                (context as Activity).overridePendingTransition(
                    androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                when(position)
                {
                    0 -> {  intent.putExtra("topic","android")
                        context.startActivity(intent) }
                    1 -> {  intent.putExtra("topic","ios")
                        context.startActivity(intent)
                    }
                    2 -> {  intent.putExtra("topic","website")
                        context.startActivity(intent)
                    }
                }


            }
        }


    }
}
