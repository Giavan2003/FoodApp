package com.example.foodapp.fragment.Home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.Interface.APIService
import com.example.foodapp.RetrofitClient
import com.example.foodapp.adapter.Home.FoodDrinkFrgAdapter
import com.example.foodapp.databinding.FragmentFoodHomeFrgBinding
import com.example.foodapp.model.Product

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FoodHomeFrg(private val userId: String = "") : Fragment() {
    private lateinit var binding: FragmentFoodHomeFrgBinding
    private lateinit var dsCurrentFood: MutableList<Product?>
    private lateinit var totalFood: List<Product>
    private lateinit var adapter: FoodDrinkFrgAdapter
    private var isLoading = false
    private val itemCount = 5
    private var isScrolling = true
    private var lastKey: String? = null
    private lateinit var apiService: APIService
    private var position = 0

    // Mặc định khi không có userId
    constructor() : this("")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFoodHomeFrgBinding.inflate(inflater, container, false)
        val view = binding.root
        initData()
        initUI()
        return view
    }

    private fun initUI() {
        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rycFoodHome.layoutManager = linearLayoutManager
        adapter = FoodDrinkFrgAdapter(dsCurrentFood, userId, requireContext())
        binding.rycFoodHome.adapter = adapter
        binding.rycFoodHome.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.rycFoodHome.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isLoading && isScrolling) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                    if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == dsCurrentFood.size - 1) {
                        loadMore()
                        isLoading = true
                    }
                }
            }
        })
    }

    private fun initData() {
        dsCurrentFood = mutableListOf()
        val apiService: APIService = RetrofitClient.retrofit!!.create(APIService::class.java)
        apiService.getProductsByType("food").enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful) {
                    totalFood = response.body() ?: emptyList()
                    var i = 0
                    while (position < totalFood.size && i < itemCount) {
                        dsCurrentFood.add(totalFood[position])
                        position++
                        i++
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    // Handle failure case if needed
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                // Handle failure case if needed
            }
        })
    }

    private fun loadMore() {
        if (position < totalFood.size) {
            dsCurrentFood.add(null)
            adapter.notifyItemInserted(dsCurrentFood.size - 1)
            Handler(Looper.getMainLooper()).postDelayed({
                dsCurrentFood.removeAt(dsCurrentFood.size - 1)
                var i = 0
                while (position < totalFood.size && i < itemCount) {
                    dsCurrentFood.add(totalFood[position])
                    position++
                    i++
                }
                adapter.notifyDataSetChanged()
                isLoading = false
            }, 1500)
        } else {
            isScrolling = false
            adapter.notifyDataSetChanged()
        }
    }
}

