package com.example.foodapp.fragment.DeliveryManagement_Seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodapp.adapter.DeliveryManagement_Seller.StatusOrderRecyclerViewAdapter
import com.example.foodapp.databinding.FragmentShippingStatusDeliveryBinding
import com.example.foodapp.helper.FirebaseStatusOrderHelper
import com.example.foodapp.model.Bill
import java.util.Collections


class ShippingStatusDeliveryFragment(private val userId: String) : Fragment() {
    private var binding: FragmentShippingStatusDeliveryBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentShippingStatusDeliveryBinding.inflate(inflater, container, false)
        val view: View = binding!!.root

        // Set adapter and pull data for recycler view
        FirebaseStatusOrderHelper(userId).readShippingBills(
            userId,
            object : FirebaseStatusOrderHelper.DataStatus {
                override fun dataIsLoaded(bills: List<Bill>, isExistingBill: Boolean) {
                    Collections.reverse(bills)
                    val adapter = StatusOrderRecyclerViewAdapter(requireContext(), bills)
                    binding!!.recShippingDelivery.layoutManager = LinearLayoutManager(context)
                    binding!!.recShippingDelivery.setHasFixedSize(true)
                    binding!!.recShippingDelivery.adapter = adapter
                    binding!!.progressBarShippingDelivery.visibility = View.GONE
                    binding!!.txtNoneItem.visibility = if (isExistingBill) View.GONE else View.VISIBLE
                }

                override fun dataIsInserted() {}
                override fun dataIsUpdated() {}
                override fun dataIsDeleted() {}
            })
        return view
    }
}

