package com.example.foodapp.adapter.Cart

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.example.foodapp.Interface.IAdapterItemListener
import com.example.foodapp.R
import com.example.foodapp.activity.ProductInformation.ProductInfoActivity
import com.example.foodapp.custom.CustomMessageBox.CustomAlertDialog
import com.example.foodapp.custom.CustomMessageBox.FailToast
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.ItemCartProductBinding
import com.example.foodapp.helper.FirebaseNotificationHelper
import com.example.foodapp.helper.FirebaseProductInfoHelper
import com.example.foodapp.helper.FirebaseUserInfoHelper
import com.example.foodapp.model.Cart
import com.example.foodapp.model.CartInfo
import com.example.foodapp.model.Notification
import com.example.foodapp.model.Product
import com.example.foodapp.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class CartProductAdapter(
    private val mContext: Context,
    private val mCartInfos: List<CartInfo>,
    private val cartId: String,
    private var isCheckAll: Boolean,
    private val userId: String
) : RecyclerView.Adapter<CartProductAdapter.ViewHolder>() {

    private val viewBinderHelper = ViewBinderHelper()
    private var checkedItemCount = 0
    private var checkedItemPrice: Long = 0
    private var adapterItemListener: IAdapterItemListener? = null
    private var userName: String? = null
    private val selectedItems = ArrayList<CartInfo>()

    init {
        viewBinderHelper.setOpenOnlyOne(true)

        FirebaseUserInfoHelper(mContext).readUserInfo(userId, object : FirebaseUserInfoHelper.DataStatus {
            override fun dataIsLoaded(user: User?) {
                userName = user?.userName ?: ""
            }

            override fun dataIsInserted() {}

            override fun dataIsUpdated() {}

            override fun dataIsDeleted() {}
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cartInfo = mCartInfos[position]
        Log.d("CartProductAdapter", "Cart Info: $cartInfo")
        viewBinderHelper.bind(holder.binding.swipeRevealLayout, cartInfo.cartInfoId)
        holder.binding.checkBox.isChecked = isCheckAll

        val productId = cartInfo.productId
        Log.d("Product", "Product Info: $productId")
        if (!productId.isNullOrEmpty()) {
            FirebaseDatabase.getInstance().getReference().child("Products").child(productId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val product = snapshot.getValue(Product::class.java)
                        if (product != null) {
                            // Gán thông tin sản phẩm vào các view
                            holder.binding.productName.text = product.productName
                            holder.binding.productPrice.text = (convertToMoney(product.productPrice?.toLong() ?: 0L)) + "đ"
                            Glide.with(mContext).load(product.productImage1).placeholder(R.mipmap.ic_launcher).into(holder.binding.productImage)
                            holder.binding.productAmount.text = cartInfo.amount.toString()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        } else {
            Log.e("CartProductAdapter", "Invalid productId: $productId")
        }

        isLiked(holder.binding.like, cartInfo.productId!!)

        holder.binding.add.setOnClickListener {
            FirebaseDatabase.getInstance().reference.child("Products")
                .child(cartInfo.productId!!).child("remainAmount")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val amount = holder.binding.productAmount.text.toString().toInt()
                        val remainAmount = snapshot.getValue(Int::class.java) ?: 0
                        if (amount >= remainAmount) {
                            FailToast(mContext, "Can't add anymore!").showToast()
                        } else {
                            // Change display value
                            var updatedAmount = amount + 1
                            holder.binding.productAmount.setText(updatedAmount.toString())
                            holder.binding.checkBox.isChecked = false
                            isCheckAll = false

                            adapterItemListener?.let {
                                it.onCheckedItemCountChanged(0, 0, ArrayList())
                                it.onAddClicked()
                            }


                            FirebaseDatabase.getInstance().reference.child("CartInfo's")
                                .child(cartId).child(cartInfo.cartInfoId!!)
                                .child("amount").setValue(updatedAmount)

                            FirebaseDatabase.getInstance().reference.child("Carts")
                                .child(cartId).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val cart = snapshot.getValue(Cart::class.java)
                                        FirebaseDatabase.getInstance().reference.child("Products")
                                            .child(cartInfo.productId!!)
                                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(snapshot1: DataSnapshot) {
                                                    val product = snapshot1.getValue(Product::class.java)
                                                    val totalAmount = (cart?.totalAmount ?: 0) + 1
                                                    val totalPrice = (cart?.totalPrice?.toLong() ?: 0L) + (product?.productPrice?.toLong() ?: 0L)

                                                    val map = hashMapOf<String, Any>(
                                                        "totalAmount" to totalAmount,
                                                        "totalPrice" to totalPrice
                                                    )
                                                    FirebaseDatabase.getInstance().reference.child("Carts")
                                                        .child(cartId).updateChildren(map)
                                                }

                                                override fun onCancelled(error: DatabaseError) {}
                                            })
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }

        holder.binding.subtract.setOnClickListener {
            val amount = holder.binding.productAmount.text.toString().toIntOrNull()
            if (amount != null && amount > 1) {
                // Change display value
                var newAmount = amount - 1
                holder.binding.productAmount.setText(newAmount.toString())
                isCheckAll = false

                adapterItemListener?.apply {
                    onCheckedItemCountChanged(0, 0, ArrayList())
                    onSubtractClicked()
                }

                // Save to firebase
                FirebaseDatabase.getInstance().getReference().child("CartInfo's").child(cartId).child(cartInfo.cartInfoId!!).child("amount")
                    .setValue(newAmount)

                FirebaseDatabase.getInstance().getReference().child("Carts").child(cartId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val cart = snapshot.getValue(Cart::class.java)
                            FirebaseDatabase.getInstance().getReference().child("Products").child(cartInfo.productId!!)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot1: DataSnapshot) {
                                        val product = snapshot1.getValue(Product::class.java)
                                        val totalAmount = cart?.totalAmount?.minus(1) ?: 0
                                        val totalPrice = cart?.totalPrice?.minus(product?.productPrice ?: 0) ?: 0L

                                        val map = hashMapOf<String, Any>(
                                            "totalAmount" to totalAmount,
                                            "totalPrice" to totalPrice
                                        )
                                        FirebaseDatabase.getInstance().getReference().child("Carts").child(cartId).updateChildren(map)
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })

                FirebaseDatabase.getInstance().getReference().child("Products").child(cartInfo.productId!!)
                    .child("remainAmount")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val remainAmount = snapshot.getValue(Int::class.java) ?: 0
                            FirebaseDatabase.getInstance().getReference().child("Products").child(cartInfo.productId!!)
                                .child("remainAmount").setValue(remainAmount + 1)
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            } else {
                FailToast(mContext, "Can't reduce anymore!").showToast()
            }
        }

        holder.binding.like.setOnClickListener {
            when (holder.binding.like.tag) {
                "like" -> {
                    FirebaseDatabase.getInstance().getReference()
                        .child("Favorites")
                        .child(userId)
                        .child(cartInfo.productId!!)
                        .setValue(true)
                    pushNotificationFavourite(cartInfo)
                    SuccessfulToast(mContext, "Added to your favourite list").showToast()
                }
                "liked" -> {
                    FirebaseDatabase.getInstance().getReference()
                        .child("Favorites")
                        .child(userId)
                        .child(cartInfo.productId!!)
                        .removeValue()
                    SuccessfulToast(mContext, "Removed from your favourite list").showToast()
                }
            }
        }

        holder.binding.delete.setOnClickListener {
            CustomAlertDialog(mContext, "Delete this product?")

            val customAlertDialog = CustomAlertDialog(mContext, "Delete this product?").apply {
                binding.btnYes.setOnClickListener {
                    alertDialog.dismiss()

                    FirebaseDatabase.getInstance().getReference()
                        .child("CartInfo's")
                        .child(cartId)
                        .child(cartInfo.cartInfoId!!)
                        .removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                SuccessfulToast(mContext, "Delete product successfully!").showToast()
                                adapterItemListener?.onDeleteProduct()
                            }
                        }

                    FirebaseDatabase.getInstance().getReference()
                        .child("Carts")
                        .child(cartId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val cart = snapshot.getValue(Cart::class.java)
                                FirebaseDatabase.getInstance().getReference()
                                    .child("Products")
                                    .child(cartInfo.productId!!)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot1: DataSnapshot) {
                                            val product = snapshot1.getValue(Product::class.java)
                                            val totalAmount = cart?.totalAmount?.minus(cartInfo.amount) ?: 0
                                            val totalPrice = cart?.totalPrice?.minus((product?.productPrice ?: 0) * cartInfo.amount) ?: 0L

                                            val map = hashMapOf<String, Any>(
                                                "totalAmount" to totalAmount,
                                                "totalPrice" to totalPrice
                                            )
                                            FirebaseDatabase.getInstance().getReference()
                                                .child("Carts")
                                                .child(cartId)
                                                .updateChildren(map)
                                        }

                                        override fun onCancelled(error: DatabaseError) {}
                                    })
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })

                    FirebaseDatabase.getInstance().getReference()
                        .child("Products")
                        .child(cartInfo.productId!!)
                        .child("remainAmount")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val remainAmount = (snapshot.getValue(Int::class.java) ?: 0) + cartInfo.amount
                                FirebaseDatabase.getInstance().getReference()
                                    .child("Products")
                                    .child(cartInfo.productId!!)
                                    .child("remainAmount")
                                    .setValue(remainAmount)
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }

                binding.btnNo.setOnClickListener {
                    alertDialog.dismiss()
                }
            }

            customAlertDialog.showAlertDialog()

        }

        holder.binding.itemContainer.setOnClickListener {
            FirebaseDatabase.getInstance().reference.child("Products").child(cartInfo.productId!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val product = snapshot.getValue(Product::class.java)
                    product?.let {
                        val intent = Intent(mContext, ProductInfoActivity::class.java).apply {
                            putExtra("productId", it.productId)
                            putExtra("productName", it.productName)
                            putExtra("productPrice", it.productPrice)
                            putExtra("productImage1", it.productImage1)
                            putExtra("productImage2", it.productImage2)
                            putExtra("productImage3", it.productImage3)
                            putExtra("productImage4", it.productImage4)
                            putExtra("ratingStar", it.ratingStar)
                            putExtra("productDescription", it.description)
                            putExtra("publisherId", it.publisherId)
                            putExtra("sold", it.sold)
                            putExtra("productType", it.productType)
                            putExtra("remainAmount", it.remainAmount)
                            putExtra("ratingAmount", it.ratingAmount)
                            putExtra("state", it.state)
                            putExtra("userId", userId)
                            putExtra("userName", userName)
                        }
                        mContext.startActivity(intent)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle cancellation if needed
                }
            })
        }

        holder.binding.checkBox.setOnCheckedChangeListener { compoundButton, isChecked ->
            FirebaseDatabase.getInstance().reference.child("Products").child(cartInfo.productId!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val product = snapshot.getValue(Product::class.java)
                    if (product != null) {
                        if (isChecked) {
                            checkedItemCount += cartInfo.amount
                            checkedItemPrice += cartInfo.amount * product.productPrice
                            selectedItems.add(cartInfo)
                        } else {
                            checkedItemCount -= cartInfo.amount
                            checkedItemPrice -= cartInfo.amount * product.productPrice
                            selectedItems.removeIf { c -> c.cartInfoId == cartInfo.cartInfoId }
                        }

                        adapterItemListener?.onCheckedItemCountChanged(checkedItemCount, checkedItemPrice, ArrayList(selectedItems))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle cancellation if necessary
                }
            })
        }

    }

    private fun isLiked(imageButton: ImageButton, productId: String) {
        FirebaseDatabase.getInstance().getReference().child("Favorites").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(productId).exists()) {
                        imageButton.setImageResource(R.drawable.ic_liked)
                        imageButton.tag = "liked"
                    } else {
                        imageButton.setImageResource(R.drawable.ic_like)
                        imageButton.tag = "like"
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

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

    override fun getItemCount(): Int {
        return mCartInfos.size
    }

    fun saveStates(outState: Bundle) {
        viewBinderHelper.saveStates(outState)
    }

    fun setAdapterItemListener(adapterItemListener: IAdapterItemListener) {
        this.adapterItemListener = adapterItemListener
    }

    fun restoreStates(instate: Bundle) {
        viewBinderHelper.restoreStates(instate)
    }

    class ViewHolder(val binding: ItemCartProductBinding) : RecyclerView.ViewHolder(binding.root)

    fun pushNotificationFavourite(cartInfo: CartInfo) {
        FirebaseProductInfoHelper(cartInfo.productId!!).readInformationById(object : FirebaseProductInfoHelper.DataStatusInformationOfProduct {
            override fun DataIsLoaded(product: Product?) {
                val title = "Favourite product"
                val content = "$userName liked your product: ${product?.productName}. Go to Product Information to check it."
                val notification = FirebaseNotificationHelper.createNotification(
                    title, content, product?.productImage1 ?: "", product?.productId ?: "", "None", "None", null
                )

                FirebaseNotificationHelper(mContext).addNotification(product?.publisherId ?: "", notification, object : FirebaseNotificationHelper.DataStatus {
                    override fun DataIsLoaded(notificationList: List<Notification>, notificationListToNotify: List<Notification>) {}

                    override fun DataIsInserted() {}

                    override fun DataIsUpdated() {}

                    override fun DataIsDeleted() {}
                })
            }

            override fun DataIsInserted() {}

            override fun DataIsUpdated() {}

            override fun DataIsDeleted() {}
        })



    }
}

