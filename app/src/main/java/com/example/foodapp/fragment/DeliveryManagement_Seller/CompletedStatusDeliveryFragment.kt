package com.example.foodapp.fragment.DeliveryManagement_Seller



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodapp.adapter.DeliveryManagement_Seller.StatusOrderRecyclerViewAdapter
import com.example.foodapp.databinding.FragmentCompletedStatusDeliveryBinding
import com.example.foodapp.helper.FirebaseStatusOrderHelper
import com.example.foodapp.model.Bill
import java.util.*

class CompletedStatusDeliveryFragment(private val userId: String) : Fragment() {

    private lateinit var binding: FragmentCompletedStatusDeliveryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCompletedStatusDeliveryBinding.inflate(inflater, container, false)

        // Pull data and set adapter for recycler view
        FirebaseStatusOrderHelper(userId).readCompletedBills(userId, object : FirebaseStatusOrderHelper.DataStatus {
            override fun dataIsLoaded(bills: List<Bill>, isExistingBill: Boolean) {
                val reversedBills = bills.reversed()
                var adapter = StatusOrderRecyclerViewAdapter(requireContext(), reversedBills)
                binding.recCompletedDelivery.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(context)
                    this.adapter = adapter
                }
                binding.progressBarCompletedDelivery.visibility = View.GONE
                binding.txtNoneItem.visibility = if (isExistingBill) View.GONE else View.VISIBLE
            }

            override fun dataIsInserted() {}

            override fun dataIsUpdated() {}

            override fun dataIsDeleted() {}
        })

        return binding.root
    }
}
