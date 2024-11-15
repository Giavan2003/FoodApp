package com.example.foodapp.adapter.Cart

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.example.foodapp.Interface.APIService
import com.example.foodapp.Interface.IAdapterItemListener
import com.example.foodapp.R
import com.example.foodapp.RetrofitClient
import com.example.foodapp.custom.CustomMessageBox.CustomAlertDialog
import com.example.foodapp.custom.CustomMessageBox.FailToast
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.ItemCartProductBinding
import com.example.foodapp.model.CartInfo
import com.example.foodapp.model.CartProduct
import com.example.foodapp.model.User
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CartProductAdapter(
    private val context: Context,
    private var cartInfos: List<CartInfo>,
    private val cartId: String,
    private var isCheckAll: Boolean,
    private val userId: String
) : RecyclerView.Adapter<CartProductAdapter.ViewHolder>() {

    private val viewBinderHelper = ViewBinderHelper()
    private var checkedItemCount = 0
    private var checkedItemPrice = 0L
    private var selectedItems = ArrayList<CartInfo>()
    private var adapterItemListener: IAdapterItemListener? = null
    private var userName: String? = null
    private val apiService: APIService =  RetrofitClient.retrofit!!.create(APIService::class.java)

    init {
        viewBinderHelper.setOpenOnlyOne(true)

        apiService.getUserByUserId(userId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    userName = response.body()?.userName
                } else {
                    Log.d("usernameCartAdapter", "unsuccessfully")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.d("usernameCartAdapterFailure", t.message ?: "Error")
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cartInfo = cartInfos[position]
        viewBinderHelper.bind(holder.binding.swipeRevealLayout, cartInfo.cartInfoId)
        holder.binding.checkBox.isChecked = isCheckAll

        apiService.getProductCart(cartInfo.productId!!).enqueue(object : Callback<CartProduct> {
            override fun onResponse(call: Call<CartProduct>, response: Response<CartProduct>) {
                if (response.isSuccessful) {
                    response.body()?.let { cartProduct ->
                        with(holder.binding) {
                            productName.text = cartProduct.productName
                            productPrice.text = "${convertToMoney(cartProduct.productPrice)}đ"
                            Glide.with(context).load(cartProduct.productImage1).placeholder(R.mipmap.ic_launcher).into(productImage)
                            productAmount.text = cartInfo.amount.toString()
                        }
                        holder.remainAmount = cartProduct.remainAmount
                    }
                } else {
                    Log.e("Retrofit", "Response not successful: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<CartProduct>, t: Throwable) {
                Log.e("Retrofit", "API call failed: ${t.message}")
            }
        })

        holder.binding.add.setOnClickListener {
            var amount = holder.binding.productAmount.text.toString().toInt()
            if (amount >= holder.remainAmount) {
                FailToast(context, "Can't add anymore!").showToast()
            } else {
                amount++
                holder.binding.productAmount.text = amount.toString()
                holder.binding.checkBox.isChecked = false
                isCheckAll = false
                adapterItemListener?.onCheckedItemCountChanged(0, 0, ArrayList())
                adapterItemListener?.onAddClicked()

                FirebaseDatabase.getInstance()
                    .getReference("CartInfos")
                    .child(cartId)
                    .child(cartInfo.cartInfoId!!)
                    .child("amount")
                    .setValue(amount)
            }
        }

        holder.binding.subtract.setOnClickListener {
            var amount = holder.binding.productAmount.text.toString().toInt()
            if (amount > 1) {
                amount--
                holder.binding.productAmount.text = amount.toString()
                isCheckAll = false
                adapterItemListener?.onCheckedItemCountChanged(0, 0, ArrayList())
                adapterItemListener?.onSubtractClicked()

                FirebaseDatabase.getInstance()
                    .getReference("CartInfos")
                    .child(cartId)
                    .child(cartInfo.cartInfoId!!)
                    .child("amount")
                    .setValue(amount)
            } else {
                FailToast(context, "Can't reduce anymore!").showToast()
            }
        }

        holder.binding.like.setOnClickListener {
            handleLikeAction(holder.binding.like, cartInfo)
        }

        holder.binding.delete.setOnClickListener {
            CustomAlertDialog(context, "Delete this product?")

            CustomAlertDialog.binding.btnYes.setOnClickListener {
                CustomAlertDialog.alertDialog.dismiss()

                FirebaseDatabase.getInstance()
                    .getReference("CartInfos")
                    .child(cartId)
                    .child(cartInfo.cartInfoId!!)
                    .removeValue()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            SuccessfulToast(context, "Delete product successfully!").showToast()
                            adapterItemListener?.onDeleteProduct()
                        }
                    }
            }

            CustomAlertDialog.binding.btnNo.setOnClickListener {
                CustomAlertDialog.alertDialog.dismiss()
            }

            CustomAlertDialog.showAlertDialog()
        }

    }

    override fun getItemCount(): Int = cartInfos.size
    fun saveStates(outState: Bundle?) {
        viewBinderHelper.saveStates(outState)
    }

    fun setAdapterItemListener(listener: IAdapterItemListener) {
        this.adapterItemListener = listener
    }
    fun restoreStates(instate: Bundle?) {
        viewBinderHelper.restoreStates(instate)
    }

    private fun convertToMoney(price: Long): String {
        return price.toString().reversed().chunked(3).joinToString(",").reversed()
    }

    private fun handleLikeAction(likeButton: ImageButton, cartInfo: CartInfo) {
        val favoritesRef = FirebaseDatabase.getInstance().getReference("Favorites").child(userId)
        if (likeButton.tag == "like") {
            favoritesRef.child(cartInfo.productId!!).setValue(true).addOnCompleteListener {
                if (it.isSuccessful) {
                    SuccessfulToast(context, "Added to your favourite list").showToast()
                    likeButton.setImageResource(R.drawable.ic_liked)
                    likeButton.tag = "liked"
                }
            }
        } else if (likeButton.tag == "liked") {
            favoritesRef.child(cartInfo.productId!!).removeValue().addOnCompleteListener {
                if (it.isSuccessful) {
                    SuccessfulToast(context, "Removed from your favourite list").showToast()
                    likeButton.setImageResource(R.drawable.ic_like)
                    likeButton.tag = "like"
                }
            }
        }
    }

    inner class ViewHolder(val binding: ItemCartProductBinding) : RecyclerView.ViewHolder(binding.root) {
        var remainAmount: Int = 0
    }
}
