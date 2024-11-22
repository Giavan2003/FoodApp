package com.example.foodapp.adapter.DeliveryManagement_Seller



import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodapp.R
import com.example.foodapp.databinding.ItemOrderDetailListBinding
import com.example.foodapp.helper.FirebaseOrderDetailHelper
import com.example.foodapp.model.BillInfo
import com.example.foodapp.model.Product

class ListOfItemInOrderAdapter(
    private val mContext: Context,
    private val billInfos: List<BillInfo>
) : RecyclerView.Adapter<ListOfItemInOrderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderDetailListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val billInfo = billInfos[position]

        billInfo.productId?.let {
            FirebaseOrderDetailHelper().readProductInfo(it, object : FirebaseOrderDetailHelper.DataStatus2 {
                override fun DataIsLoaded(product: Product) {
                    holder.binding.txtProductNameInDetail.text = product.productName
                    holder.binding.txtPriceOfItemInDetail.text = "${convertToMoney(product.productPrice.toLong())} đ"
                    holder.binding.txtCountInDetail.text = "Count: ${billInfo.amount}"
                    holder.binding.imgProductImageInDetail.scaleType = ImageView.ScaleType.CENTER_CROP

                    try {
                        Glide.with(mContext)
                            .asBitmap()
                            .load(product.productImage1)
                            .placeholder(R.drawable.background_loading_layout)
                            .into(holder.binding.imgProductImageInDetail)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }

                override fun DataIsInserted() {}
                override fun DataIsUpdated() {}
                override fun DataIsDeleted() {}
            })
        }
    }

    override fun getItemCount(): Int {
        return billInfos.size
    }

    class ViewHolder(val binding: ItemOrderDetailListBinding) : RecyclerView.ViewHolder(binding.root)

    private fun convertToMoney(price: Long): String {
        val temp = price.toString()
        var output = ""
        var count = 3
        for (i in temp.length - 1 downTo 0) {
            count--
            if (count == 0) {
                count = 3
                output = ",${temp[i]}$output"
            } else {
                output = "${temp[i]}$output"
            }
        }

        return if (output.startsWith(",")) output.substring(1) else output
    }
}
