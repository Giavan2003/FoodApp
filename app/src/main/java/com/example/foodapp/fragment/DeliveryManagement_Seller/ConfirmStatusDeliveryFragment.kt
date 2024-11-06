package com.example.foodapp.fragment.DeliveryManagement_Seller



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodapp.adapter.DeliveryManagement_Seller.StatusOrderRecyclerViewAdapter
import com.example.foodapp.databinding.FragmentConfirmStatusDeliveryBinding
import com.example.foodapp.helper.FirebaseStatusOrderHelper
import com.example.foodapp.model.Bill

class ConfirmStatusDeliveryFragment(private val userId: String) : Fragment() {

    private lateinit var binding: FragmentConfirmStatusDeliveryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentConfirmStatusDeliveryBinding.inflate(inflater, container, false)

        // Set data and adapter for list
        FirebaseStatusOrderHelper(userId).readConfirmBills(userId, object : FirebaseStatusOrderHelper.DataStatus {
            override fun dataIsLoaded(bills: List<Bill>, isExistingBill: Boolean) {
                val reversedBills = bills.reversed()
                val adapter = StatusOrderRecyclerViewAdapter(requireContext(), reversedBills)
                binding.recConfirmDelivery.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(context)
                    this.adapter = adapter
                }
                binding.progressBarConfirmDelivery.visibility = View.GONE
                binding.txtNoneItem.visibility = if (isExistingBill) View.GONE else View.VISIBLE
            }

            override fun dataIsInserted() {}

            override fun dataIsUpdated() {}

            override fun dataIsDeleted() {}
        })

        return binding.root
    }
}
