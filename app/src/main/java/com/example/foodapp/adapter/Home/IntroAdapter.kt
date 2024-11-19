package com.example.foodapp.adapter.Home

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.databinding.ItemIntroBinding



class IntroAdapter(private val ds: ArrayList<Int>, private val context: Activity) : RecyclerView.Adapter<IntroAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIntroBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.img.setImageResource(ds[position])
    }

    override fun getItemCount(): Int {
        return ds.size
    }

    class ViewHolder(val binding: ItemIntroBinding) : RecyclerView.ViewHolder(binding.root)
}

