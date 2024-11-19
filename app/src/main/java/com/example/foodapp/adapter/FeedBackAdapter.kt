package com.example.foodapp.adapter

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.google.firebase.database.FirebaseDatabase;


import com.example.foodapp.R;

import com.example.foodapp.activity.feedback.FeedBackActivity;
import com.example.foodapp.custom.CustomMessageBox.FailToast;
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast

import com.example.foodapp.databinding.LayoutFeedbackBillifoBinding;
import com.example.foodapp.dialog.UploadDialog;
import com.example.foodapp.helper.FirebaseNotificationHelper;
import com.example.foodapp.model.Bill;
import com.example.foodapp.model.BillInfo;

import com.example.foodapp.model.Notification;
import com.example.foodapp.model.Product;
import com.example.foodapp.model.Comment;
import com.example.foodapp.model.IntegerWrapper




class FeedBackAdapter(
    private val mContext: Context,
    private val ds: ArrayList<BillInfo>,
    private val currentBill: Bill,
    private val userId: String
) : RecyclerView.Adapter<FeedBackAdapter.ViewHolder>() {


    private var tmp: Product? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutFeedbackBillifoBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = ds[position]

        holder.binding.edtComment.setText("")
        val starRating = IntegerWrapper(5)

        setEventForStar(holder, starRating)
        holder.binding.star5.performClick()

        val databaseReference = FirebaseDatabase.getInstance().getReference("Products")
        item.productId?.let {
            databaseReference.child(it).get()
                .addOnSuccessListener { snapshot ->
                    val product = snapshot.getValue(Product::class.java)
                    product?.let {
                        tmp = it
                        it.productName?.let { it1 -> Log.d("ProductBillInfo", it1) }
                        holder.binding.lnBillInfo.txtPrice.text =
                            CurrencyFormatter.getFormatter().format(item.amount * it.productPrice.toDouble())
                        holder.binding.lnBillInfo.txtName.text = it.productName
                        holder.binding.lnBillInfo.txtCount.text = "Count: ${item.amount}"
                        Glide.with(mContext)
                            .load(it.productImage1)
                            .placeholder(R.drawable.default_image)
                            .into(holder.binding.lnBillInfo.imgFood)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("ProductError", exception.message ?: "Error fetching product info")
                }
        }


        holder.binding.edtComment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length ?: 0 >= 200) {
                    FailToast(mContext, "Your comment's length must not be over 200 characters!").showToast()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        holder.binding.btnSend.setOnClickListener {
            val commentText = holder.binding.edtComment.text.toString().trim()
            if (commentText.isNotEmpty()) {
                val dialog = UploadDialog(mContext)
                dialog.show()

                val commentId = FirebaseDatabase.getInstance().reference.push().key.orEmpty()
                val comment = Comment(commentText, commentId, userId, starRating.value.toFloat())

                item.productId?.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Comments")
                        .child(it1)
                        .child(commentId)
                        .setValue(comment)
                        .addOnCompleteListener { task ->
                            dialog.dismiss()
                            if (task.isSuccessful) {
                                SuccessfulToast(
                                    mContext,
                                    "Thank you for giving feedback to my product!"
                                ).showToast()
                                pushNotificationFeedBack(item)
                                updateListBillInfo(item)

                                tmp?.let {
                                    val currentRatingStar = it.ratingStar ?: 0.0 // Giá trị mặc định là 0.0 nếu null
                                    val currentRatingAmount = it.ratingAmount ?: 0 // Giá trị mặc định là 0 nếu null

                                    val ratingAmount = currentRatingAmount + 1
                                    val ratingStar = (currentRatingStar * currentRatingAmount + starRating.value) / ratingAmount

                                    val productUpdate = mapOf(
                                        "ratingAmount" to ratingAmount,
                                        "ratingStar" to ratingStar
                                    )

                                    val databaseReference = FirebaseDatabase.getInstance().getReference("Products")
                                    item.productId?.let { it1 ->
                                        databaseReference.child(it1).updateChildren(productUpdate)
                                            .addOnSuccessListener {
                                                Log.d("Comment", "Success")
                                            }
                                            .addOnFailureListener { exception ->
                                                Log.d("CommentFailure", exception.message ?: "Error")
                                            }
                                    }
                                }

                            } else {
                                FailToast(mContext, "Some errors occurred!").showToast()
                            }
                        }
                }
            } else {
                AlertDialog.Builder(mContext)
                    .setIcon(R.drawable.icon_alert)
                    .setTitle("Chú ý")
                    .setMessage("Nhớ ghi comment nha bạn ơi!")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Ok", null)
                    .create()
                    .show()
            }
        }
    }

    private fun setEventForStar(holder: ViewHolder, starRating: IntegerWrapper) {
        listOf(holder.binding.star1, holder.binding.star2, holder.binding.star3, holder.binding.star4, holder.binding.star5).forEachIndexed { index, star ->
            star.setOnClickListener { onStarClicked(it, holder, starRating, index + 1) }
        }
    }

    private fun onStarClicked(view: View, holder: ViewHolder, starRating: IntegerWrapper, starValue: Int) {
        starRating.value = starValue
        val stars = listOf(holder.binding.star1, holder.binding.star2, holder.binding.star3, holder.binding.star4, holder.binding.star5)
        stars.forEachIndexed { index, star ->
            star.setImageResource(if (index < starValue) R.drawable.star_filled else R.drawable.star_none)
        }
    }

    private fun updateListBillInfo(item: BillInfo) {
        ds.remove(item)
        notifyDataSetChanged()

        currentBill.billId?.let {
            item.billInfoId?.let { it1 ->
                FirebaseDatabase.getInstance().reference
                    .child("BillInfos")
                    .child(it)
                    .child(it1)
                    .child("check")
                    .setValue(true)
            }
        }

        if (ds.isEmpty()) {
            currentBill.billId?.let {
                FirebaseDatabase.getInstance().reference
                    .child("Bills")
                    .child(it)
                    .child("checkAllComment")
                    .setValue(true)
            }

            (mContext as? FeedBackActivity)?.finish()
        }
    }

    private fun pushNotificationFeedBack(billInfo: BillInfo) {
        tmp?.let {
            val title = "Product feedback"
            val content = "Your product '${it.productName}' has just got a new feedback. Check it out!"
            val notification = it.productImage1?.let { it1 ->
                it.productId?.let { it2 ->
                    FirebaseNotificationHelper.createNotification(
                        title, content, it1, it2, "None", "None", null
                    )
                }
            }
            it.publisherId?.let { it1 ->
                if (notification != null) {
                    FirebaseNotificationHelper(mContext).addNotification(it1, notification, object : FirebaseNotificationHelper.DataStatus {
                        override fun DataIsInserted() {
                            Log.d("Notification", "Notification inserted successfully")
                        }

                        override fun DataIsLoaded(
                            notificationList: List<Notification>,
                            notificationListToNotify: List<Notification>
                        ) {
                            // Không cần sử dụng cho addNotification
                        }

                        override fun DataIsUpdated() {
                            // Không cần thiết cho addNotification
                        }

                        override fun DataIsDeleted() {
                            // Không cần thiết cho addNotification
                        }
                    })
                }
            }
        }
    }

    override fun getItemCount(): Int = ds.size

    class ViewHolder(val binding: LayoutFeedbackBillifoBinding) : RecyclerView.ViewHolder(binding.root)
}
