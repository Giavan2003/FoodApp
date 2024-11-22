package com.example.foodapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.R
import com.example.foodapp.databinding.ItemImagesBinding
import com.example.foodapp.model.Images


class ImagesViewPageAdapter(private val imagesList: List<Images>?) : RecyclerView.Adapter<ImagesViewPageAdapter.ImagesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemImagesBinding = ItemImagesBinding.inflate(inflater, parent, false)
        val view: View = binding.root
        return ImagesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
        val image = imagesList?.get(position)
        image?.let {
            holder.getImageView().setImageResource(image.imagesId)
        }
    }

    override fun getItemCount(): Int {
        return imagesList?.size ?: 0
    }

    inner class ImagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imgView)

        // Getter cho imageView
        fun getImageView(): ImageView {
            return imageView
        }
    }
}

