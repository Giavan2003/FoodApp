package com.example.foodapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodapp.databinding.ItemProductInfoBinding

class ProductInfoImageAdapter(
    private val context: Context,
    images: ArrayList<String>
) : RecyclerView.Adapter<ProductInfoImageAdapter.ViewHolder>() {

    private val filteredImages: List<String> = images.filter { it.isNotEmpty() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductInfoBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context)
            .load(filteredImages[position])
            .into(holder.binding.imgFood)
    }

    override fun getItemCount(): Int {
        return filteredImages.size
    }

    class ViewHolder(val binding: ItemProductInfoBinding) : RecyclerView.ViewHolder(binding.root)
}
