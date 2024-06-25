package com.example.ittalk.ui.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ittalk.databinding.BannerLayoutBinding
import com.example.ittalk.ui.learnCourse.LearnCourseActivity

class ViewPagerAdapter2(
    private val context: Context,
    private var imagesList: ArrayList<Int>
) : RecyclerView.Adapter<ViewPagerAdapter2.SliderViewHolder>() {

    inner class SliderViewHolder(val binding: BannerLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val binding = BannerLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return SliderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val imageResId = imagesList[position]
        holder.binding.banner1.setImageResource(imageResId)
        holder.binding.banner1.setOnClickListener {
            val intent = Intent(context, LearnCourseActivity::class.java)
            context.startActivity(intent)
            (context as Activity).overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)

        }
    }

    override fun getItemCount(): Int = imagesList.size
}
