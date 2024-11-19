package com.example.foodapp.adapter.manager;

import android.content.Context;
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodapp.databinding.ItemManagerProductBinding

import com.example.foodapp.model.Product;
import com.google.firebase.database.FirebaseDatabase


class ManagerProductAdapter(
    private var ds: List<Product>,
    private val userId: String,

) : RecyclerView.Adapter<ManagerProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemManagerProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = ds[position]
        holder.binding.apply {
            txtFoodName.text = item.productName
            txtUserId.text = "UserId: ${item.publisherId}"
            Glide.with(root)
                .load(item.productImage1)
                .into(imgFood)
            sw.isChecked = item.isChecked

            parentOfItemInHome.setOnClickListener {
                // Xử lý sự kiện click vào item
            }

            sw.setOnCheckedChangeListener { _, isChecked ->
                val database = FirebaseDatabase.getInstance()
                val productRef = item.productId?.let { database.getReference("Products").child(it) }

                productRef?.child("checked")?.setValue(isChecked)?.addOnSuccessListener {
                    Log.d("State", "success")
                    val message = if (isChecked) {
                        "Đã kích hoạt sản phẩm này"
                    } else {
                        "Đã tắt sản phẩm này"
                    }
                    Toast.makeText(root.context, message, Toast.LENGTH_LONG).show()
                }?.addOnFailureListener { exception ->
                    Log.e("State", "fail: ${exception.message}")
                }
            }
        }
    }

    override fun getItemCount(): Int = ds.size

    class ViewHolder(val binding: ItemManagerProductBinding) : RecyclerView.ViewHolder(binding.root)
}

