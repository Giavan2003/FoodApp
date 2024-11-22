package com.example.foodapp.adapter.DeliveryManagement_Seller

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.foodapp.fragment.DeliveryManagement_Seller.CompletedStatusDeliveryFragment
import com.example.foodapp.fragment.DeliveryManagement_Seller.ConfirmStatusDeliveryFragment
import com.example.foodapp.fragment.DeliveryManagement_Seller.ShippingStatusDeliveryFragment
import com.example.foodapp.model.Bill

class StatusManagementPagerAdapter(
    fragmentActivity: FragmentActivity,
    dsShipping: ArrayList<Bill>,
    dsComplete: ArrayList<Bill>,
    dsConfirm: ArrayList<Bill>,
    id: String
) : FragmentStateAdapter(fragmentActivity) {
    private val dsShipping: ArrayList<Bill>
    private val dsComplete: ArrayList<Bill>
    private val dsConfirm: ArrayList<Bill>
    private val userId: String
    init {
        this.dsShipping = dsShipping
        this.dsComplete = dsComplete
        this.dsConfirm = dsConfirm
        userId = id
    }
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> ShippingStatusDeliveryFragment(dsShipping)
            2 -> CompletedStatusDeliveryFragment(dsComplete)
            else -> ConfirmStatusDeliveryFragment(dsConfirm)
        }
    }

    override fun getItemCount(): Int = 3


}


