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
import com.example.foodapp.adapter.Home.FoodDrinkFrgAdapter
import com.example.foodapp.databinding.FragmentDrinkHomeFrgBinding
import com.example.foodapp.model.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


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
        FirebaseDatabase.getInstance().getReference("Products")
            .orderByChild("productType")
            .equalTo("Drink")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    totalDrink = mutableListOf()
                    for (item in snapshot.children) {
                        val product = item.getValue(Product::class.java)
                        if (product != null) {
                            if (product.isChecked && product.remainAmount > 0) {
                                totalDrink +=  product
                            }
                        }
                    }

                    var i = 0
                    while (position < totalDrink.size && i < itemCount) {
                        dsCurrentDrink.add(totalDrink[position])
                        position++
                        i++
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error fetching data", error.toException())
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
