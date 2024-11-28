package com.example.foodapp.adapter.MyShopAdapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.example.foodapp.R
import com.example.foodapp.activity.MyShop.AddFoodActivity
import com.example.foodapp.activity.ProductInformation.ProductInfoActivity
import com.example.foodapp.custom.CustomMessageBox.CustomAlertDialog
import com.example.foodapp.custom.CustomMessageBox.FailToast
import com.example.foodapp.custom.CustomMessageBox.SuccessfulToast
import com.example.foodapp.databinding.ItemProductRevenueRowBinding
import com.example.foodapp.databinding.LayoutFoodItemBinding
import com.example.foodapp.model.Product
import com.google.firebase.database.FirebaseDatabase


class StatisticalAdapter(
    private var ds: MutableList<Product>,
    private val context: Context
) : BaseAdapter() {
    override fun getCount(): Int {
        return ds.size
    }

    override fun getItem(position: Int): Any {
        return ds[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // Sử dụng ViewBinding
        val binding: ItemProductRevenueRowBinding = if (convertView == null) {
            ItemProductRevenueRowBinding.inflate(LayoutInflater.from(context), parent, false)
        } else {
            ItemProductRevenueRowBinding.bind(convertView)
        }
        // Lấy sản phẩm từ danh sách
        val product = ds[position]
        // Cập nhật dữ liệu vào các view qua ViewBinding
        Glide.with(context)
            .load(product.productImage1)
            .placeholder(R.drawable.baseline_manages_product_24)
            .into(binding.productImage)
        binding.productName.text = product.productName
        binding.productSold.text = product.sold.toString()
        binding.productRevenue.text = "$${product.sold * product.productPrice}"
        return binding.root // Trả về root view đã được bind
    }
}
