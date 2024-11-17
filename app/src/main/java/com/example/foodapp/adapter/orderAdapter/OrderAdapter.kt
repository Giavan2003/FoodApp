package com.example.foodapp.adapter.orderAdapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodapp.Interface.APIService
import com.example.foodapp.R
import com.example.foodapp.RetrofitClient
import com.example.foodapp.activity.order.OrderActivity
import com.example.foodapp.activity.order.OrderDetailActivity
import com.example.foodapp.custom.CustomMessageBox.CustomAlertDialog
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.ItemOrderLayoutBinding
import com.example.foodapp.helper.FirebaseStatusOrderHelper
import com.example.foodapp.model.Bill
import com.example.foodapp.model.BillInfo
import com.example.foodapp.model.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class OrderAdapter(
    private val context: Context,
    private val dsOrder: ArrayList<Bill>?,
    private val type: Int,
    private val userId: String
) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    private val apiService: APIService = RetrofitClient.retrofit!!.create(APIService::class.java)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemOrderLayoutBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val tmp: Bill = dsOrder!![position]
        viewHolder.binding.txtId.text = tmp.billId ?: "Unknown"
        viewHolder.binding.txtDate.text = tmp.orderDate ?: "Unknown"
        viewHolder.binding.txtStatus.text = tmp.orderStatus ?: "Unknown"
        viewHolder.binding.txtTotal.text = CurrencyFormatter.getFormatter().format(tmp.totalPrice.toDouble())
        tmp.billId?.let { billId ->
            FirebaseDatabase.getInstance().getReference("BillInfos").child(billId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val billInfo: BillInfo? = snapshot.children.iterator().next().getValue(
                            BillInfo::class.java
                        )
                        billInfo?.let {
                            apiService.getProductInfor(it.productId!!)
                                .enqueue(object : Callback<Product?> {
                                    override fun onResponse(
                                        call: Call<Product?>,
                                        response: Response<Product?>
                                    ) {
                                        response.body()?.let { product ->
                                            Glide.with(context)
                                                .load(product.productImage1)
                                                .placeholder(R.drawable.default_image)
                                                .into(viewHolder.binding.imgFood)
                                        }
                                    }

                                    override fun onFailure(call: Call<Product?>, t: Throwable) {

                                    }
                                })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }


        if (type == OrderActivity.CURRENT_ORDER) {
            viewHolder.binding.btnSee.text = "Received"
            updateReceivedButtonState(viewHolder, tmp.orderStatus)
            viewHolder.binding.btnSee.setOnClickListener {
                if (viewHolder.binding.txtStatus.text == "Shipping") {
                    CustomAlertDialog(context, "Do you want to confirm this order?")
                    CustomAlertDialog.binding.btnYes.setOnClickListener {
                        tmp.billId?.let { billId ->
                            FirebaseStatusOrderHelper().setShippingToCompleted(
                                billId,
                                object : FirebaseStatusOrderHelper.DataStatus {
                                    override fun dataIsLoaded(bills: List<Bill>, isExistingBill: Boolean) {

                                    }

                                    override fun dataIsInserted() {

                                    }

                                    override fun dataIsUpdated() {
                                        SuccessfulToast(
                                            context,
                                            "Your order has been changed to completed state!"
                                        ).showToast()
                                        viewHolder.binding.txtStatus.text = "Completed"
                                        updateReceivedButtonState(viewHolder, "Completed")
                                    }

                                    override fun dataIsDeleted() {

                                    }
                                }
                            )
                        }
                        CustomAlertDialog.alertDialog.dismiss()
                    }
                    CustomAlertDialog.binding.btnNo.setOnClickListener {
                        CustomAlertDialog.alertDialog.dismiss()
                    }
                    CustomAlertDialog.showAlertDialog()
                }
            }
        } else {
            viewHolder.binding.txtStatus.setTextColor(Color.parseColor("#48DC7D"))
            viewHolder.binding.btnSee.text = "Feedback & Rate"
            viewHolder.binding.btnSee.isEnabled = !tmp.isCheckAllComment
            viewHolder.binding.btnSee.setBackgroundResource(
                if (tmp.isCheckAllComment) R.drawable.background_feedback_disabled_button
                else R.drawable.background_feedback_enable_button
            )
            viewHolder.binding.btnSee.setOnClickListener {
                val intent = Intent(context, OrderDetailActivity::class.java)
                intent.putExtra("Bill", tmp)
                intent.putExtra("userId", userId)
                context.startActivity(intent)
            }
        }

        viewHolder.binding.root.setOnClickListener {
            val intent = Intent(context, OrderDetailActivity::class.java)
            intent.putExtra("Bill", tmp)
            intent.putExtra("userId", userId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return dsOrder?.size ?: 0
    }

    private fun updateReceivedButtonState(viewHolder: ViewHolder, status: String?) {
        viewHolder.binding.btnSee.isEnabled = status == "Shipping"
    }

    class ViewHolder(val binding: ItemOrderLayoutBinding) : RecyclerView.ViewHolder(binding.root)

}

