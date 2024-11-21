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


class ShippingStatusDeliveryFragment(private val ds: ArrayList<Bill>) : Fragment() {
    private lateinit var binding: FragmentShippingStatusDeliveryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShippingStatusDeliveryBinding.inflate(inflater, container, false)
        val adapter = StatusOrderRecyclerViewAdapter(requireContext(), ds)
        binding.recShippingDelivery.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
        binding.progressBarShippingDelivery.visibility = View.GONE
        binding.txtNoneItem.visibility = View.GONE
        return binding.root
    }

}

