package com.example.foodapp.helper



import com.google.firebase.database.*
import com.example.foodapp.Interface.APIService
import com.example.foodapp.RetrofitClient
import com.example.foodapp.model.BillInfo
import com.example.foodapp.model.Product
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FirebaseOrderDetailHelper {

    private val mDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val mReferenceStatusOrder: DatabaseReference = mDatabase.reference
    private var apiService: APIService? = null
    private val billInfos = mutableListOf<BillInfo>()

    interface DataStatus {
        fun DataIsLoaded(address: String, billInfos: List<BillInfo>)
        fun DataIsInserted()
        fun DataIsUpdated()
        fun DataIsDeleted()
    }

    interface DataStatus2 {
        fun DataIsLoaded(product: Product)
        fun DataIsInserted()
        fun DataIsUpdated()
        fun DataIsDeleted()
    }

    fun readOrderDetail(addressId: String, userId: String, billId: String, dataStatus: DataStatus?) {
        mReferenceStatusOrder.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val addressDetail = snapshot.child("Address").child(userId).child(addressId).child("detailAddress")
                    .getValue(String::class.java)
                billInfos.clear()
                for (keyNode in snapshot.child("BillInfos").child(billId).children) {
                    billInfos.add(keyNode.getValue(BillInfo::class.java) ?: BillInfo())
                }
                dataStatus?.DataIsLoaded(addressDetail ?: "", billInfos)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun readProductInfo(productId: String, dataStatus: DataStatus2?) {
        mReferenceStatusOrder.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                apiService = RetrofitClient.retrofit!!.create(APIService::class.java)
                apiService?.getProductInfor(productId)?.enqueue(object : Callback<Product> {
                    override fun onResponse(call: Call<Product>, response: Response<Product>) {
                        response.body()?.let {
                            dataStatus?.DataIsLoaded(it)
                        }
                    }

                    override fun onFailure(call: Call<Product>, t: Throwable) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
