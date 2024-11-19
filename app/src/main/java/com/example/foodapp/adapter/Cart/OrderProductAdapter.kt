package com.example.foodapp.adapter.Cart

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodapp.R
import com.example.foodapp.databinding.ItemOrderProductBinding
import com.example.foodapp.model.CartInfo
import com.example.foodapp.model.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OrderProductAdapter(
    private val mContext: Context,
    private val mCartInfos: List<CartInfo>
) : RecyclerView.Adapter<OrderProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cartInfo = mCartInfos[position]

        FirebaseDatabase.getInstance().getReference("Products")
            .child(cartInfo.productId!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val product = snapshot.getValue(Product::class.java)
                    product?.let {
                        holder.binding.orderProductName.text = it.productName
                        holder.binding.orderProductPrice.text =
                            "${convertToMoney(it.productPrice.toLong())}đ"
                        Glide.with(mContext.applicationContext)
                            .load(it.productImage1)
                            .placeholder(R.mipmap.ic_launcher)
                            .into(holder.binding.orderProductImage)
                        holder.binding.amount.text = "Count: ${cartInfo.amount}"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    override fun getItemCount(): Int = mCartInfos.size

    class ViewHolder(val binding: ItemOrderProductBinding) : RecyclerView.ViewHolder(binding.root)

    private fun convertToMoney(price: Long): String {
        val temp = price.toString()
        val output = StringBuilder()
        var count = 3
        for (i in temp.length - 1 downTo 0) {
            count--
            if (count == 0) {
                count = 3
                output.insert(0, ",${temp[i]}")
            } else {
                output.insert(0, temp[i])
            }
        }

        return if (output.startsWith(",")) {
            output.substring(1)
        } else {
            output.toString()
        }
    }
}
