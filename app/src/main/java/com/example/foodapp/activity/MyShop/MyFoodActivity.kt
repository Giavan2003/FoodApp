package com.example.foodapp.activity.MyShop

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.Interface.APIService
import com.example.foodapp.RetrofitClient
import com.example.foodapp.adapter.MyShopAdapter.MyShopAdapter
import com.example.foodapp.databinding.ActivityMyFoodBinding
import com.example.foodapp.dialog.LoadingDialog
import com.example.foodapp.model.Product
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyFoodActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyFoodBinding
    private val ds: MutableList<Product> = ArrayList()
    private lateinit var adapter: MyShopAdapter
    private var userId: String? = null
    private lateinit var apiService: APIService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")

        userId = intent.getStringExtra("userId")
        adapter = MyShopAdapter(ds, this, userId ?: "")
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

        apiService = RetrofitClient.retrofit!!.create(APIService::class.java)
        apiService.getProductsPublisherId(userId ?: "")?.enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                val lstProduct = response.body()
                if (response.isSuccessful && lstProduct != null) {
                    ds.clear()
                    ds.addAll(lstProduct)
                    dialog.dismiss()
                    adapter.notifyDataSetChanged()
                    Log.d("userid", userId ?: "")
                    Log.d("List food", ds.size.toString())
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                Log.e("MyFoodActivity", "API call failed", t)
            }
        })
    }
}