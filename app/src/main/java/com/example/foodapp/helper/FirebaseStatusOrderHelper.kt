package com.example.foodapp.helper


import android.util.Log
import com.google.firebase.database.*
import com.example.foodapp.Interface.APIService
import com.example.foodapp.RetrofitClient
import com.example.foodapp.model.Bill
import com.example.foodapp.model.BillInfo
import com.example.foodapp.model.Product
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FirebaseStatusOrderHelper(private val userId: String? = null) {
    private val mDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val mReferenceStatusOrder: DatabaseReference = mDatabase.reference
    private val bills = mutableListOf<Bill>()
    private val billInfoList = mutableListOf<BillInfo>()
    private val soldValueList = mutableListOf<Int>()
    private var apiService: APIService = RetrofitClient.retrofit!!.create(APIService::class.java)

    interface DataStatus {
        fun dataIsLoaded(bills: List<Bill>, isExistingBill: Boolean)
        fun dataIsInserted()
        fun dataIsUpdated()
        fun dataIsDeleted()
    }

    fun readConfirmBills(userId: String, dataStatus: DataStatus?) {
        // Đọc và lấy các hoá đơn có trạng thái "Confirm" của một user
        mReferenceStatusOrder.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bills.clear()
                var isExistingBill = false
                for (keyNode in snapshot.child("Bills").children) {
                    if (keyNode.child("senderId").getValue(String::class.java) == userId &&
                        keyNode.child("orderStatus").getValue(String::class.java) == "Confirm") {
                        val bill = keyNode.getValue(Bill::class.java)
                        bill?.let {
                            bills.add(it)
                            isExistingBill = true
                        }
                    }
                }
                dataStatus?.dataIsLoaded(bills, isExistingBill)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun readShippingBills(userId: String, dataStatus: DataStatus?) {
        // Đọc và lấy các hoá đơn có trạng thái "Shipping" của một user
        mReferenceStatusOrder.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bills.clear()
                var isExistingShippingBill = false
                for (keyNode in snapshot.child("Bills").children) {
                    if (keyNode.child("senderId").getValue(String::class.java) == userId &&
                        keyNode.child("orderStatus").getValue(String::class.java) == "Shipping") {
                        val bill = keyNode.getValue(Bill::class.java)
                        bill?.let {
                            bills.add(it)
                            isExistingShippingBill = true
                        }
                    }
                }
                dataStatus?.dataIsLoaded(bills, isExistingShippingBill)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun readCompletedBills(userId: String, dataStatus: DataStatus?) {
        // Đọc và lấy các hoá đơn có trạng thái "Completed" của một user
        mReferenceStatusOrder.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bills.clear()
                var isExistingBill = false
                for (keyNode in snapshot.child("Bills").children) {
                    if (keyNode.child("senderId").getValue(String::class.java) == userId &&
                        keyNode.child("orderStatus").getValue(String::class.java) == "Completed") {
                        val bill = keyNode.getValue(Bill::class.java)
                        bill?.let {
                            bills.add(it)
                            isExistingBill = true
                        }
                    }
                }
                dataStatus?.dataIsLoaded(bills, isExistingBill)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun setConfirmToShipping(billId: String, dataStatus: DataStatus?) {
        // Cập nhật trạng thái của một hoá đơn từ "Confirm" sang "Shipping"
        mReferenceStatusOrder.child("Bills").child(billId).child("orderStatus").setValue("Shipping")
            .addOnSuccessListener {
                dataStatus?.dataIsUpdated()
            }

        // set sold and remainAmount value of Product
        billInfoList.clear()
        soldValueList.clear()

        mReferenceStatusOrder.child("BillInfos").child(billId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (keyNode in snapshot.children) {
                    val billInfo = keyNode.getValue(BillInfo::class.java)
                    billInfo?.let { billInfoList.add(it) }
                }
                readSomeInfoOfBill()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun setShippingToCompleted(billId: String, dataStatus: DataStatus?) {
        // Cập nhật trạng thái của một hoá đơn từ "Shipping" sang "Completed"
        mReferenceStatusOrder.child("Bills").child(billId).child("orderStatus").setValue("Completed")
            .addOnSuccessListener {
                dataStatus?.dataIsUpdated()
            }
    }

    private fun readSomeInfoOfBill() {
        // Đọc thông tin về số lượng bán và cập nhật các giá trị liên quan cho các sản phẩm trong hoá đơn.
        for (info in billInfoList) {
            info.productId?.let {
                apiService.getProductInfor(it).enqueue(object : Callback<Product> {
                    override fun onResponse(call: Call<Product>, response: Response<Product>) {
                        if (response.isSuccessful && response.body() != null) {
                            val pro = response.body()!!
                            if (info.productId == pro.productId) {
                                val sold = info.amount + pro.sold
                                val amount = pro.remainAmount
                                soldValueList.add(sold)
                                pro.sold = sold
                                pro.remainAmount = amount - sold
                                updateSoldValueOfProduct(pro)
                            }
                        }
                    }

                    override fun onFailure(call: Call<Product>, t: Throwable) {}
                })
            }
        }
    }

    private fun updateSoldValueOfProduct(pro: Product) {
        apiService.updateProduct(pro).enqueue(object : Callback<Product> {
            override fun onResponse(call: Call<Product>, response: Response<Product>) {
                if (response.isSuccessful) {
                    Log.d("FirebaseStatusOrderHelper", "Product updated successfully.")
                }
            }

            override fun onFailure(call: Call<Product>, t: Throwable) {
                Log.e("FirebaseStatusOrderHelper", "Failed to update product: ${t.message}")
            }
        })
    }
}
