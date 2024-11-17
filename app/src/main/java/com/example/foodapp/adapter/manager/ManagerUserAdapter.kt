package com.example.foodapp.adapter.manager

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodapp.databinding.ItemManagerProductBinding
import com.example.foodapp.databinding.ItemManagerUserBinding

import com.example.foodapp.model.User
import com.google.firebase.database.FirebaseDatabase

class ManagerUserAdapter (
    private var ds: List<User>
) : RecyclerView.Adapter<ManagerUserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemManagerUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = ds[position]
        holder.binding.apply {
            txtUsername.text = "UserName: ${item.userName}"
            txtEmail.text = "Email: ${item.email}"
            txtPhone.text= "Phone numbere: ${item.phoneNumber}"
            Glide.with(root)
                .load(item.avatarURL)
                .into(avt)
            sw.isChecked = item.isActive
            parentOfItemInHome.setOnClickListener {
                // Xử lý sự kiện click vào item
            }

            sw.setOnCheckedChangeListener { _, isChecked ->
                val database = FirebaseDatabase.getInstance()
                val userRef = item.userId?.let { database.getReference("Users").child(it) }

                userRef?.child("active")?.setValue(isChecked)?.addOnSuccessListener {
                    Log.d("State", "success")
                    val message = if (isChecked) {
                        "Đã mở khóa tài khoản này"
                    } else {
                        "Đã khóa tài khoản này này"
                    }
                    Toast.makeText(root.context, message, Toast.LENGTH_LONG).show()
                }?.addOnFailureListener { exception ->
                    Log.e("State", "fail: ${exception.message}")
                }
            }
        }
    }

    override fun getItemCount(): Int = ds.size

    class ViewHolder(val binding: ItemManagerUserBinding) : RecyclerView.ViewHolder(binding.root)
}

