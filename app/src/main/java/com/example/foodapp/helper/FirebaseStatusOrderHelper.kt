package com.example.foodapp.helper



import com.example.foodapp.model.Bill
import com.example.foodapp.model.BillInfo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class FirebaseStatusOrderHelper(private val userId: String? = null) {
    private val mDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val mReferenceStatusOrder: DatabaseReference = mDatabase.reference
    private val bills = mutableListOf<Bill>()
    private val billInfoList = mutableListOf<BillInfo>()
    private val soldValueList = mutableListOf<Int>()


    interface DataStatus {
        fun dataIsLoaded(bills: List<Bill>, isExistingBill: Boolean)
        fun dataIsInserted()
        fun dataIsUpdated()
        fun dataIsDeleted()
    }

    fun readConfirmBills(userId: String, dataStatus: DataStatus?) {
        // Đọc và lấy các hoá đơn có trạng thái "Confirm" của một user
        mReferenceStatusOrder.addListenerForSingleValueEvent(object : ValueEventListener {
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
        mReferenceStatusOrder.addListenerForSingleValueEvent(object : ValueEventListener {
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
        mReferenceStatusOrder.addListenerForSingleValueEvent(object : ValueEventListener {
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

    fun readSomeInfoOfBill() {
        mReferenceStatusOrder.child("Products").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (info in billInfoList) {
                    val sold = snapshot.child(info.productId!!).child("sold").getValue(Int::class.java) ?: 0
                    val newSoldValue = sold + info.amount
                    soldValueList.add(newSoldValue)
                }
                updateSoldValueOfProduct()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    fun updateSoldValueOfProduct() {
        for (i in billInfoList.indices) {
            mReferenceStatusOrder.child("Products").child(billInfoList[i].productId!!).child("sold")
                .setValue(
                    soldValueList[i]
                )
        }
    }
}
