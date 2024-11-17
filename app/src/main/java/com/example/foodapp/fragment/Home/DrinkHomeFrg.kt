package com.example.foodapp.fragment.Home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.example.foodapp.databinding.FragmentDrinkHomeFrgBinding
import com.example.foodapp.model.Product
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DrinkHomeFrg(private val userId: String) : Fragment() {
    private lateinit var dsCurrentDrink: MutableList<Product?>

    private lateinit var totalDrink: List<Product>
    private lateinit var binding: FragmentDrinkHomeFrgBinding
    private lateinit var adapter: FoodDrinkFrgAdapter
    private var isLoading = false
    private val itemCount = 5
    private var isScrolling = true
    private var lastKey: String? = null
    private var position = 0
    private lateinit var apiService: APIService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDrinkHomeFrgBinding.inflate(inflater, container, false)
        val view = binding.root
        initData()
        initUI()
        return view
    }

    private fun initUI() {
        Log.d("UI", "done")
        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rycDrinkHome.layoutManager = linearLayoutManager
        adapter = FoodDrinkFrgAdapter(dsCurrentDrink, userId, requireContext())
        binding.rycDrinkHome.adapter = adapter
        binding.rycDrinkHome.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        binding.rycDrinkHome.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isLoading && isScrolling) {
                    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                    if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == dsCurrentDrink.size - 1) {
                        loadMore()
                        isLoading = true
                    }
                }
            }
        })
    }

    private fun initData() {
        Log.d("Data", "done")
        dsCurrentDrink = mutableListOf()
        apiService = RetrofitClient.retrofit!!.create(APIService::class.java)
        apiService.getProductsByType("drink").enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful) {
                    totalDrink = response.body() ?: emptyList()
                    var i = 0
                    while (position < totalDrink.size && i < itemCount) {
                        dsCurrentDrink.add(totalDrink[position])
                        position++
                        i++
                    }
                    adapter.notifyDataSetChanged()
                } else {

                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {

            }
        })
    }

    private fun loadMore() {
        if (position < totalDrink.size) {
            dsCurrentDrink.add(null)

            adapter.notifyItemInserted(dsCurrentDrink.size - 1)
            Handler(Looper.getMainLooper()).postDelayed({
                dsCurrentDrink.removeAt(dsCurrentDrink.size - 1)
                var i = 0
                while (position < totalDrink.size && i < itemCount) {
                    dsCurrentDrink.add(totalDrink[position])
                    position++
                    i++
                }
                adapter.notifyDataSetChanged()
                isLoading = false
            }, 1000)
        } else {
            isScrolling = false
            adapter.notifyDataSetChanged()
        }
    }

}

