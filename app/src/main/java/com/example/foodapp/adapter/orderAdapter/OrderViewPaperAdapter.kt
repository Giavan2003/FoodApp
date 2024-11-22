package com.example.foodapp.adapter.orderAdapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.foodapp.fragment.order.CurrentOrderFragment
import com.example.foodapp.fragment.order.HistoryOrderFragment
import com.example.foodapp.model.Bill


class OrderViewPaperAdapter(
    fragmentActivity: FragmentActivity,
    dsCurrentOrder: ArrayList<Bill>,
    dsHistoryOrder: ArrayList<Bill>,
    id: String
) :
    FragmentStateAdapter(fragmentActivity) {
    private val dsCurrentOrder: ArrayList<Bill>
    private val dsHistoryOrder: ArrayList<Bill>
    private val userId: String

    init {
        this.dsCurrentOrder = dsCurrentOrder
        this.dsHistoryOrder = dsHistoryOrder
        userId = id
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 1) {
            HistoryOrderFragment(dsHistoryOrder, userId)
        } else CurrentOrderFragment(dsCurrentOrder, userId)
    }

    override fun getItemCount(): Int {
        return 2
    }
}
