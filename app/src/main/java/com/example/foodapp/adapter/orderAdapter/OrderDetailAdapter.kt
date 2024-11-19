package com.example.foodapp.adapter.orderAdapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodapp.R
import com.example.foodapp.databinding.ItemBillinfoBinding
import com.example.foodapp.model.BillInfo
import com.example.foodapp.model.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OrderDetailAdapter(
    private val context: Context,
    private val ds: ArrayList<BillInfo>
) : RecyclerView.Adapter<OrderDetailAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBillinfoBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewHolder = holder
        val billInfo = ds[position]

        FirebaseDatabase.getInstance().reference.child("Products").child(billInfo.productId!!).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tmp = snapshot.getValue(Product::class.java)
                tmp?.let {
                    viewHolder.binding.txtName.text = it.productName
                    viewHolder.binding.txtPrice.text = CurrencyFormatter.getFormatter().format(it.productPrice.toDouble() * billInfo.amount).toString()
                    Glide.with(context)
                        .load(it.productImage1)
                        .placeholder(R.drawable.default_image)
                        .into(viewHolder.binding.imgFood)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        viewHolder.binding.txtCount.text = "Count: ${billInfo.amount}"
    }

    override fun getItemCount(): Int {
        return ds.size
    }

    inner class ViewHolder(val binding: ItemBillinfoBinding) : RecyclerView.ViewHolder(binding.root)
}
