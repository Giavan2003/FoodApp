package com.example.foodapp.fragment.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.activity.order.OrderActivity
import com.example.foodapp.adapter.orderAdapter.OrderAdapter
import com.example.foodapp.databinding.FragmentCurrentOrderBinding
import com.example.foodapp.model.Bill


class CurrentOrderFragment(private val ds: ArrayList<Bill>, private val id: String) : Fragment() {
    private lateinit var binding: FragmentCurrentOrderBinding
    private val dsBill: ArrayList<Bill> = ds
    private val userId: String = id

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCurrentOrderBinding.inflate(inflater, container, false)


        val adapter = OrderAdapter(requireContext(), dsBill, OrderActivity.CURRENT_ORDER, userId)
        binding.ryc.adapter = adapter
        binding.ryc.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        return binding.root
    }
}