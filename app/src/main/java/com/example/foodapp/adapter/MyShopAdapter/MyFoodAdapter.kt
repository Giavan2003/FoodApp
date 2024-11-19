package com.example.foodapp.adapter.MyShopAdapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.google.firebase.database.FirebaseDatabase
import com.example.foodapp.R
import com.example.foodapp.activity.MyShop.AddFoodActivity
import com.example.foodapp.activity.ProductInformation.ProductInfoActivity
import com.example.foodapp.custom.CustomMessageBox.CustomAlertDialog
import com.example.foodapp.custom.CustomMessageBox.FailToast
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.LayoutFoodItemBinding
import com.example.foodapp.model.Product

class MyFoodAdapter(
    private var ds: List<Product>,
    private val context: Context,
    private val userId: String
) : RecyclerView.Adapter<MyFoodAdapter.ViewHolder>() {

    private val viewBinderHelper = ViewBinderHelper().apply { setOpenOnlyOne(true) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutFoodItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = ds[position]
        viewBinderHelper.bind(holder.binding.SwipeRevealLayout, product.productId)

        holder.binding.txtNameProdiuct.text = product.productName
        holder.binding.txtPrice.text = "${convertToMoney(product.productPrice.toLong())}đ"

        Glide.with(context)
            .load(product.productImage1)
            .placeholder(R.drawable.baseline_image_search_24)
            .into(holder.binding.imgFood)

        holder.binding.imgDelete.setOnClickListener {
            CustomAlertDialog(context, "Delete this product?")
            CustomAlertDialog.binding.btnYes.setOnClickListener {
                CustomAlertDialog.alertDialog.dismiss()
                FirebaseDatabase.getInstance().getReference("Products")
                    .child(product.productId ?: "")
                    .child("state")
                    .setValue("deleted")
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            (ds as ArrayList).remove(product)
                            notifyItemRemoved(position)
                            SuccessfulToast(context, "Delete product successfully!").showToast()
                        } else {
                            FailToast(context, "Delete product failed!").showToast()
                            Log.e("My Shop", "Error remove")
                        }
                    }
            }
            CustomAlertDialog.binding.btnNo.setOnClickListener {
                CustomAlertDialog.alertDialog.dismiss()
            }
            CustomAlertDialog.showAlertDialog()
        }

        holder.binding.imgEdit.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                holder.binding.SwipeRevealLayout.resetPivot()
            }
            val intent = Intent(context, AddFoodActivity::class.java)
            intent.putExtra("Product updating", product)
            context.startActivity(intent)
        }

        holder.binding.productContainer.setOnClickListener {
            val intent = Intent(context, ProductInfoActivity::class.java).apply {
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
                putExtra("userName", product)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = ds.size

    inner class ViewHolder(val binding: LayoutFoodItemBinding) : RecyclerView.ViewHolder(binding.root)

    private fun convertToMoney(price: Long): String {
        val temp = price.toString()
        val output = StringBuilder()
        var count = 3
        for (i in temp.indices.reversed()) {
            count--
            if (count == 0) {
                count = 3
                output.insert(0, ",${temp[i]}")
            } else {
                output.insert(0, temp[i])
            }
        }
        return if (output.startsWith(",")) output.substring(1) else output.toString()
    }
}
