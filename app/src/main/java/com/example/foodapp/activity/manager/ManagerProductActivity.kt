package com.example.foodapp.activity.manager

import android.os.Bundle
import android.util.Log

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager


import com.example.foodapp.adapter.manager.ManagerProductAdapter

import com.example.foodapp.databinding.ActivityManagerProductBinding
import com.example.foodapp.model.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ManagerProductActivity : AppCompatActivity() {
    private lateinit var adapter: ManagerProductAdapter
    private lateinit var binding: ActivityManagerProductBinding
    private val ds: MutableList<Product> = ArrayList()
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagerProductBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        initData()
        initUI()
    }
    private fun initUI() {
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rc.layoutManager = linearLayoutManager
        adapter = ManagerProductAdapter(ds, userId)
        binding.rc.adapter = adapter
        binding.rc.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.imgBack.setOnClickListener {
            finish()
        }
    }
    private fun initData() {
        val intent = intent
        userId = intent.getStringExtra("userId").toString()
        FirebaseDatabase.getInstance().getReference("Products")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    ds.clear()
                    var i = 0
                    for (item in snapshot.children) {
                        val tmp = item.getValue(Product::class.java)
                        if (tmp != null) {
                            ds.add(tmp)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("Lỗi Firebase", error.message)
                }
            })
    }
}