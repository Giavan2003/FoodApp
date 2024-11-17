package com.example.foodapp.adapter.Home

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodapp.activity.ProductInformation.ProductInfoActivity
import com.example.foodapp.databinding.ItemHomeBinding
import com.example.foodapp.databinding.ItemProgressbarBinding
import com.example.foodapp.model.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale


class FoodDrinkFrgAdapter(
    private val ds: List<Product?>?,
    private val userId: String,
    private val mContext: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1
    private val nf = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    private var userName: String? = null

    init {
        FirebaseDatabase.getInstance().reference.child("Users").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userName = snapshot.child("userName").getValue(String::class.java)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val binding = ItemHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ItemViewHolder(binding)
        } else {
            val binding = ItemProgressbarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            LoadingViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            populateItemRows(holder, position)
        } else if (holder is LoadingViewHolder) {
            showLoadingView(holder, position)
        }
    }

    override fun getItemCount(): Int {
        return ds?.size ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (ds?.get(position) == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    inner class ItemViewHolder(val binding: ItemHomeBinding) : RecyclerView.ViewHolder(binding.root)

    inner class LoadingViewHolder(val binding: ItemProgressbarBinding) : RecyclerView.ViewHolder(binding.root)

    private fun showLoadingView(viewHolder: LoadingViewHolder, position: Int) {
        viewHolder.binding.progressBar.visibility = View.VISIBLE
    }

    private fun populateItemRows(viewHolder: ItemViewHolder, position: Int) {
        val item = ds?.get(position)
        item?.let {
            Glide.with(viewHolder.binding.root)
                .load(it.productImage1) // Thay thế `getProductImage1()` bằng `productImage1` nếu nó là thuộc tính
                .into(viewHolder.binding.imgFood)

            viewHolder.binding.txtFoodName.text = it.productName // Tương tự cho `productName`
            viewHolder.binding.txtSold.text = "Đã bán: ${it.sold}"
            viewHolder.binding.txtRating.text = it.ratingStar.toString()
            viewHolder.binding.parentOfItemInHome.setOnClickListener {
                val intent = Intent(mContext, ProductInfoActivity::class.java).apply {
                    putExtra("productId", item.productId)
                    putExtra("productName", item.productName)
                    putExtra("productPrice", item.productPrice)
                    putExtra("productImage1", item.productImage1)
                    putExtra("productImage2", item.productImage2)
                    putExtra("productImage3", item.productImage3)
                    putExtra("productImage4", item.productImage4)
                    putExtra("ratingStar", item.ratingStar)
                    putExtra("productDescription", item.description)
                    putExtra("publisherId", item.publisherId)
                    putExtra("sold", item.sold)
                    putExtra("productType", item.productType)
                    putExtra("remainAmount", item.remainAmount)
                    putExtra("ratingAmount", item.ratingAmount)
                    putExtra("state", item.state)
                    putExtra("userId", userId)
                    putExtra("userName", userName)
                }
                mContext.startActivity(intent)
            }
        }
    }
}

