package com.example.foodapp.activity.orderSellerManagement

package com.uteating.foodapp.activity.orderSellerManagement

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodapp.adapter.DeliveryManagement_Seller.ListOfItemInOrderAdapter
import com.example.foodapp.databinding.ActivityDetailOfOrderDeliveryManagementBinding
import com.example.foodapp.helper.FirebaseOrderDetailHelper
import com.example.foodapp.model.BillInfo

class DetailOfOrderDeliveryManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailOfOrderDeliveryManagementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailOfOrderDeliveryManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#E8584D")
        window.navigationBarColor = Color.parseColor("#E8584D")

        val intent = intent
        intent?.let {
            val billId = it.getStringExtra("billId")
            val addressId = it.getStringExtra("addressId")
            val recipientId = it.getStringExtra("recipientId")
            val orderStatus = it.getStringExtra("orderStatus")
            val price = it.getLongExtra("totalBill", -1)

            try {
                binding.txtOrderIdDetail.text = "Order Id: $billId"
                binding.txtBillTotalInDetail.text = "${convertToMoney(price)}đ"
                binding.txtStatusOrderDetail.text = orderStatus

                if (orderStatus == "Completed") {
                    binding.txtStatusOrderDetail.setTextColor(Color.parseColor("#48DC7D"))
                }

                FirebaseOrderDetailHelper().readOrderDetail(addressId, recipientId, billId, object : FirebaseOrderDetailHelper.DataStatus {
                    override fun DataIsLoaded(address: String, billInfos: List<BillInfo>) {
                        binding.txtAddressDetail.text = address
                        val adapter = ListOfItemInOrderAdapter(this@DetailOfOrderDeliveryManagementActivity, billInfos)
                        binding.recOrderDetail.layoutManager = LinearLayoutManager(this@DetailOfOrderDeliveryManagementActivity)
                        binding.recOrderDetail.setHasFixedSize(true)
                        binding.recOrderDetail.adapter = adapter
                    }

                    override fun DataIsInserted() {}
                    override fun DataIsUpdated() {}
                    override fun DataIsDeleted() {}
                })
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun convertToMoney(price: Long): String {
        val temp = price.toString()
        var output = ""
        var count = 3
        for (i in temp.length - 1 downTo 0) {
            count--
            if (count == 0) {
                count = 3
                output = ",${temp[i]}$output"
            } else {
                output = "${temp[i]}$output"
            }
        }

        return if (output.startsWith(",")) output.substring(1) else output
    }
}
