package com.example.foodapp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.foodapp.fragment.Home.DrinkHomeFrg
import com.example.foodapp.fragment.Home.FoodHomeFrg


class ViewPager2Adapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val userId: String
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    val currentFragment: Fragment? = null

    override fun createFragment(position: Int): Fragment {
        return if (position == 1) {
            DrinkHomeFrg(userId)
        } else FoodHomeFrg(userId)
    }

    override fun getItemCount(): Int {
        return 2
    }
}
