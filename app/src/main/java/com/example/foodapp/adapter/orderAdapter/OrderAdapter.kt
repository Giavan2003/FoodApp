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
    private val dsOrder: ArrayList<Bill>,
    private val type: Int,
    private val userId: String
) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemOrderLayoutBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tmp = dsOrder[position]
        with(holder.binding) {
            if (type == OrderActivity.CURRENT_ORDER) {
                btnSee.text = "Received"
                btnSee.setOnClickListener {
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
                                        SuccessfulToast(context, "Your order has been changed to completed state!").showToast()

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
            } else {
                txtStatus.setTextColor(Color.parseColor("#48DC7D"))
                btnSee.text = "Feedback & Rate"
                if (tmp.isCheckAllComment) {
                    btnSee.isEnabled = false
                    btnSee.setBackgroundResource(R.drawable.background_feedback_disabled_button)
                } else {
                    btnSee.isEnabled = true
                    btnSee.setBackgroundResource(R.drawable.background_feedback_enable_button)
                }
                btnSee.setOnClickListener {
                    val intent = Intent(context, OrderDetailActivity::class.java).apply {
                        putExtra("Bill", tmp)
                        putExtra("userId", userId)
                    }
                    context.startActivity(intent)
                }
            }

            txtId.text = tmp.billId.toString()
            txtDate.text = tmp.orderDate.toString()
            txtStatus.text = tmp.orderStatus
            txtTotal.text = CurrencyFormatter.getFormatter().format(tmp.totalPrice.toDouble()).toString()

            root.setOnClickListener {
                val intent = Intent(context, OrderDetailActivity::class.java).apply {
                    putExtra("Bill", tmp)
                    putExtra("userId", userId)
                }
                context.startActivity(intent)
            }

            FirebaseDatabase.getInstance().getReference("BillInfos").child(tmp.billId!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val tmpBillInfo = snapshot.children.firstOrNull()?.getValue(BillInfo::class.java)
                        tmpBillInfo?.let {
                            FirebaseDatabase.getInstance().getReference("Products")
                                .child(it.productId!!)
                                .child("productImage1")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        Glide.with(context)
                                            .load(snapshot.getValue(String::class.java))
                                            .placeholder(R.drawable.default_image)
                                            .into(imgFood)
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    override fun getItemCount(): Int = dsOrder.size

    class ViewHolder(val binding: ItemOrderLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}


