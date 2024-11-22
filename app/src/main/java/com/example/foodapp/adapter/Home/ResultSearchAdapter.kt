package com.example.foodapp.adapter.Home

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodapp.R
import com.example.foodapp.activity.ProductInformation.ProductInfoActivity
import com.example.foodapp.databinding.ItemHomeFindLayoutBinding
import com.example.foodapp.databinding.ItemProgressbarBinding
import com.example.foodapp.model.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class ResultSearchAdapter(
    private val ds: MutableList<Product?>,
    private val userId: String,
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1
    private val nf: NumberFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    private var userName: String? = null

    init {
        FirebaseDatabase.getInstance().getReference("Users")
            .child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userName = snapshot.child("userName").getValue(String::class.java)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if needed
                }
            })
    }

    override fun getItemViewType(position: Int): Int {
        return if (ds[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val binding = ItemHomeFindLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ItemViewHolder(binding)
        } else {
            val binding = ItemProgressbarBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            LoadingViewHolder(binding)
        }
    }

    override fun getItemCount(): Int = ds.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            populateItemRows(holder, position)
        } else if (holder is LoadingViewHolder) {
            showLoadingView(holder)
        }
    }

    private fun showLoadingView(viewHolder: LoadingViewHolder) {
        viewHolder.binding.progressBar.visibility = View.VISIBLE
    }

    private fun populateItemRows(holder: ItemViewHolder, position: Int) {
        val item = ds[position]
        item?.let {
            with(holder.binding) {
                Glide.with(root)
                    .load(item.productImage1)
                    .placeholder(R.drawable.image_default)
                    .into(imgFood)

                txtFoodName.text = item.productName
                val ratingStar = ((item.ratingStar ?: 0.0) * 10).toInt() / 10.0
                txtRating.text = "$ratingStar/5.0"
                imgRate.setImageResource(
                    when {
                        (item.ratingStar ?: 0.0) >= 5 -> R.drawable.rating_star_filled
                        (item.ratingStar ?: 0.0) >= 3 -> R.drawable.rating_star_half
                        else -> R.drawable.rating_star_empty
                    }
                )
                txtFoodPrice.text = nf.format(item.productPrice)
                txtSold.text = "Đã bán: ${item.sold}"

                parentOfItemInFindActivity.setOnClickListener {
                    val intent = Intent(context, ProductInfoActivity::class.java).apply {
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
                    context.startActivity(intent)
                }
            }
        }
    }

    class ItemViewHolder(val binding: ItemHomeFindLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    class LoadingViewHolder(val binding: ItemProgressbarBinding) :
        RecyclerView.ViewHolder(binding.root)
}
