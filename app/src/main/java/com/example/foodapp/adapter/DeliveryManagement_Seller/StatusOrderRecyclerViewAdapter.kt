package com.example.foodapp.adapter.DeliveryManagement_Seller



import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.example.foodapp.Interface.APIService
import com.example.foodapp.R;
import com.example.foodapp.RetrofitClient
import com.example.foodapp.activity.orderSellerManagement.DetailOfOrderDeliveryManagementActivity
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.ItemOrderStatusListBinding
import com.example.foodapp.helper.FirebaseNotificationHelper
import com.example.foodapp.helper.FirebaseStatusOrderHelper
import com.example.foodapp.model.Bill
import com.example.foodapp.model.BillInfo
import com.example.foodapp.model.Notification
import com.example.foodapp.model.Product
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class StatusOrderRecyclerViewAdapter(
    private val mContext: Context,
    private val billList: List<Bill>
) : RecyclerView.Adapter<StatusOrderRecyclerViewAdapter.ViewHolder>() {

    private var apiService: APIService? = null
    private var remainAmount: Int = 0
    private var sold: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderStatusListBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bill = billList[position]
        holder.binding.txtOrderId.text = bill.billId
        holder.binding.txtStatus.text = bill.orderStatus
        holder.binding.txtDateOfOrder.text = bill.orderDate
        holder.binding.txtOrderTotal.text = "${convertToMoney(bill.totalPrice)}đ"

        holder.binding.imgProductImage.scaleType = ImageView.ScaleType.CENTER_CROP
        Glide.with(mContext)
            .asBitmap()
            .load(bill.imageUrl)
            .into(holder.binding.imgProductImage)

        if (bill.orderStatus == "Confirm") {
            holder.binding.btnChangeStatus.text = "Confirm"
            holder.binding.btnChangeStatus.setOnClickListener {
                bill.billId?.let { it1 ->
                    checkRemainAmount(it1) { canChangeStatus ->
                        if (canChangeStatus) {
                            bill.billId?.let { it1 ->
                                FirebaseStatusOrderHelper().setConfirmToShipping(it1, object : FirebaseStatusOrderHelper.DataStatus {
                                    override fun dataIsLoaded(bills: List<Bill>, isExistingBill: Boolean) {}
                                    override fun dataIsInserted() {}
                                    override fun dataIsUpdated() {
                                        SuccessfulToast(mContext, "Order has been changed to shipping state!").showToast()
                                        bill.recipientId?.let { it2 ->
                                            bill.imageUrl?.let { it3 ->
                                                pushNotificationOrderStatusForReceiver(bill.billId!!, " đang giao hàng",
                                                    it2,
                                                    it3
                                                )
                                            }
                                        }
                                    }
                                    override fun dataIsDeleted() {}
                                })
                            }
                        } else {
                            holder.binding.btnChangeStatus.isEnabled = false
                            holder.binding.btnChangeStatus.setBackgroundResource(R.drawable.background_feedback_disabled_button)
                        }
                    }
                }
            }
        } else if (bill.orderStatus == "Shipping") {
            holder.binding.btnChangeStatus.text = "Completed"
            holder.binding.btnChangeStatus.setOnClickListener {
                bill.billId?.let { it1 ->
                    FirebaseStatusOrderHelper().setShippingToCompleted(it1, object : FirebaseStatusOrderHelper.DataStatus {
                        override fun dataIsLoaded(bills: List<Bill>, isExistingBill: Boolean) {}
                        override fun dataIsInserted() {}
                        override fun dataIsUpdated() {
                            SuccessfulToast(mContext, "Order has been changed to completed state!").showToast()
                            bill.recipientId?.let { it2 ->
                                bill.imageUrl?.let { it3 ->
                                    pushNotificationOrderStatusForReceiver(bill.billId!!, " giao hàng thành công",
                                        it2, it3
                                    )
                                }
                            }
                        }
                        override fun dataIsDeleted() {}
                    })
                }
            }
        } else {
            holder.binding.txtStatus.setTextColor(Color.parseColor("#48DC7D"))
            holder.binding.btnChangeStatus.visibility = View.GONE
        }

        // view detail
        holder.binding.parentOfItemCard.setOnClickListener {
            val intent = Intent(mContext, DetailOfOrderDeliveryManagementActivity::class.java)
            intent.putExtra("billId", bill.billId)
            intent.putExtra("addressId", bill.addressId)
            intent.putExtra("recipientId", bill.recipientId)
            intent.putExtra("totalBill", bill.totalPrice)
            intent.putExtra("orderStatus", bill.orderStatus)
            mContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = billList.size

    class ViewHolder(@NonNull val binding: ItemOrderStatusListBinding) : RecyclerView.ViewHolder(binding.root)

    private fun convertToMoney(price: Long): String {
        val temp = price.toString()
        var output = ""
        var count = 3
        for (i in temp.length - 1 downTo 0) {
            count--
            if (count == 0) {
                count = 3
                output = "," + temp[i] + output
            } else {
                output = temp[i] + output
            }
        }
        return if (output[0] == ',') output.substring(1) else output
    }

    private fun pushNotificationOrderStatusForReceiver(billId: String, status: String, receiverId: String, productImage1: String) {
        val title = "Order status"
        val content = "Order $billId has been updated to $status, go to My Order to check it."
        val notification = FirebaseNotificationHelper.createNotification(
            title, content, productImage1, "None", billId, "None", null
        )
        FirebaseNotificationHelper(mContext).addNotification(receiverId, notification, object : FirebaseNotificationHelper.DataStatus {
            override fun DataIsLoaded(notificationList: List<Notification>, notificationListToNotify: List<Notification>) {}
            override fun DataIsInserted() {}
            override fun DataIsUpdated() {}
            override fun DataIsDeleted() {}
        })
    }

    private fun checkRemainAmount(billId: String, callback: (Boolean) -> Unit) {
        val billInfoRef = FirebaseDatabase.getInstance().getReference("BillInfos").child(billId)
        billInfoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    val billInfoList = mutableListOf<BillInfo>()
                    snapshot.children.forEach { billInfoSnapshot ->
                        val billInfo = billInfoSnapshot.getValue(BillInfo::class.java)
                        billInfo?.let { billInfoList.add(it) }
                    }
                    checkRemainAmountForAllProducts(billInfoList, callback)
                } else {
                    callback(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }

    private fun checkRemainAmountForAllProducts(billInfoList: List<BillInfo>, callback: (Boolean) -> Unit) {
        val pendingRequests = AtomicInteger(billInfoList.size)
        val allValid = AtomicBoolean(true)

        for (billInfo in billInfoList) {
            val sold = billInfo.amount
            val apiService = RetrofitClient.retrofit!!.create(APIService::class.java)
            billInfo.productId?.let {
                apiService.getProductInfor(it).enqueue(object : Callback<Product> {
                    override fun onResponse(call: Call<Product>, response: Response<Product>) {
                        if (response.isSuccessful && response.body() != null) {
                            val product = response.body()
                            val remainAmount = product?.remainAmount ?: 0
                            if (remainAmount < sold) {
                                allValid.set(false)
                            }
                        } else {
                            allValid.set(false)
                        }
                        if (pendingRequests.decrementAndGet() == 0) {
                            callback(allValid.get())
                        }
                    }

                    override fun onFailure(call: Call<Product>, t: Throwable) {
                        allValid.set(false)
                        if (pendingRequests.decrementAndGet() == 0) {
                            callback(false)
                        }
                    }
                })
            }
        }

        // If there are no items in the list, directly call the callback with false
        if (billInfoList.isEmpty()) {
            callback(false)
        }
    }
}
