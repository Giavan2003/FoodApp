package com.example.foodapp.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodapp.R
import com.example.foodapp.activity.Home.ChatDetailActivity
import com.example.foodapp.activity.ProductInformation.ProductInfoActivity

import com.example.foodapp.activity.order.OrderDetailActivity
import com.example.foodapp.activity.orderSellerManagement.DeliveryManagementActivity
import com.example.foodapp.databinding.ItemNotificationBinding
import com.example.foodapp.helper.FirebaseNotificationHelper
import com.example.foodapp.helper.FirebaseProductInfoHelper
import com.example.foodapp.model.Bill
import com.example.foodapp.model.Notification
import com.example.foodapp.model.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationListAdapter(
    private val mContext: Context,
    private val notificationList: List<Notification>,
    private val userId: String
) : RecyclerView.Adapter<NotificationListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notificationList[position]
        with(holder.binding) {
            txtTitleNotification.text = notification.title
            txtContentNotification.text = notification.content
            txtTimeNotification.text = notification.time

            if (notification.imageURL?.isEmpty() == true) {
                holder.binding.imgNotification.setImageResource(R.drawable.ic_launcher_background)
            } else {
                holder.binding.imgNotification.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(mContext)
                    .asBitmap()
                    .load(notification.imageURL)
                    .into(holder.binding.imgNotification)
            }


            if (!notification.isRead) {
                dotStatusRead.visibility = View.VISIBLE
                backgroundNotificationItem.setBackgroundColor(Color.parseColor("#e3e3e3"))
            } else {
                dotStatusRead.visibility = View.GONE
                backgroundNotificationItem.setBackgroundColor(Color.TRANSPARENT)
            }

            backgroundNotificationItem.setOnClickListener {
                if (!notification.isRead) {
                    notification.isRead = true
                    FirebaseNotificationHelper(mContext).updateNotification(userId, notification, object : FirebaseNotificationHelper.DataStatus {
                        override fun DataIsLoaded(
                            notificationList: List<Notification>,
                            notificationListToNotify: List<Notification>
                        ) {}

                        override fun DataIsInserted() {}
                        override fun DataIsUpdated() {}
                        override fun DataIsDeleted() {}
                    })
                }

                when {
                    notification.billId != "None" -> {
                        val bill = Bill().apply { billId = notification.billId }
                        val intent = Intent(mContext, OrderDetailActivity::class.java).apply {
                            putExtra("Bill", bill)
                            putExtra("userId", userId)
                        }
                        mContext.startActivity(intent)
                    }

                    notification.productId != "None" -> {
                        val userName = arrayOfNulls<String>(1)
                        FirebaseDatabase.getInstance().reference.child("Users").child(userId)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    userName[0] = snapshot.child("userName").getValue(String::class.java)
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })

                        FirebaseProductInfoHelper(notification.productId!!)
                            .readInformationById(object : FirebaseProductInfoHelper.DataStatusInformationOfProduct {
                                override fun DataIsLoaded(item: Product?) {
                                    val intent = Intent(mContext, ProductInfoActivity::class.java).apply {
                                        putExtra("productId", item?.productId)
                                        putExtra("productName", item?.productName)
                                        putExtra("productPrice", item?.productPrice)
                                        putExtra("productImage1", item?.productImage1)
                                        putExtra("productImage2", item?.productImage2)
                                        putExtra("productImage3", item?.productImage3)
                                        putExtra("productImage4", item?.productImage4)
                                        putExtra("ratingStar", item?.ratingStar)
                                        putExtra("productDescription", item?.description)
                                        putExtra("publisherId", item?.publisherId)
                                        putExtra("sold", item?.sold)
                                        putExtra("productType", item?.productType)
                                        putExtra("remainAmount", item?.remainAmount)
                                        putExtra("ratingAmount", item?.ratingAmount)
                                        putExtra("state", item?.state)
                                        putExtra("userId", userId)
                                        putExtra("userName", userName[0])
                                    }
                                    mContext.startActivity(intent)
                                }

                                override fun DataIsInserted() {}
                                override fun DataIsUpdated() {}
                                override fun DataIsDeleted() {}
                            })
                    }

                    notification.confirmId != "None" -> {
                        val intent = Intent(mContext, DeliveryManagementActivity::class.java).apply {
                            putExtra("userId", userId)
                        }
                        mContext.startActivity(intent)
                    }

                    notification.publisher != null -> {
                        val intent = Intent(mContext, ChatDetailActivity::class.java).apply {
                            action = "chatActivity"
                            putExtra("publisher", notification.publisher)
                        }
                        mContext.startActivity(intent)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = notificationList.size

    class ViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)
}
