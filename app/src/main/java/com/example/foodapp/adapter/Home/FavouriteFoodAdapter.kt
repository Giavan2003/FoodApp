package com.example.foodapp.adapter.Home

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodapp.R
import com.example.foodapp.activity.ProductInfomation.ProductInfoActivity
import com.example.foodapp.databinding.ItemFavouriteProductBinding
import com.example.foodapp.model.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class FavouriteFoodAdapter(
    private val mContext: Context,
    private val favouriteLists: ArrayList<Product>,
    private val userId: String
) : RecyclerView.Adapter<FavouriteFoodAdapter.ViewHolder>() {

    private var userName: String? = null
    private val nf: NumberFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    init {
        FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userName = snapshot.child("userName").getValue(String::class.java)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle cancellation
                }
            })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFavouriteProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = favouriteLists[position]
        product?.let {
            val layoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams
            layoutParams.setMargins(0, if (position == 1) 120 else if (position == 0) 50 else 0, 0, 0)
            holder.itemView.layoutParams = layoutParams

            Glide.with(mContext)
                .load(product.productImage1)
                .placeholder(R.drawable.image_default)
                .into(holder.binding.imgFavouriteFood)

            holder.binding.txtFavouriteFoodName.text = product.productName
            val ratingStar = ((product.ratingStar ?: 0.0) * 10).toInt() / 10.0
            holder.binding.txtFavouriteRating.text = "$ratingStar/5.0"

            holder.binding.imgFavouriteRate.setImageResource(
                when {
                    (product.ratingStar ?: 0.0) >= 5 -> R.drawable.rating_star_filled
                    (product.ratingStar ?: 0.0) >= 3 -> R.drawable.rating_star_half
                    else -> R.drawable.rating_star_empty
                }
            )
            holder.binding.txtFavouriteFoodPrice.text = nf.format(product.productPrice)
            holder.binding.parentOfItemInFavourite.setOnClickListener {
                val intent = Intent(mContext, ProductInfoActivity::class.java).apply {
                    putExtra("productId", product.productId)
                    putExtra("productName", product.productName)
                    putExtra("productPrice", product.productPrice)
                    putExtra("productImage1", product.productImage1)
                    putExtra("productImage2", product.productImage2)
                    putExtra("productImage3", product.productImage3)
                    putExtra("productImage4", product.productImage4)
                    putExtra("ratingStar", product.ratingStar)
                    putExtra("productDescription", product.description)
                    putExtra("publisherId", product.publisherId)
                    putExtra("sold", product.sold)
                    putExtra("productType", product.productType)
                    putExtra("remainAmount", product.remainAmount)
                    putExtra("ratingAmount", product.ratingAmount)
                    putExtra("state", product.state)
                    putExtra("userId", userId)
                    putExtra("userName", userName)
                }
                mContext.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = favouriteLists.size

    class ViewHolder(val binding: ItemFavouriteProductBinding) : RecyclerView.ViewHolder(binding.root)
}
