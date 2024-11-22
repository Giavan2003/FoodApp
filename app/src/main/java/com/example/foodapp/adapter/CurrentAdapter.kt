package com.example.foodapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.databinding.ItemInfoCurrentProductBinding
import com.example.foodapp.model.InfoCurrentProduct

class CurrentAdapter(
    private val context: Context,
    private var listInfoCurrentProducts: List<InfoCurrentProduct>
) : RecyclerView.Adapter<CurrentAdapter.CurrentViewHolder>() {

    fun setData(list: List<InfoCurrentProduct>) {
        this.listInfoCurrentProducts = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentViewHolder {
        val binding = ItemInfoCurrentProductBinding.inflate(LayoutInflater.from(context), parent, false)
        return CurrentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CurrentViewHolder, position: Int) {
        val infoCurrentProduct = listInfoCurrentProducts[position]

        with(holder.binding) {
            imgCurrentProduct.setImageResource(infoCurrentProduct.resourceId)
            txtNameCurrentProduct.text = infoCurrentProduct.nameCurrentProduct
            txtState.text = infoCurrentProduct.state
            txtPriceCurrentProduct.text = infoCurrentProduct.priceCurrentProduct
        }
    }

    override fun getItemCount(): Int {
        return listInfoCurrentProducts.size
    }

    class CurrentViewHolder(val binding: ItemInfoCurrentProductBinding) : RecyclerView.ViewHolder(binding.root)
}
