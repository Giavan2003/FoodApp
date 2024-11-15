package com.example.foodapp.activity.manager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodapp.adapter.manager.ManagerProductAdapter
import com.example.foodapp.adapter.manager.ManagerUserAdapter
import com.example.foodapp.databinding.ActivityManagerBinding
import com.example.foodapp.databinding.ActivityManagerUserBinding
import com.example.foodapp.model.Product
import com.example.foodapp.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManagerUserActivity : AppCompatActivity() {
    private lateinit var adapter: ManagerUserAdapter
    private lateinit var binding: ActivityManagerUserBinding
    private val ds: MutableList<User> = ArrayList()
    private lateinit var userId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagerUserBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        initData()
        initUI()
    }
    private fun initUI() {
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rc.layoutManager = linearLayoutManager
        adapter = ManagerUserAdapter(ds)
        binding.rc.adapter = adapter
        binding.rc.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.imgBack.setOnClickListener {
            finish()
        }
    }
    private fun initData() {
        val intent = intent
//        userId = intent.getStringExtra("userId").toString()
        userId = "sWuMLC04RPbSx4mzR0faHjhpwVP2"
        FirebaseDatabase.getInstance().getReference("Users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    ds.clear()
                    var i = 0
                    for (item in snapshot.children) {
                        val tmp = item.getValue(User::class.java)
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