package com.example.foodapp.activity.MyShop

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.adapter.MyShopAdapter.MyFoodAdapter
import com.example.foodapp.databinding.ActivityMyFoodBinding
import com.example.foodapp.dialog.LoadingDialog
import com.example.foodapp.model.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MyFoodActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyFoodBinding
    private val ds: MutableList<Product> = ArrayList()
    private lateinit var adapter: MyFoodAdapter
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")

        //userId = intent.getStringExtra("userId")
        userId = "sWuMLC04RPbSx4mzR0faHjhpwVP2"
        adapter = MyFoodAdapter(ds, this, userId)
        binding.recycleView.setHasFixedSize(true)
        binding.recycleView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recycleView.adapter = adapter
        binding.imgBack.setOnClickListener {
            finish()
        }
        binding.flpAddFood.setOnClickListener {
            val intent = Intent(this, AddFoodActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = LoadingDialog(this)
        dialog.show()
        FirebaseDatabase.getInstance().getReference("Products")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    ds.clear()
                    var i = 0
                    for (item in snapshot.children) {
                        Log.e("Thông báo", i++.toString())
                        val tmp = item.getValue(Product::class.java)
                        if (tmp != null && tmp.publisherId != null) {
                            if (tmp.publisherId == userId && tmp.state != "deleted") {
                                ds.add(tmp)
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                    dialog.dismiss()
                }
                override fun onCancelled(error: DatabaseError) {

                    Log.e("Lỗi Firebase", error.message)
                    dialog.dismiss()  // Đảm bảo đóng dialog khi có lỗi
                }
            })
    }
}