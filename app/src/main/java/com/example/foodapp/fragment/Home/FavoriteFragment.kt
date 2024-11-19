package com.example.foodapp.fragment.Home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.foodapp.adapter.Home.FavouriteFoodAdapter
import com.example.foodapp.databinding.FragmentFavoriteBinding
import com.example.foodapp.helper.FirebaseFavouriteUserHelper
import com.example.foodapp.model.Product

class FavoriteFragment(private val userId: String) : Fragment() {

    private var binding: FragmentFavoriteBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        val view = binding?.root

        readFavouriteList()

        return view
    }

    private fun readFavouriteList() {
        FirebaseFavouriteUserHelper().readFavouriteList(userId, object : FirebaseFavouriteUserHelper.DataStatus {
            override fun DataIsLoaded(favouriteProducts: ArrayList<Product>, keys: ArrayList<String>) {
                val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                binding?.recFavouriteFood?.apply {
                    this.layoutManager = layoutManager
                    setHasFixedSize(true)
                    adapter = FavouriteFoodAdapter(context, favouriteProducts, userId)
                }
                binding?.progressBarFavouriteList?.visibility = View.GONE
            }

            override fun DataIsInserted() {

            }

            override fun DataIsUpdated() {

            }

            override fun DataIsDeleted() {

            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
