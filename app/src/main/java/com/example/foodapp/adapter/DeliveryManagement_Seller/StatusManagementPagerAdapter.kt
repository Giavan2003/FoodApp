package com.example.foodapp.adapter.DeliveryManagement_Seller

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.foodapp.fragment.DeliveryManagement_Seller.CompletedStatusDeliveryFragment
import com.example.foodapp.fragment.DeliveryManagement_Seller.ConfirmStatusDeliveryFragment
import com.example.foodapp.fragment.DeliveryManagement_Seller.ShippingStatusDeliveryFragment

class StatusManagementPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val userId: String
) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> ShippingStatusDeliveryFragment(userId)
            2 -> CompletedStatusDeliveryFragment(userId)
            else -> ConfirmStatusDeliveryFragment(userId)
        }
    }

    override fun getItemCount(): Int = 3
}
